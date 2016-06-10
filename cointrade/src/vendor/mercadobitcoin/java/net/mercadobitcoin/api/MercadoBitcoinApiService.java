/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import net.trader.api.ApiService;
import net.trader.beans.Balance;
import net.trader.beans.Coin;
import net.trader.beans.Currency;
import net.trader.beans.RecordFilter;
import net.trader.beans.UserConfiguration;
import net.trader.beans.OrderStatus;
import net.trader.beans.Operation;
import net.trader.beans.Order;
import net.trader.beans.OrderBook;
import net.trader.beans.RecordSide;
import net.trader.beans.Ticker;
import net.trader.exception.ApiProviderException;
import net.trader.utils.HostnameVerifierBag;
import net.trader.utils.JsonHashMap;
import net.trader.utils.TrustManagerBag;
import net.trader.utils.TrustManagerBag.SslContextTrustManager;

import com.google.gson.JsonParser;

public class MercadoBitcoinApiService extends ApiService {
	
	private enum ApiVersion {
		V1,
		V3;
	}
	
	private enum HttpMethod {
		GET,
		POST
	}
	
	private enum RequestMethod {
		LIST_SYSTEM_MESSAGES("list_system_messages"),
		GET_ACCOUNT_INFO("get_account_info"),
		GET_ORDER("get_order"),
		LIST_ORDERS("list_orders"),
		LIST_ORDERBOOK("list_orderbook"),
		PLACE_BUY_ORDER("place_buy_order"),
		PLACE_SELL_ORDER("place_sell_order"),
		CANCEL_ORDER("cancel_order"),
		GET_WITHDRAWAL("get_withdrawal"),
		WITHDRAWAL_COIN("withdrawal_coin"),
		
		V1_TRADE("Trade"),
		V1_CANCEL_ORDER("CancelOrder");
		
		public final String value;
		
		private RequestMethod(String requestMethod) {
			this.value = requestMethod;
		}
	}

	private static final String API_PATH = "/api/";
	private static final String TAPI_PATH = "/tapi/v3/";
	private static final String V1_TAPI_PATH = "/tapi/";
	private static final String ENCRYPT_ALGORITHM = "HmacSHA512";
	private static final String METHOD_PARAM = "tapi_method";
	private static final String V1_METHOD_PARAM = "method";
	private static final String DOMAIN = "https://www.mercadobitcoin.net";

	private byte[] mbTapiCodeBytes;

	public MercadoBitcoinApiService(UserConfiguration userConfiguration) throws ApiProviderException {
		super(userConfiguration);
		
		try {
			if (usingHttps()) {
				setSslContext(SslContextTrustManager.DEFAULT);
			}
		} catch (KeyManagementException e) {
			throw new ApiProviderException("Internal error: Invalid SSL Connection.");
		} catch (NoSuchAlgorithmException e) {
			throw new ApiProviderException("Internal error: Invalid SSL Algorithm.");
		}
		
		if (userConfiguration.getSecret() == null) {
			throw new ApiProviderException("Null code.");
		}
		
		if (userConfiguration.getKey() == null) {
			throw new ApiProviderException("Null key.");
		}
		
		this.mbTapiCodeBytes = userConfiguration.getSecret().getBytes();
	}
	
	private final boolean usingHttps() {
		return DOMAIN.toUpperCase().startsWith("HTTPS");
	}
	
	private String getDomain() {
		return DOMAIN + getApiPath();
	}

	private String getApiPath() {
		return API_PATH;
	}
	
	private Coin getCoin() {
		return userConfiguration.getCoin();
	}
	
	private Currency getCurrency() {
		return userConfiguration.getCurrency();
	}

	@Override
	public Ticker getTicker() throws ApiProviderException {
		String url = assemblyUrl("ticker");
		com.google.gson.JsonParser jsonParser = new JsonParser();
		com.google.gson.JsonObject jsonObject = (com.google.gson.JsonObject)jsonParser.parse(invokeApiMethod(url));
		return getTicker(jsonObject);
	}
	
	@Override
	public Balance getBalance() throws ApiProviderException {
		com.google.gson.JsonObject jsonObject = makeRequest(RequestMethod.GET_ACCOUNT_INFO.value, ApiVersion.V3);
		return getBalance(jsonObject);		
	}
	
	@Override
	public OrderBook getOrderBook() throws ApiProviderException {
		String url = assemblyUrl("orderbook");
		com.google.gson.JsonParser jsonParser = new JsonParser();
		com.google.gson.JsonObject jsonObject = (com.google.gson.JsonObject)jsonParser.parse(invokeApiMethod(url));
		OrderBook orderBook = new OrderBook(getCoin(), getCurrency());

		com.google.gson.JsonArray asking = jsonObject.getAsJsonArray("asks");
		ArrayList<Order> askOrders = new ArrayList<Order>();
		for (int i = 0; i < asking.size(); i++) {
			com.google.gson.JsonArray pairAmount = asking.get(i).getAsJsonArray();
			BigDecimal coinAmount = new BigDecimal(pairAmount.get(1).toString());
			BigDecimal currencyPrice = new BigDecimal(pairAmount.get(0).toString());
			Order order = new Order(
				getCoin(), getCurrency(), RecordSide.SELL, coinAmount, currencyPrice
			);
			order.setStatus(OrderStatus.ACTIVE);
			order.setPosition(i + 1);
			askOrders.add(order);
		}
		orderBook.setAskOrders(askOrders);
		
		com.google.gson.JsonArray bidding = jsonObject.get("bids").getAsJsonArray();
		ArrayList<Order> bidOrders = new ArrayList<Order>();
		for (int i = 0; i < bidding.size(); i++) {
			com.google.gson.JsonArray pairAmount = bidding.get(i).getAsJsonArray();
			BigDecimal coinAmount = new BigDecimal(pairAmount.get(1).toString());
			BigDecimal currencyPrice = new BigDecimal(pairAmount.get(0).toString());
			Order order = new Order(
				getCoin(), getCurrency(), RecordSide.BUY, coinAmount, currencyPrice
			);
			order.setStatus(OrderStatus.ACTIVE);
			order.setPosition(i + 1);
			bidOrders.add(order);
		}
		orderBook.setBidOrders(bidOrders);
		
		return orderBook;
	}
	
	@Override
	public List<Operation> getOperationList(Calendar from, Calendar to) throws ApiProviderException {
		return tradeList(from, to);
	}
	
	@Override
	public List<Order> getUserActiveOrders() throws ApiProviderException {
		RecordFilter orderFilter = new RecordFilter(getCoin(), getCurrency());
		orderFilter.setStatus(OrderStatus.ACTIVE);
			
		List<Order> orders = getUserOrders(orderFilter);
		Collections.sort(orders);
		return orders;
	}
	
	private List<Order> getUserCompletedOrders() throws ApiProviderException {
		RecordFilter orderFilter = new RecordFilter(getCoin(), getCurrency());
		orderFilter.setHasFills(true);
		
		List<Order> orders = getUserOrders(orderFilter);
		Collections.sort(orders);
		
		return orders;
	}
	
	@Override
	public List<Operation> getUserOperations() throws ApiProviderException {
		List<Operation> operations = new ArrayList<Operation>();
		for (Order o: getUserCompletedOrders()) {
			Order order = (Order) o;
			if (order.getOperations() != null)
				for (Operation operation: order.getOperations()) {
					operation.setSide(order.getSide());
					operations.add(operation);
				}
		}
		return operations;
	}
	
	@Override
	public Order cancelOrder(Order order) throws ApiProviderException {
		if (order == null) {
			throw new ApiProviderException("Invalid filter.");
		}
		
		v1CancelOrder(order);
		order.setStatus(OrderStatus.CANCELED);
		return order;
	}
	
	@Override
	public Order createOrder(Order order) throws ApiProviderException {
		if (order == null) {
			throw new ApiProviderException("Invalid order.");
		}
		
		v1CreateOrder(order);
		return order;
	}
	
	private Order v1CancelOrder(Order order) throws ApiProviderException {
		if (order == null) {
			throw new ApiProviderException("Invalid filter.");
		}
		
		makeRequest(getParams(order, ApiVersion.V1), RequestMethod.V1_CANCEL_ORDER.value, ApiVersion.V1);
		return null;
	}
	
	private Order v1CreateOrder(Order order) throws ApiProviderException {
		if (order == null) {
			throw new ApiProviderException("Invalid order.");
		}
		
		Order newOrder = new Order(
			order.getCoin(), order.getCurrency(), order.getSide(), 
			order.getCoinAmount(), order.getCurrencyPrice()
		);
		
		makeRequest(getParams(newOrder, ApiVersion.V1), RequestMethod.V1_TRADE.value, ApiVersion.V1);
		return null;
	}
	
	@Override
	public Order createBuyOrder(Order order) throws ApiProviderException {
		RecordSide side = RecordSide.BUY;
		order.setSide(side);
		return createOrder(order);
	}
	
	@Override
	public Order createSellOrder(Order order) throws ApiProviderException {
		RecordSide side = RecordSide.SELL;
		order.setSide(side);
		return createOrder(order);
	}
	
	private List<Operation> tradeList(Calendar from, Calendar to) throws ApiProviderException {
		List<String> paths = new ArrayList<String>();
		paths.add(from.getTimeInMillis() / 1000 + "/");
		
		if (to != null)
			paths.add(to.getTimeInMillis() / 1000 + "/");
		
		Operation[] operationArray = tradeList(paths.toArray(new String[0]));
		List<Operation> operations = Arrays.asList(operationArray);
		
		return operations;
	}

	private Operation[] tradeList(String ... complements) throws ApiProviderException {
		String url = assemblyUrl("trades", complements);
		com.google.gson.JsonParser jsonParser = new JsonParser();
		com.google.gson.JsonArray jsonArray = jsonParser.parse(invokeApiMethod(url.toString())).getAsJsonArray();
		
		//Convert Json response to object
		Operation[] operationList = new Operation[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			com.google.gson.JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
			
			long created = Integer.valueOf(jsonObject.get("date").toString());
			BigDecimal coinAmount = new BigDecimal(jsonObject.get("amount").toString());
			BigDecimal currencyPrice = new BigDecimal(jsonObject.get("price").toString());
			
			String sideString = jsonObject.get("type").getAsString();
			RecordSide side = sideString.equals("buy")? RecordSide.BUY: 
				(sideString.equals("sell")? RecordSide.SELL: null);
			
			Operation operation = new Operation(
				getCoin(), getCurrency(), side, coinAmount, currencyPrice
			);

			operation.setId(BigInteger.valueOf(jsonObject.get("tid").getAsLong()));
			operation.setRate(null);
			operation.setCreationDate(Calendar.getInstance());
			operation.getCreationDate().setTimeInMillis(created * 1000);			

			operationList[i] = operation;
		}
		return operationList;
	}
	
	private String assemblyUrl(String method, String ... pathParams) throws ApiProviderException {
		if (getCoin() == null || getCurrency() == null) {
			throw new ApiProviderException("Invalid coin pair.");
		}

		StringBuffer url = new StringBuffer(method);
		
		if (getCoin() == Coin.LTC) {
			url.append("_litecoin");
		}
		url.append("/");
		
		for (String pathParam : pathParams) {
			url.append(pathParam);
		}
		
		return url.toString();
	}

	private String invokeApiMethod(String params) throws ApiProviderException {
		try {
			URL url = generateApiUrl(params);
			HttpsURLConnection conn = getHttpGetConnection(url);
			getRequestToServer(conn);
			return getResponseFromServer(conn);
		} catch (MalformedURLException e) {
			throw new ApiProviderException("Internal error: Invalid URL.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new ApiProviderException("Internal error: Failure in connection.");
		}
	}
	
	private URL generateApiUrl(String params) throws MalformedURLException {
		URL url = new URL(getDomain() + params);
		return url;
	}

	private HttpsURLConnection getHttpGetConnection(URL url) throws IOException {
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		conn.setRequestMethod(HttpMethod.GET.name());
		conn.setRequestProperty("Content-Type", "application/json");
		
		return conn;
	}

	private void getRequestToServer(HttpsURLConnection conn) throws IOException {
		conn.getResponseCode();
	}
	
	private String getResponseFromServer(HttpsURLConnection conn) throws IOException {
		String responseStr = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} catch (IOException e) {
			if (conn.getErrorStream() != null) {
				reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
			}
		}
		StringBuilder sb = new StringBuilder();
		String line = null;

		if (reader != null) {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		}

		responseStr = sb.toString();
		return responseStr;
	}

	private List<Order> getUserOrders(RecordFilter filter) throws ApiProviderException {
		if (filter == null) {
			throw new ApiProviderException("Invalid filter.");
		}
		com.google.gson.JsonObject jsonResponse = makeRequest(
			getParams(filter), RequestMethod.LIST_ORDERS.value, ApiVersion.V3
		);
		
		com.google.gson.JsonArray jsonArray = jsonResponse != null? 
			jsonResponse.get("orders").getAsJsonArray(): new com.google.gson.JsonArray();
		
		List<Order> orders = new ArrayList<Order>();
		for (com.google.gson.JsonElement jsonOrder: jsonArray) {
			Order order = getOrder(jsonOrder.getAsJsonObject());
			orders.add(order);
		}
		
		return orders;
	}
	
	private JsonHashMap getParams(Order order, ApiVersion version) throws ApiProviderException {
		JsonHashMap hashMap = new JsonHashMap();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			
			if (version == ApiVersion.V3) {
				if (getCoin() != null && getCurrency() != null)
					params.put("coin_pair", getCurrency().getValue().toUpperCase() + getCoin().getValue().toUpperCase());
				if (order.getCoinAmount() != null)
					params.put("quantity", order.getCoinAmount());
				if (order.getCurrencyPrice() != null)
					params.put("limit_price", order.getCurrencyPrice());
			}
			else if (version == ApiVersion.V1) {
				if (getCoin() != null && getCurrency() != null)
					params.put("pair", getCoin().getValue().toLowerCase() + "_" + getCurrency().getValue().toLowerCase());
				if (order.getSide() != null)
					params.put(
						"type", 
						order.getSide() == RecordSide.BUY? "buy": 
						(order.getSide() == RecordSide.SELL? "sell": null)
					);
				if (order.getCoinAmount() != null)
					params.put("volume", order.getCoinAmount());
				if (order.getCurrencyPrice() != null)
					params.put("price", order.getCurrencyPrice());
				if (order.getId() != null)
					params.put("order_id", order.getId());
				if (order.getStatus() != null)
					params.put("status", order.getStatus());
				if (order.getCreationDate() != null)
					params.put("created", order.getCreationDate().getTime());
			}
			
			hashMap.putAll(params);
		} catch (Throwable e) {
			throw new ApiProviderException("Internal error: Unable to transform the parameters in a request.");
		}
		return hashMap;
	}
	
	private JsonHashMap getParams(RecordFilter filter) throws ApiProviderException {
		JsonHashMap hashMap = new JsonHashMap();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			
			if (filter.getCoin() != null && filter.getCurrency() != null)
				params.put(
					"coin_pair", 
					filter.getCurrency().getValue().toUpperCase() + 
					filter.getCoin().getValue().toUpperCase() 
				);
			if (filter.getSide() != null)
				params.put(
					"order_type", 
					filter.getSide() == RecordSide.BUY? "1": 
					(filter.getSide() == RecordSide.SELL? "2": null)
				);
			if (filter.getStatus() != null)
				params.put(
					"status_list",
					filter.getStatus() == OrderStatus.ACTIVE? "[2]": 
					(filter.getStatus() == OrderStatus.CANCELED? "[3]":
					(filter.getStatus() == OrderStatus.COMPLETED? "[4]": null))
				);
			if (filter.getHasFills() != null)
				params.put("has_fills", filter.getHasFills());
			if (filter.getFromId() != null)
				params.put("from_id", filter.getFromId());
			if (filter.getToId() != null)
				params.put("to_id", filter.getToId());
			if (filter.getFromTimestamp() != null)
				params.put("from_timestamp", filter.getFromTimestamp());
			if (filter.getToTimestamp() != null)
				params.put("to_timestamp", filter.getToTimestamp());
			
			hashMap.putAll(params);
		} catch (Throwable e) {
			throw new ApiProviderException("Internal error: Unable to transform the parameters in a request.");
		}
		return hashMap;
	}
	
	private Balance getBalance(com.google.gson.JsonObject balanceJsonObject) {
		Balance balance = new Balance(getCoin(), getCurrency());
		
		com.google.gson.JsonObject jsonObject = balanceJsonObject.getAsJsonObject("balance");
		
		com.google.gson.JsonObject brlJsonObject = jsonObject.getAsJsonObject("brl");
		BigDecimal brlAvailable = new BigDecimal(brlJsonObject.getAsJsonPrimitive("available").getAsString());
		BigDecimal brlTotal = new BigDecimal(brlJsonObject.getAsJsonPrimitive("total").getAsString());
		
		com.google.gson.JsonObject btcJsonObject = jsonObject.getAsJsonObject("btc");
		BigDecimal btcAvailable = new BigDecimal(btcJsonObject.getAsJsonPrimitive("available").getAsString());
		BigDecimal btcTotal = new BigDecimal(btcJsonObject.getAsJsonPrimitive("total").getAsString());
		//BigDecimal btcAmountOpenOrders = new BigDecimal(btcJsonObject.get("amount_open_orders").asDouble());
		
		com.google.gson.JsonObject ltcJsonObject = jsonObject.getAsJsonObject("ltc");
		BigDecimal ltcAvailable = new BigDecimal(ltcJsonObject.getAsJsonPrimitive("available").getAsString());
		BigDecimal ltcTotal = new BigDecimal(ltcJsonObject.getAsJsonPrimitive("total").getAsString());
		//BigDecimal ltcAmountOpenOrders = new BigDecimal(ltcJsonObject.get("amount_open_orders").asDouble());
		
		BigDecimal coinAmount = getCoin() == Coin.BTC? btcTotal: (getCoin() == Coin.LTC? ltcTotal: null);
		BigDecimal currencyAmount = getCurrency() == Currency.BRL? brlTotal: null;
		
		BigDecimal coinLocked = getCoin() == Coin.BTC? btcTotal.subtract(btcAvailable):
			(getCoin() == Coin.LTC? ltcTotal.subtract(ltcAvailable): null);
		BigDecimal currencyLocked =
			getCurrency() == Currency.BRL? brlTotal.subtract(brlAvailable): null;
		
		balance.setCoinAmount(coinAmount);
		balance.setCurrencyAmount(currencyAmount);
		balance.setCoinLocked(coinLocked);
		balance.setCurrencyLocked(currencyLocked);
		
		return balance;
	}
	
	private Ticker getTicker(com.google.gson.JsonObject tickerJsonObject) throws ApiProviderException {
		Ticker ticker = new Ticker(getCoin(), getCurrency());
		
		com.google.gson.JsonObject jsonObject = tickerJsonObject.get("ticker").getAsJsonObject();
		
		ticker.setHigh(new BigDecimal(jsonObject.getAsJsonPrimitive("high").getAsString()));
		ticker.setLow(new BigDecimal(jsonObject.getAsJsonPrimitive("low").getAsString()));
		ticker.setVol(new BigDecimal(jsonObject.getAsJsonPrimitive("vol").getAsString()));
		ticker.setLast(new BigDecimal(jsonObject.getAsJsonPrimitive("last").getAsString()));
		ticker.setBuy(new BigDecimal(jsonObject.getAsJsonPrimitive("buy").getAsString()));
		ticker.setSell(new BigDecimal(jsonObject.getAsJsonPrimitive("sell").getAsString()));
		ticker.setDate(new BigDecimal(jsonObject.getAsJsonPrimitive("date").getAsString()));
		
		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();

		from.setTime(new Date());
		from.add(Calendar.HOUR, -3);
		to.setTime(new Date());
		BigDecimal last3HourVolume = new BigDecimal(0);
		List<Operation> operations = getOperationList(from, to);
		
		for (Operation operation: operations)
			last3HourVolume = last3HourVolume.add(operation.getCoinAmount());
		
		ticker.setLast3HourVolume(last3HourVolume);
		
		return ticker;
	}
	
	private Order getOrder(com.google.gson.JsonObject jsonObject) {
		String coinPair = jsonObject.get("coin_pair").getAsString().toUpperCase();
		Coin coin = Coin.valueOf(coinPair.substring(3, 6).toUpperCase());
		Currency currency = Currency.valueOf(coinPair.substring(0, 3).toUpperCase());
		Integer sideInt = jsonObject.get("order_type").getAsInt();
		RecordSide side = sideInt == 1? RecordSide.BUY: (sideInt == 2? RecordSide.SELL: null);
		BigDecimal coinAmount = new BigDecimal(jsonObject.get("quantity").getAsString());
		BigDecimal currencyPrice = new BigDecimal(jsonObject.get("limit_price").getAsString());
		
		Order order = new Order(coin, currency, side, coinAmount, currencyPrice);
		order.setId(BigInteger.valueOf(jsonObject.get("order_id").getAsLong()));
		Integer statusInt = jsonObject.get("status").getAsInt();
		OrderStatus status = 
			statusInt == 2? OrderStatus.ACTIVE: 
			(statusInt == 3? OrderStatus.CANCELED:
			(statusInt == 4? OrderStatus.COMPLETED: null));
		order.setStatus(status);
		long created = Integer.valueOf(jsonObject.get("created_timestamp").getAsString());
		order.setCreationDate(Calendar.getInstance());
		order.getCreationDate().setTimeInMillis((long)created * 1000);
		
		List<Operation> operations = new ArrayList<Operation>();
		com.google.gson.JsonArray jsonOperationArray = jsonObject.get("operations") == null?
			new com.google.gson.JsonArray(): jsonObject.get("operations").getAsJsonArray();
		
		for (com.google.gson.JsonElement jsonOperation: jsonOperationArray) {
			Operation operation = getOperation(
				jsonOperation.getAsJsonObject(), side
			);
			operation.setSide(order.getSide());
			operations.add(operation);
		}
		order.setOperations(operations);
		
		return order;
	}
	
	private Operation getOperation(com.google.gson.JsonObject jsonObject, RecordSide side) {
		Operation operation;
		
		long created = Long.valueOf(jsonObject.get("executed_timestamp").getAsString());
		BigDecimal coinAmount = new BigDecimal(jsonObject.get("quantity").getAsString());
		BigDecimal currencyPrice = new BigDecimal(jsonObject.get("price").getAsString());
		
		operation = new Operation(
			getCoin(), getCurrency(), side, coinAmount, currencyPrice
		);
		
		operation.setId(BigInteger.valueOf((jsonObject.get("operation_id").getAsLong())));
		operation.setRate(new BigDecimal(jsonObject.get("fee_rate").getAsString()));
		operation.setCreationDate(Calendar.getInstance());
		operation.getCreationDate().setTimeInMillis(created * 1000);
		
		return operation;
	}
	
	private com.google.gson.JsonObject makeRequest(String method, ApiVersion version) throws ApiProviderException {
		return makeRequest(new JsonHashMap(), method, version);
	}
	
	private com.google.gson.JsonObject makeRequest(JsonHashMap params, String method, ApiVersion version) 
		throws ApiProviderException {
		if (version == ApiVersion.V3) {
			params.put("tapi_nonce", generateTonce());
			params.put(METHOD_PARAM, method);
		}
		else if (version == ApiVersion.V1) {
			params.put(V1_METHOD_PARAM, method);
			params.put("tonce", generateTonce());
		}
			
		String jsonResponse = invokeTapiMethod(params, version);
		if (jsonResponse == null) {
			throw new ApiProviderException("Internal error: null response from the server.");
		}
		
		JsonParser jsonParser = new JsonParser();
		com.google.gson.JsonObject jsonObject = (com.google.gson.JsonObject)jsonParser.parse(jsonResponse);
		
		com.google.gson.JsonObject returnData = jsonObject.getAsJsonObject(
			version == ApiVersion.V3? "response_data": (version == ApiVersion.V1? "return": null)
		);
		// putting delay time
		try {
			TimeUnit.MILLISECONDS.sleep(1010);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return (returnData == null) ? null : returnData;
	}
	
	private String invokeTapiMethod(JsonHashMap params, ApiVersion version) throws ApiProviderException {
		try {
			String jsonParams = params.toUrlEncoded();
			String signature = version == ApiVersion.V3? generateSignature(TAPI_PATH + "?" + jsonParams):
				(version == ApiVersion.V1? generateSignature(jsonParams): null);
			URL url = generateTapiUrl(version);
			HttpURLConnection conn = getHttpPostConnection(url, signature, version);
			postRequestToServer(conn, params);
			return getResponseFromServer(conn);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ApiProviderException("Internal error: Failure in connection.");
		} catch (NoSuchAlgorithmException e) {
			throw new ApiProviderException("Internal error: Cryptography Algorithm not found.");
		} catch (InvalidKeyException e) {
			throw new ApiProviderException("Invalid Key or Signature.");
		} 
	}

	private String generateSignature(String parameters) throws NoSuchAlgorithmException, InvalidKeyException {
		SecretKeySpec key = null;
		Mac mac = null;

		key = new SecretKeySpec(mbTapiCodeBytes, ENCRYPT_ALGORITHM);
		mac = Mac.getInstance(ENCRYPT_ALGORITHM);
		mac.init(key);
		String sign = encodeHexString(mac.doFinal(parameters.getBytes()));
		return sign;
	}

	private URL generateTapiUrl(ApiVersion version) throws MalformedURLException {
		URL url = version == ApiVersion.V3? new URL(DOMAIN + TAPI_PATH):
			(version == ApiVersion.V1? new URL(DOMAIN + V1_TAPI_PATH): null);
		return url;
	}
	
	private HttpURLConnection getHttpPostConnection(URL url, String signature, ApiVersion version) 
		throws IOException {
		HttpURLConnection conn;
		if (usingHttps()) {
			conn = (HttpsURLConnection) url.openConnection();
		} else {
			conn = (HttpURLConnection) url.openConnection();
		}
		
		conn.setRequestMethod(HttpMethod.POST.name());
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		if (version == ApiVersion.V3) {
			conn.setRequestProperty("TAPI-ID", userConfiguration.getKey());
			conn.setRequestProperty("TAPI-MAC", signature);
		}
		else if (version == ApiVersion.V1) {
			conn.setRequestProperty("Key", userConfiguration.getKey());
			conn.setRequestProperty("Sign", signature);
		}
		conn.setDoOutput(true);

		return conn;
	}

	private void postRequestToServer(HttpURLConnection conn, JsonHashMap params) throws IOException {
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(params.toUrlEncoded());
		wr.flush();
		wr.close();
	}

	private String getResponseFromServer(HttpURLConnection conn) throws IOException {
		String responseStr = null;
		BufferedReader reader = null;
		reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}

		responseStr = sb.toString();
		return responseStr;
	}
	
	@SuppressWarnings("incomplete-switch")
	private final void setSslContext(SslContextTrustManager sctm) throws NoSuchAlgorithmException, KeyManagementException {
		// Enables protocols "TLSv1", "TLSv1.1" and "TLSv1.2"
		SSLContext sc = SSLContext.getInstance("TLS");

		switch (sctm) {
			case BYPASS:
				sc.init(null, TrustManagerBag.BYPASS_TRUST_MANAGER_LIST, TrustManagerBag.SECURE_RANDOM);
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
				HttpsURLConnection.setDefaultHostnameVerifier(HostnameVerifierBag.BYPASS_HOSTNAME_VERIFIER);
				break;
			case DEFAULT:
				HttpsURLConnection.setDefaultSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
				break;
		}
	}
	
	private static final String encodeHexString(byte[] bytes) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	private static final long generateTonce() {
		long unixTime = System.currentTimeMillis() / 1000L;
		return unixTime;
	}
	
}