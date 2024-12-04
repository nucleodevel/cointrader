package org.nucleodevel.cointrader.api.blinktrade;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.nucleodevel.cointrader.api.ApiService;
import org.nucleodevel.cointrader.beans.Balance;
import org.nucleodevel.cointrader.beans.Broker;
import org.nucleodevel.cointrader.beans.Coin;
import org.nucleodevel.cointrader.beans.CoinCurrencyPair;
import org.nucleodevel.cointrader.beans.Currency;
import org.nucleodevel.cointrader.beans.Operation;
import org.nucleodevel.cointrader.beans.Order;
import org.nucleodevel.cointrader.beans.OrderBook;
import org.nucleodevel.cointrader.beans.OrderStatus;
import org.nucleodevel.cointrader.beans.OrderType;
import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.beans.Ticker;
import org.nucleodevel.cointrader.beans.UserConfiguration;
import org.nucleodevel.cointrader.exception.ApiProviderException;
import org.nucleodevel.cointrader.utils.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class BlinktradeApiService extends ApiService {

	private Client client = ClientBuilder.newClient();

	// --------------------- Constructor

	public BlinktradeApiService(UserConfiguration userConfiguration, CoinCurrencyPair coinCurrencyPair)
			throws ApiProviderException {
		super(userConfiguration, coinCurrencyPair);
	}

	// --------------------- Getters and setters

	@Override
	protected String getDomain() {
		return "https://api.blinktrade.com";
	}

	@Override
	protected String getPublicApiUrl() {
		return getDomain() + getPublicApiPath();
	}

	@Override
	protected String getPrivateApiUrl() {
		return getDomain() + getPrivateApiPath();
	}

	@Override
	protected String getPublicApiPath() {
		return "/api/v1/" + getCurrency().getValue().toUpperCase() + "/";
	}

	@Override
	protected String getPrivateApiPath() {
		return "/tapi/v1/message";
	}

	@Override
	public TimeZone getTimeZone() {
		return TimeZone.getTimeZone("GMT-03:00");
	}

	@Override
	protected void makeActionInConstructor() throws ApiProviderException {
		if (userConfiguration.getKey() == null) {
			throw new ApiProviderException("Key cannot be null");
		}

		if (userConfiguration.getSecret() == null) {
			throw new ApiProviderException("Secret cannot be null");
		}

		if (userConfiguration.getBroker() == null) {
			throw new ApiProviderException("Broker cannot be null");
		}
	}

	// --------------------- Overrided methods

	@Override
	public Ticker getTicker() throws ApiProviderException {
		return null;
	}

	@Override
	public Balance getBalance() throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "U2");
		request.put("BalanceReqID", Integer.valueOf((int) (System.currentTimeMillis() / 1000)));

		String response = makePrivateRequest(GSON.toJson(request));
		JsonObject balanceJsonObject = JsonParser.parseString(response).getAsJsonObject();

		return getBalance(balanceJsonObject);
	}

	@Override
	public OrderBook getOrderBook() throws ApiProviderException {

		String responseMessage = makePublicRequest("orderbook");
		JsonObject orderBookJsonObject = JsonParser.parseString(responseMessage).getAsJsonObject();

		return getOrderBook(orderBookJsonObject);
	}

	@Override
	public List<Operation> getOperationList(Calendar from, Calendar to) throws ApiProviderException {
		String responseMessage = makePublicRequest("trades");

		JsonElement operationListJsonElement = JsonParser.parseString(responseMessage);
		JsonArray operationListJsonArray = operationListJsonElement.getAsJsonArray();

		Operation[] operationList = new Operation[operationListJsonArray.size()];
		for (int i = 0; i < operationListJsonArray.size(); i++) {
			JsonObject jsonObject = operationListJsonArray.get(i).getAsJsonObject();

			long created = Integer.valueOf(jsonObject.get("date").toString());
			BigDecimal coinAmount = new BigDecimal(jsonObject.get("amount").toString());
			BigDecimal currencyPrice = new BigDecimal(jsonObject.get("price").toString());

			String sideString = jsonObject.get("side").getAsString();
			RecordSide side = sideString.equals("buy") ? RecordSide.BUY
					: (sideString.equals("sell") ? RecordSide.SELL : null);

			Operation operation = new Operation(getCoin(), getCurrency(), side, coinAmount, currencyPrice);

			BigInteger id = BigInteger.valueOf(jsonObject.get("tid").getAsLong());
			operation.setId("" + id.longValue());

			operation.setRate(null);
			operation.setCreationDate(Calendar.getInstance());
			operation.getCreationDate().setTimeInMillis(created * 1000);

			operationList[i] = operation;
		}

		List<Operation> operations = Arrays.asList(operationList);

		return operations;
	}

	@Override
	public List<Order> getUserActiveOrders() throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		List<String> filters = new ArrayList<String>(1);
		filters.add("has_leaves_qty eq 1");

		request.put("MsgType", "U4");
		request.put("OrdersReqID", Integer.valueOf((int) (System.currentTimeMillis() / 1000)));
		request.put("Page", Integer.valueOf(0));
		request.put("PageSize", Integer.valueOf(100));
		request.put("Filter", filters);

		String response = makePrivateRequest(GSON.toJson(request));
		JsonObject jo = JsonParser.parseString(response).getAsJsonObject();

		JsonArray activeOrdListGrp = jo.getAsJsonArray("Responses").get(0).getAsJsonObject()
				.getAsJsonArray("OrdListGrp");
		List<Order> activeOrders = new ArrayList<Order>();
		if (activeOrdListGrp != null)
			for (JsonElement jsonElement : activeOrdListGrp)
				if (jsonElement != null) {
					JsonArray jsonArray = jsonElement.getAsJsonArray();
					Order order = getOrder(jsonArray);
					activeOrders.add(order);
				}
		return activeOrders;
	}

	@Override
	public List<Operation> getUserOperations() throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		List<String> filters = new ArrayList<String>(1);
		filters.add("has_cum_qty eq 1");

		request.put("MsgType", "U4");
		request.put("OrdersReqID", Integer.valueOf((int) (System.currentTimeMillis() / 1000)));
		request.put("Page", Integer.valueOf(0));
		request.put("PageSize", Integer.valueOf(100));
		request.put("Filter", filters);

		String response = makePrivateRequest(GSON.toJson(request));
		JsonObject jo = JsonParser.parseString(response).getAsJsonObject();

		JsonArray completedOrdListGrp = jo.getAsJsonArray("Responses").get(0).getAsJsonObject()
				.getAsJsonArray("OrdListGrp");
		List<Operation> clientOperations = new ArrayList<Operation>();
		if (completedOrdListGrp != null)
			for (JsonElement jsonElement : completedOrdListGrp)
				if (jsonElement != null) {
					JsonArray jsonArray = jsonElement.getAsJsonArray();
					Operation operation = getOperation(jsonArray);
					clientOperations.add(operation);
				}
		return clientOperations;
	}

	@Override
	public Order cancelOrder(Order order) throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "F");
		request.put("ClOrdID", ((Order) order).getClientId());
		request.put("BrokerID", getBrokerId());

		makePrivateRequest(GSON.toJson(request));

		return null;

	}

	@Override
	public Order createOrder(Order order) throws ApiProviderException {

		Integer clientOrderId = Integer.valueOf((int) (System.currentTimeMillis() / 1000));
		Coin coin = order.getCoin();
		Currency currency = order.getCurrency();
		RecordSide side = order.getSide();
		OrderType type = OrderType.LIMITED;
		BigDecimal coinAmount = order.getCoinAmount();
		BigDecimal currencyPrice = order.getCurrencyPrice();

		coinAmount = coinAmount.multiply(new BigDecimal(SATOSHI_BASE));
		currencyPrice = currencyPrice.multiply(new BigDecimal(currency == Currency.BRL ? SATOSHI_BASE + 1 : 1));

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "D");
		request.put("ClOrdID", clientOrderId);
		request.put("Symbol", coin.getValue() + currency.getValue());
		request.put("Side", side == RecordSide.BUY ? "1" : (side == RecordSide.SELL ? "2" : null));
		request.put("OrdType", type == OrderType.MARKET ? "1" : (type == OrderType.LIMITED ? "2" : null));
		request.put("OrderQty", coinAmount.toBigInteger());
		request.put("Price", currencyPrice.toBigInteger());
		request.put("BrokerID", getBrokerId());

		makePrivateRequest(GSON.toJson(request));

		return null;
	}

	// --------------------- Request methods

	private String makePublicRequest(String requestUrl) throws ApiProviderException {

		// putting delay time
		try {
			TimeUnit.MILLISECONDS.sleep(1010);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String url = getPublicApiUrl() + requestUrl;

		return client.target(url.toString()).request(MediaType.APPLICATION_JSON).get(String.class);
	}

	private String makePrivateRequest(String requestMessage) throws ApiProviderException {

		String nonce = Long.toString(System.currentTimeMillis());

		String signature = null;
		try {
			final String ALGORITHM = "HmacSHA256";
			try {
				Mac sha_HMAC = Mac.getInstance(ALGORITHM);
				SecretKeySpec secret_key = new SecretKeySpec(userConfiguration.getSecret().getBytes(), ALGORITHM);
				sha_HMAC.init(secret_key);
				byte encoded[] = sha_HMAC.doFinal(nonce.getBytes());

				StringBuilder hexString = new StringBuilder();
				for (byte b : encoded) {
					String hex = Integer.toHexString(0xff & b);
					if (hex.length() == 1)
						hexString.append('0');
					hexString.append(hex);
				}

				signature = hexString.toString();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} catch (Exception e) {
			throw new ApiProviderException("Message signature fail", e);
		}

		String url = getPrivateApiUrl();

		Builder request = client.target(url).request(MediaType.APPLICATION_JSON)
				.header("Content-Type", "application/json").header("APIKey", userConfiguration.getKey())
				.header("Nonce", nonce).header("Signature", signature);

		Response response = request.post(Entity.json(requestMessage));
		return response.hasEntity() ? response.readEntity(String.class) : null;

	}

	// --------------------- Json to object

	private Balance getBalance(JsonObject balanceJsonObject) {
		Coin coin = getCoin();
		Currency currency = getCurrency();

		Balance balance = new Balance(coin, currency);

		balanceJsonObject = balanceJsonObject.getAsJsonArray("Responses").get(0).getAsJsonObject();

		balance.setClientId(balanceJsonObject.getAsJsonPrimitive("ClientID").getAsString());

		balance.setCoinAmount(balanceJsonObject.getAsJsonObject("4").getAsJsonPrimitive(coin.getValue())
				.getAsBigDecimal().divide(new BigDecimal(currency == Currency.BRL ? SATOSHI_BASE : 1)));
		balance.setCoinLocked(balanceJsonObject.getAsJsonObject("4").getAsJsonPrimitive(coin.getValue() + "_locked")
				.getAsBigDecimal().divide(new BigDecimal(currency == Currency.BRL ? SATOSHI_BASE : 1)));

		balance.setCurrencyAmount(balanceJsonObject.getAsJsonObject("4").getAsJsonPrimitive(currency.getValue())
				.getAsBigDecimal().divide(new BigDecimal(currency == Currency.BRL ? SATOSHI_BASE : 1)));
		balance.setCurrencyLocked(
				balanceJsonObject.getAsJsonObject("4").getAsJsonPrimitive(currency.getValue() + "_locked")
						.getAsBigDecimal().divide(new BigDecimal(currency == Currency.BRL ? SATOSHI_BASE : 1)));
		return balance;
	}

	private OrderBook getOrderBook(JsonObject orderBookJsonObject) {
		JsonArray bidArray = orderBookJsonObject.getAsJsonArray("bids");
		JsonArray askArray = orderBookJsonObject.getAsJsonArray("asks");

		List<Order> bidOrders = new ArrayList<Order>();
		for (JsonElement row : bidArray) {
			JsonArray rowArray = row.getAsJsonArray();
			Order bidOrder = new Order(getCoin(), getCurrency(), RecordSide.BUY, rowArray.get(1).getAsBigDecimal(),
					rowArray.get(0).getAsBigDecimal());
			bidOrder.setClientId(rowArray.get(2).getAsBigInteger());
			bidOrder.setStatus(OrderStatus.ACTIVE);
			bidOrders.add(bidOrder);
		}

		List<Order> askOrders = new ArrayList<Order>();
		for (JsonElement row : askArray) {
			JsonArray rowArray = row.getAsJsonArray();
			Order askOrder = new Order(getCoin(), getCurrency(), RecordSide.SELL, rowArray.get(1).getAsBigDecimal(),
					rowArray.get(0).getAsBigDecimal());
			askOrder.setClientId(rowArray.get(2).getAsBigInteger());
			askOrder.setStatus(OrderStatus.ACTIVE);
			askOrders.add(askOrder);
		}

		OrderBook orderBook = new OrderBook(getCoin(), getCurrency());
		orderBook.setBidOrders(bidOrders);
		orderBook.setAskOrders(askOrders);

		return orderBook;
	}

	public Order getOrder(JsonArray jsonArray) {
		String sideString = jsonArray.get(8).getAsString();
		RecordSide side = sideString.equals("1") ? RecordSide.BUY : (sideString.equals("2") ? RecordSide.SELL : null);
		BigDecimal cumQty = jsonArray.get(2).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE));
		BigDecimal leavesQty = jsonArray.get(4).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE));
		BigDecimal coinAmount = cumQty.add(leavesQty);
		BigDecimal currencyPrice = jsonArray.get(11).getAsBigDecimal()
				.divide(new BigDecimal(getCurrency() == Currency.BRL ? SATOSHI_BASE : 1));

		Order order = new Order(getCoin(), getCurrency(), side, coinAmount, currencyPrice);

		order.setClientId(jsonArray.get(0).getAsBigInteger());

		BigInteger id = jsonArray.get(1).getAsBigInteger();
		order.setId("" + id.longValue());

		order.setCreationDate(Utils.getCalendar(jsonArray.get(12).getAsString()));

		return order;
	}

	public Operation getOperation(JsonArray jsonArray) {
		String sideString = jsonArray.get(8).getAsString();
		RecordSide side = sideString.equals("1") ? RecordSide.BUY : (sideString.equals("2") ? RecordSide.SELL : null);
		BigDecimal cumQty = jsonArray.get(2).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE));
		BigDecimal leavesQty = jsonArray.get(4).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE));
		BigDecimal coinAmount = cumQty.add(leavesQty);
		BigDecimal currencyPrice = jsonArray.get(11).getAsBigDecimal()
				.divide(new BigDecimal(getCurrency() == Currency.BRL ? SATOSHI_BASE : 1));

		Operation operation = new Operation(getCoin(), getCurrency(), side, coinAmount, currencyPrice);

		operation.setClientId(jsonArray.get(0).getAsBigInteger());

		BigInteger id = jsonArray.get(1).getAsBigInteger();
		operation.setId("" + id.longValue());

		operation.setCreationDate(Utils.getCalendar(jsonArray.get(12).getAsString()));

		return operation;
	}

	// --------------------- Custom

	private static final long SATOSHI_BASE = 100000000;

	private static final Gson GSON = new Gson();

	private String getBrokerId() {
		if (userConfiguration.getBroker() == Broker.FOXBIT)
			return "4";
		return null;
	}

}
