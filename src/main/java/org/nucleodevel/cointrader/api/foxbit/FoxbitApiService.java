package org.nucleodevel.cointrader.api.foxbit;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.nucleodevel.cointrader.api.ApiService;
import org.nucleodevel.cointrader.beans.Balance;
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

public class FoxbitApiService extends ApiService {

	private Client client = ClientBuilder.newClient();

	// --------------------- Constructor

	public FoxbitApiService(UserConfiguration userConfiguration, CoinCurrencyPair coinCurrencyPair)
			throws ApiProviderException {
		super(userConfiguration, coinCurrencyPair);
	}

	// --------------------- Getters and setters

	@Override
	protected String getDomain() {
		return "https://api.foxbit.com.br";
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
		return "/rest/v3";
	}

	@Override
	protected String getPrivateApiPath() {
		return "/rest/v3";
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

	private String getCoinCurrencyPairUrlString() {
		return getCoin().getValue().toLowerCase() + getCurrency().getValue().toLowerCase();
	}

	// --------------------- Overrided methods

	@Override
	public Ticker getTicker() throws ApiProviderException {

		JsonElement responseJsonElement = request("GET",
				"/rest/v3/markets/" + getCoinCurrencyPairUrlString() + "/ticker/24hr", null, null);
		JsonObject responseJsonObject = responseJsonElement.getAsJsonObject();

		return getTicker(responseJsonObject);
	}

	@Override
	public Balance getBalance() throws ApiProviderException {

		JsonElement responseJsonElement = request("GET", "/rest/v3/accounts", null, null);
		JsonObject responseJsonObject = responseJsonElement.getAsJsonObject();

		return getBalance(responseJsonObject);
	}

	@Override
	public OrderBook getOrderBook() throws ApiProviderException {

		JsonElement responseJsonElement = request("GET",
				"/rest/v3/markets/" + getCoinCurrencyPairUrlString() + "/orderbook", null, null);
		JsonObject responseJsonObject = responseJsonElement.getAsJsonObject();

		return getOrderBook(responseJsonObject);
	}

	@Override
	public List<Operation> getOperationList(Calendar from, Calendar to) throws ApiProviderException {

		Map<String, String> queryParams = new HashMap<>();

		queryParams.put("page_size", "200");

		if (from != null)
			queryParams.put("start_time", Utils.toISO8601UTCWithoutMillisAndFinalZ(from));

		if (to != null)
			queryParams.put("end_time", Utils.toISO8601UTCWithoutMillisAndFinalZ(to));

		JsonElement responseJsonElement = request("GET",
				"/rest/v3/markets/" + getCoinCurrencyPairUrlString() + "/trades/history", queryParams, null);
		JsonObject responseJsonObject = responseJsonElement.getAsJsonObject();

		List<Operation> operationList = new ArrayList<Operation>();

		JsonArray dataJsonArray = responseJsonObject.getAsJsonArray("data");
		for (JsonElement rowJsonElement : dataJsonArray) {
			JsonObject rowJsonObject = rowJsonElement.getAsJsonObject();

			BigInteger id = rowJsonObject.getAsJsonPrimitive("id").getAsBigInteger();
			BigDecimal coinAmount = rowJsonObject.getAsJsonPrimitive("volume").getAsBigDecimal();
			BigDecimal currencyPrice = rowJsonObject.getAsJsonPrimitive("price").getAsBigDecimal();

			String sideString = rowJsonObject.getAsJsonPrimitive("taker_side").getAsString();
			RecordSide side = sideString.equals("BUY") ? RecordSide.BUY
					: (sideString.equals("SELL") ? RecordSide.SELL : null);

			String createdAtStr = rowJsonObject.getAsJsonPrimitive("created_at").getAsString();
			Calendar createdAt = Utils.fromISO8601UTC(createdAtStr);

			Operation operation = new Operation(getCoin(), getCurrency(), side, coinAmount, currencyPrice);

			operation.setId("" + id.longValue());
			operation.setRate(null);
			operation.setCreationDate(createdAt);

			operationList.add(operation);
		}

		return operationList;
	}

	@Override
	public List<Order> getUserActiveOrders() throws ApiProviderException {

		Map<String, String> queryParams = new HashMap<>();

		queryParams.put("state", "ACTIVE");

		JsonElement responseJsonElement = request("GET", "/rest/v3/orders", queryParams, null);
		JsonObject responseJsonObject = responseJsonElement.getAsJsonObject();

		JsonArray dataJsonArray = responseJsonObject.getAsJsonArray("data");

		List<Order> activeOrders = new ArrayList<Order>();
		for (JsonElement rowJsonElement : dataJsonArray) {
			JsonObject rowJsonObject = rowJsonElement.getAsJsonObject();

			Order order = getOrder(rowJsonObject);
			activeOrders.add(order);
		}

		return activeOrders;
	}

	@Override
	public List<Operation> getUserOperations() throws ApiProviderException {

		Map<String, String> queryParams = new HashMap<>();

		queryParams.put("market_symbol", getCoinCurrencyPairUrlString());

		JsonElement responseJsonElement = request("GET", "/rest/v3/trades", queryParams, null);
		JsonObject responseJsonObject = responseJsonElement.getAsJsonObject();

		List<Operation> operationList = new ArrayList<Operation>();

		JsonArray dataJsonArray = responseJsonObject.getAsJsonArray("data");
		for (JsonElement rowJsonElement : dataJsonArray) {
			JsonObject rowJsonObject = rowJsonElement.getAsJsonObject();

			BigInteger id = rowJsonObject.getAsJsonPrimitive("id").getAsBigInteger();
			BigDecimal coinAmount = rowJsonObject.getAsJsonPrimitive("quantity").getAsBigDecimal();
			BigDecimal currencyPrice = rowJsonObject.getAsJsonPrimitive("price").getAsBigDecimal();

			String sideString = rowJsonObject.getAsJsonPrimitive("side").getAsString();
			RecordSide side = sideString.equals("BUY") ? RecordSide.BUY
					: (sideString.equals("SELL") ? RecordSide.SELL : null);

			String createdAtStr = rowJsonObject.getAsJsonPrimitive("created_at").getAsString();
			Calendar createdAt = Utils.fromISO8601UTC(createdAtStr);

			Operation operation = new Operation(getCoin(), getCurrency(), side, coinAmount, currencyPrice);

			operation.setId("" + id.longValue());
			operation.setRate(null);
			operation.setCreationDate(createdAt);

			operationList.add(operation);
		}

		return operationList;
	}

	@Override
	public Order cancelOrder(Order order) throws ApiProviderException {

		JsonObject orderToCancel = new JsonObject();
		orderToCancel.addProperty("type", "ID");
		orderToCancel.addProperty("id", order.getId());
		request("PUT", "/rest/v3/orders/cancel", null, orderToCancel.toString());

		return order;
	}

	@Override
	public Order createOrder(Order order) throws ApiProviderException {

		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(8);
		DecimalFormatSymbols symbols = decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);

		String typeStr = order.getType() == OrderType.LIMITED ? "LIMIT"
				: (order.getType() == OrderType.MARKET ? "MARKET" : null);

		JsonObject orderToCreate = new JsonObject();
		orderToCreate.addProperty("market_symbol", getCoinCurrencyPairUrlString());
		orderToCreate.addProperty("side", order.getSide().getValue());
		orderToCreate.addProperty("type", typeStr);
		orderToCreate.addProperty("price", decFmt.format(order.getCurrencyPrice()));
		orderToCreate.addProperty("quantity", decFmt.format(order.getCoinAmount()));
		JsonElement responseJsonElement = request("POST", "/rest/v3/orders", null, orderToCreate.toString());

		JsonObject responseJsonObject = responseJsonElement.getAsJsonObject();
		BigInteger id = responseJsonObject.getAsJsonPrimitive("id").getAsBigInteger();

		order.setId("" + id.longValue());

		return order;
	}

	// --------------------- Request methods

	private Map<String, String> sign(String method, String path, Map<String, String> params, String body)
			throws NoSuchAlgorithmException, InvalidKeyException {

		StringBuilder queryString = new StringBuilder();
		if (params != null) {
			for (Map.Entry<String, String> param : params.entrySet()) {
				if (queryString.length() > 0) {
					queryString.append("&");
				}
				queryString.append(param.getKey()).append("=").append(param.getValue());
			}
		}

		String rawBody = body != null ? body : "";
		long timestamp = System.currentTimeMillis();
		String preHash = timestamp + method + path + queryString + rawBody;

		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(userConfiguration.getSecret().getBytes(StandardCharsets.UTF_8),
				"HmacSHA256");
		sha256_HMAC.init(secret_key);
		byte[] hash = sha256_HMAC.doFinal(preHash.getBytes(StandardCharsets.UTF_8));

		StringBuilder hexString = new StringBuilder();
		for (byte b : hash) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}

		Map<String, String> signatureData = new HashMap<>();
		signatureData.put("signature", hexString.toString());
		signatureData.put("timestamp", Long.toString(timestamp));
		signatureData.put("queryString", queryString.toString());

		return signatureData;
	}

	private JsonElement request(String method, String path, Map<String, String> params, String body)
			throws ApiProviderException {

		// putting delay time
		try {
			TimeUnit.MILLISECONDS.sleep(1010);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Map<String, String> signatureData;
		try {
			signatureData = sign(method, path, params, body);
			String queryString = signatureData.get("queryString");
			StringBuilder urlBuilder = new StringBuilder(getDomain()).append(path);
			if (!queryString.isEmpty()) {
				urlBuilder.append("?").append(queryString);
			}
			String url = urlBuilder.toString();

			String signature = signatureData.get("signature");
			String timestamp = signatureData.get("timestamp");

			Builder request = client.target(url).request(MediaType.APPLICATION_JSON)
					.header("X-FB-ACCESS-KEY", userConfiguration.getKey()).header("X-FB-ACCESS-TIMESTAMP", timestamp)
					.header("X-FB-ACCESS-SIGNATURE", signature).header("Content-Type", "application/json");

			String responseStr = null;

			if ("GET".equalsIgnoreCase(method)) {
				responseStr = request.get(String.class);
			} else if ("POST".equalsIgnoreCase(method)) {
				Response response = request.post(Entity.json(body));
				responseStr = response.hasEntity() ? response.readEntity(String.class) : null;
			} else if ("PUT".equalsIgnoreCase(method)) {
				Response response = request.put(Entity.json(body));
				responseStr = response.hasEntity() ? response.readEntity(String.class) : null;
			} else {
				throw new IllegalArgumentException("Unsupported HTTP method: " + method);
			}

			return responseStr == null ? null : JsonParser.parseString(responseStr);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new ApiProviderException("Internal error: Cryptography Algorithm not found.");
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			throw new ApiProviderException("Invalid Key or Signature.");
		}
	}

	// --------------------- Json to object

	private Ticker getTicker(JsonObject tickerJsonObject) {
		Coin coin = getCoin();
		Currency currency = getCurrency();

		Ticker ticker = new Ticker(coin, currency);

		JsonObject rolling24hJsonObj = tickerJsonObject.getAsJsonArray("data").get(0).getAsJsonObject()
				.getAsJsonObject("rolling_24h");

		ticker.setHigh(rolling24hJsonObj.getAsJsonPrimitive("high").getAsBigDecimal());
		ticker.setLow(rolling24hJsonObj.getAsJsonPrimitive("low").getAsBigDecimal());
		ticker.setVol(rolling24hJsonObj.getAsJsonPrimitive("volume").getAsBigDecimal());

		ticker.setLast3HourVolume(ticker.getVol().divide(BigDecimal.valueOf(8L)));

		return ticker;
	}

	private Balance getBalance(JsonObject balanceJsonObject) {
		Coin coin = getCoin();
		Currency currency = getCurrency();

		Balance balance = new Balance(coin, currency);

		JsonArray dataJsonArray = balanceJsonObject.getAsJsonArray("data");
		for (JsonElement rowJsonElement : dataJsonArray) {
			JsonObject rowJsonObject = rowJsonElement.getAsJsonObject();

			String currencySymbol = rowJsonObject.getAsJsonPrimitive("currency_symbol").getAsString();
			if (currencySymbol.equals(getCoin().getValue().toLowerCase())) {
				balance.setCoinAmount(rowJsonObject.getAsJsonPrimitive("balance").getAsBigDecimal());
				balance.setCoinLocked(rowJsonObject.getAsJsonPrimitive("balance_locked").getAsBigDecimal());
			} else if (currencySymbol.equals(getCurrency().getValue().toLowerCase())) {
				balance.setCurrencyAmount(rowJsonObject.getAsJsonPrimitive("balance").getAsBigDecimal());
				balance.setCurrencyLocked(rowJsonObject.getAsJsonPrimitive("balance_locked").getAsBigDecimal());
			}
		}
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
			bidOrder.setClientId(null);
			bidOrder.setStatus(OrderStatus.ACTIVE);
			bidOrders.add(bidOrder);
		}

		List<Order> askOrders = new ArrayList<Order>();
		for (JsonElement row : askArray) {
			JsonArray rowArray = row.getAsJsonArray();
			Order askOrder = new Order(getCoin(), getCurrency(), RecordSide.SELL, rowArray.get(1).getAsBigDecimal(),
					rowArray.get(0).getAsBigDecimal());
			askOrder.setClientId(null);
			askOrder.setStatus(OrderStatus.ACTIVE);
			askOrders.add(askOrder);
		}

		OrderBook orderBook = new OrderBook(getCoin(), getCurrency());
		orderBook.setBidOrders(bidOrders);
		orderBook.setAskOrders(askOrders);

		return orderBook;
	}

	public Order getOrder(JsonObject jsonObject) {

		BigInteger id = jsonObject.getAsJsonPrimitive("id").getAsBigInteger();
		BigInteger clientId = jsonObject.has("client_order_id") ? null
				: jsonObject.getAsJsonPrimitive("client_order_id").getAsBigInteger();

		BigDecimal coinAmount = jsonObject.getAsJsonPrimitive("quantity").getAsBigDecimal();
		BigDecimal currencyPrice = jsonObject.getAsJsonPrimitive("price").getAsBigDecimal();

		String sideString = jsonObject.getAsJsonPrimitive("side").getAsString();
		RecordSide side = sideString.equals("BUY") ? RecordSide.BUY
				: (sideString.equals("SELL") ? RecordSide.SELL : null);

		String statusString = jsonObject.getAsJsonPrimitive("state").getAsString();
		OrderStatus status = statusString.equals("ACTIVE") ? OrderStatus.ACTIVE
				: (statusString.equals("CANCELED") || statusString.equals("PARTIALLY_CANCELED") ? OrderStatus.CANCELED
						: (statusString.equals("FILLED") || statusString.equals("PARTIALLY_FILLED")
								? OrderStatus.COMPLETED
								: null));

		String createdAtStr = jsonObject.getAsJsonPrimitive("created_at").getAsString();
		Calendar createdAt = Utils.fromISO8601UTC(createdAtStr);

		Order order = new Order(getCoin(), getCurrency(), side, coinAmount, currencyPrice);

		order.setClientId(clientId);
		order.setId("" + id.longValue());
		order.setStatus(status);

		order.setCreationDate(createdAt);

		return order;
	}

}
