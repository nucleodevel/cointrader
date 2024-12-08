/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package org.nucleodevel.cointrader.api.poloniex;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.nucleodevel.cointrader.api.AbstractApiService;
import org.nucleodevel.cointrader.beans.Balance;
import org.nucleodevel.cointrader.beans.Coin;
import org.nucleodevel.cointrader.beans.CoinCurrencyPair;
import org.nucleodevel.cointrader.beans.Currency;
import org.nucleodevel.cointrader.beans.Operation;
import org.nucleodevel.cointrader.beans.Order;
import org.nucleodevel.cointrader.beans.OrderBook;
import org.nucleodevel.cointrader.beans.OrderStatus;
import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.beans.Ticker;
import org.nucleodevel.cointrader.beans.UserConfiguration;
import org.nucleodevel.cointrader.exception.ApiProviderException;
import org.nucleodevel.cointrader.utils.JsonHashMap;

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

public class PoloniexApiService extends AbstractApiService {

	private Client client = ClientBuilder.newClient();

	// --------------------- Constructor

	public PoloniexApiService(UserConfiguration userConfiguration, CoinCurrencyPair coinCurrencyPair)
			throws ApiProviderException {
		super(userConfiguration, coinCurrencyPair);
	}

	// --------------------- Getters and setters

	@Override
	protected String getDomain() {
		return "https://poloniex.com";
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
		return "/public";
	}

	@Override
	protected String getPrivateApiPath() {
		return "/tradingApi";
	}

	@Override
	public TimeZone getTimeZone() {
		return TimeZone.getTimeZone("GMT");
	}

	@Override
	protected void makeActionInConstructor() throws ApiProviderException {
		this.mbTapiCodeBytes = userConfiguration.getSecret().getBytes();
	}

	// --------------------- Overrided methods

	@Override
	public Ticker getTicker() throws ApiProviderException {
		JsonObject tickerJsonObject = (JsonObject) makePublicRequest("returnTicker", new JsonHashMap());

		JsonObject jsonObject = tickerJsonObject.getAsJsonObject(getCurrency().getValue() + "_" + getCoin().getValue());

		Ticker ticker = new Ticker(getCoin(), getCurrency());

		ticker.setVol(jsonObject.getAsJsonPrimitive("quoteVolume").getAsBigDecimal());
		ticker.setHigh(jsonObject.getAsJsonPrimitive("highestBid").getAsBigDecimal());
		ticker.setLow(jsonObject.getAsJsonPrimitive("lowestAsk").getAsBigDecimal());

		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();

		from.setTime(new Date());
		from.add(Calendar.HOUR, -3);
		to.setTime(new Date());
		BigDecimal last3HourVolume = new BigDecimal(0);
		List<Operation> last3HourOperations = getOperationList(from, to);

		for (Operation operation : last3HourOperations)
			last3HourVolume = last3HourVolume.add(operation.getCoinAmount());

		return ticker;
	}

	@Override
	public Balance getBalance() throws ApiProviderException {
		JsonHashMap args = new JsonHashMap();
		args.put("command", "returnCompleteBalances");

		JsonObject balanceJsonObject = (JsonObject) makePrivateRequest("returnBalances", args);
		return getBalance(balanceJsonObject);
	}

	@Override
	public OrderBook getOrderBook() throws ApiProviderException {
		JsonObject orderBookJsonObject = (JsonObject) makePublicRequest("returnOrderBook", new JsonHashMap());
		return getOrderBook(orderBookJsonObject);
	}

	@Override
	public List<Operation> getOperationList(Calendar from, Calendar to) throws ApiProviderException {
		JsonHashMap args = new JsonHashMap();
		args.put("start", (Long) (from.getTimeInMillis() / 1000));
		args.put("end", (Long) (to.getTimeInMillis() / 1000));

		JsonArray jsonArray = makePublicRequest("returnTradeHistory", args).getAsJsonArray();

		// Convert Json response to object
		Operation[] operationList = new Operation[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

			BigDecimal coinAmount = jsonObject.get("amount").getAsBigDecimal();
			BigDecimal currencyPrice = jsonObject.get("rate").getAsBigDecimal();

			String sideString = jsonObject.get("type").getAsString();
			RecordSide side = sideString.equals("buy") ? RecordSide.BUY
					: (sideString.equals("sell") ? RecordSide.SELL : null);

			Operation operation = new Operation(getCoin(), getCurrency(), side, coinAmount, currencyPrice);

			BigInteger id = BigInteger.valueOf(jsonObject.get("tradeID").getAsLong());
			operation.setId("" + id.longValue());

			operation.setRate(null);
			operation.setCreationDate(Calendar.getInstance());
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				operation.getCreationDate().setTime(df.parse(jsonObject.get("date").getAsString()));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			operationList[i] = operation;
		}

		List<Operation> operations = Arrays.asList(operationList);

		return operations;
	}

	@Override
	public List<Order> getUserActiveOrders() throws ApiProviderException {
		JsonHashMap args = new JsonHashMap();
		args.put("command", "returnOpenOrders");
		args.put("currencyPair", getCurrency() + "_" + getCoin());

		JsonArray activeOrdersJsonArray = (JsonArray) makePrivateRequest("returnOpenOrders", args);

		List<Order> orders = new ArrayList<Order>();
		for (JsonElement jsonOrder : activeOrdersJsonArray) {
			Order order = getOrder(jsonOrder.getAsJsonObject());
			order.setStatus(OrderStatus.ACTIVE);
			orders.add(order);
		}

		return orders;
	}

	@Override
	public List<Operation> getUserOperations() throws ApiProviderException {
		JsonHashMap args = new JsonHashMap();

		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		from.setTime(new Date());
		from.add(Calendar.HOUR, -48);
		to.setTime(new Date());

		args.put("command", "returnTradeHistory");
		args.put("currencyPair", getCurrency() + "_" + getCoin());
		args.put("start", ((Long) (from.getTimeInMillis() / 1000)).toString());
		args.put("end", ((Long) (to.getTimeInMillis() / 1000)).toString());

		JsonArray jsonArray = (JsonArray) makePrivateRequest("returnTradeHistory", args);

		// Convert Json response to object
		Operation[] operationList = new Operation[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

			BigDecimal coinAmount = jsonObject.get("amount").getAsBigDecimal();
			BigDecimal currencyPrice = jsonObject.get("rate").getAsBigDecimal();

			String sideString = jsonObject.get("type").getAsString();
			RecordSide side = sideString.equals("buy") ? RecordSide.BUY
					: (sideString.equals("sell") ? RecordSide.SELL : null);

			Operation operation = new Operation(getCoin(), getCurrency(), side, coinAmount, currencyPrice);

			BigInteger id = BigInteger.valueOf(jsonObject.get("tradeID").getAsLong());
			operation.setId("" + id.longValue());

			operation.setRate(null);
			operation.setCreationDate(Calendar.getInstance());
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				operation.getCreationDate().setTime(df.parse(jsonObject.get("date").getAsString()));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			operationList[i] = operation;
		}

		List<Operation> operations = Arrays.asList(operationList);

		return operations;
	}

	@Override
	public Order cancelOrder(Order order) throws ApiProviderException {
		if (order == null) {
			throw new ApiProviderException("Invalid order.");
		}

		JsonHashMap args = new JsonHashMap();
		args.put("command", "cancelOrder");
		args.put("currencyPair", getCurrency() + "_" + getCoin());
		args.put("orderNumber", order.getId().toString());

		makePrivateRequest("cancelOrder", args);

		order.setStatus(OrderStatus.CANCELED);
		return order;
	}

	@Override
	public Order createOrder(Order order) throws ApiProviderException {
		if (order == null) {
			throw new ApiProviderException("Invalid order.");
		}

		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(8);

		DecimalFormatSymbols symbols = decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);

		JsonHashMap args = new JsonHashMap();
		String command = order.getSide().getValue().toLowerCase();

		args.put("command", command);
		args.put("currencyPair", getCurrency() + "_" + getCoin());
		args.put("rate", decFmt.format(order.getCurrencyPrice()));
		args.put("amount", decFmt.format(order.getCoinAmount()));

		makePrivateRequest(command, args);

		return order;
	}

	// --------------------- Request methods

	private JsonElement makePublicRequest(String method, JsonHashMap args) throws ApiProviderException {

		// putting delay time
		try {
			TimeUnit.MILLISECONDS.sleep(1010);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (getCoin() == null || getCurrency() == null) {
			throw new ApiProviderException("Invalid coin pair.");
		}

		// add method and nonce to args
		if (args == null) {
			args = new JsonHashMap();
		}

		String argsVar = "";
		for (Map.Entry<String, Object> arg : args.entrySet())
			argsVar += "&" + arg.getKey() + "=" + arg.getValue();

		String url = getPublicApiUrl() + "?command=" + method + "&currencyPair=" + getCurrency() + "_" + getCoin()
				+ argsVar;

		String responseStr = client.target(url.toString()).request(MediaType.APPLICATION_JSON).get(String.class);

		return JsonParser.parseString(responseStr);
	}

	@SuppressWarnings("deprecation")
	private JsonElement makePrivateRequest(String method, JsonHashMap args) throws ApiProviderException {

		// putting delay time
		try {
			TimeUnit.MILLISECONDS.sleep(1010);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			setAuthKeys();
		} catch (Exception e1) {
			throw new ApiProviderException();
		}
		if (!initialized) {
			throw new ApiProviderException();
		}

		// add method and nonce to args
		if (args == null) {
			args = new JsonHashMap();
		}
		long nonce = System.currentTimeMillis() * 1000;
		args.put("method", method);
		args.put("nonce", Long.toString(nonce));

		// create url form encoded post data
		String postData = "";
		for (Iterator<String> iter = args.keySet().iterator(); iter.hasNext();) {
			String arg = iter.next();
			if (postData.length() > 0) {
				postData += "&";
			}
			postData += arg + "=" + URLEncoder.encode((String) args.get(arg));
		}

		String url = getPrivateApiUrl();

		Builder request;
		try {
			request = client.target(url).request(MediaType.APPLICATION_JSON).header("Key", userConfiguration.getKey())
					.header("Sign", toHex(mac.doFinal(postData.getBytes("UTF-8"))))
					.header("Content-Type", "application/x-www-form-urlencoded").header("User-Agent", USER_AGENT);
		} catch (UnsupportedEncodingException | IllegalStateException e) {
			throw new ApiProviderException("Signature fail", e);
		}

		Response response = request.post(Entity.json(postData));
		String responseStr = response.hasEntity() ? response.readEntity(String.class) : null;
		return JsonParser.parseString(responseStr);
	}

	// --------------------- Json to object

	private Balance getBalance(JsonObject balanceJsonObject) {
		Balance balance = new Balance(getCoin(), getCurrency());

		JsonObject coinJsonObject = balanceJsonObject.getAsJsonObject(getCoin().getValue());
		BigDecimal coinAvailable = new BigDecimal(coinJsonObject.getAsJsonPrimitive("available").getAsString());
		BigDecimal coinLocked = new BigDecimal(coinJsonObject.getAsJsonPrimitive("onOrders").getAsString());
		BigDecimal coinAmount = coinAvailable.add(coinLocked);

		JsonObject currencyJsonObject = balanceJsonObject.getAsJsonObject(getCurrency().getValue());
		BigDecimal currencyAvailable = new BigDecimal(currencyJsonObject.getAsJsonPrimitive("available").getAsString());
		BigDecimal currencyLocked = new BigDecimal(currencyJsonObject.getAsJsonPrimitive("onOrders").getAsString());
		BigDecimal currencyAmount = currencyAvailable.add(currencyLocked);

		balance.setCoinAmount(coinAmount);
		balance.setCurrencyAmount(currencyAmount);
		balance.setCoinLocked(coinLocked);
		balance.setCurrencyLocked(currencyLocked);

		return balance;
	}

	private OrderBook getOrderBook(JsonObject orderBookJsonObject) throws ApiProviderException {
		OrderBook orderBook = new OrderBook(getCoin(), getCurrency());

		JsonArray asking = orderBookJsonObject.getAsJsonArray("asks");
		ArrayList<Order> askOrders = new ArrayList<Order>();
		for (int i = 0; i < asking.size(); i++) {
			JsonArray pairAmount = asking.get(i).getAsJsonArray();
			BigDecimal coinAmount = pairAmount.get(1).getAsBigDecimal();
			BigDecimal currencyPrice = pairAmount.get(0).getAsBigDecimal();
			Order order = new Order(getCoin(), getCurrency(), RecordSide.SELL, coinAmount, currencyPrice);
			order.setStatus(OrderStatus.ACTIVE);
			order.setPosition(i + 1);
			askOrders.add(order);
		}
		orderBook.setAskOrders(askOrders);

		JsonArray bidding = orderBookJsonObject.get("bids").getAsJsonArray();
		ArrayList<Order> bidOrders = new ArrayList<Order>();
		for (int i = 0; i < bidding.size(); i++) {
			JsonArray pairAmount = bidding.get(i).getAsJsonArray();
			BigDecimal coinAmount = new BigDecimal(pairAmount.get(1).toString());
			BigDecimal currencyPrice = pairAmount.get(0).getAsBigDecimal();
			Order order = new Order(getCoin(), getCurrency(), RecordSide.BUY, coinAmount, currencyPrice);
			order.setStatus(OrderStatus.ACTIVE);
			order.setPosition(i + 1);
			bidOrders.add(order);
		}
		orderBook.setBidOrders(bidOrders);

		return orderBook;
	}

	private Order getOrder(JsonObject jsonObject) {
		Coin coin = getCoin();
		Currency currency = getCurrency();
		String sideString = jsonObject.get("type").getAsString();
		RecordSide side = sideString.equals("buy") ? RecordSide.BUY
				: (sideString.equals("sell") ? RecordSide.SELL : null);
		BigDecimal coinAmount = new BigDecimal(jsonObject.get("amount").getAsString());
		BigDecimal currencyPrice = new BigDecimal(jsonObject.get("rate").getAsString());

		Order order = new Order(coin, currency, side, coinAmount, currencyPrice);

		BigInteger id = BigInteger.valueOf(jsonObject.get("orderNumber").getAsLong());
		order.setId("" + id.longValue());

		order.setCreationDate(Calendar.getInstance());
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			order.getCreationDate().setTime(df.parse(jsonObject.get("date").getAsString()));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return order;
	}

	// --------------------- Auxiliares methods

	public void setAuthKeys() throws Exception {
		SecretKeySpec keyspec = null;
		try {
			keyspec = new SecretKeySpec(userConfiguration.getSecret().getBytes("UTF-8"), "HmacSHA512");
		} catch (UnsupportedEncodingException uee) {
			throw new Exception("HMAC-SHA512 doesn't seem to be installed", uee);
		}

		try {
			mac = Mac.getInstance("HmacSHA512");
		} catch (NoSuchAlgorithmException nsae) {
			throw new Exception("HMAC-SHA512 doesn't seem to be installed", nsae);
		}

		try {
			mac.init(keyspec);
		} catch (InvalidKeyException ike) {
			throw new Exception("Invalid key for signing request", ike);
		}
		initialized = true;
	}

	private String toHex(byte[] b) throws UnsupportedEncodingException {
		return String.format("%040x", new BigInteger(1, b));
	}

	// --------------------- Constants and attributes

	private static final String USER_AGENT = "Mozilla/5.0 (compatible; BTCE-API/1.0; MSIE 6.0 compatible; +https://github.com/abwaters/btce-api)";
	private boolean initialized = false;

	@SuppressWarnings("unused")
	private byte[] mbTapiCodeBytes;
	private Mac mac;

}