/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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

import net.mercadobitcoin.common.security.HostnameVerifierBag;
import net.mercadobitcoin.common.security.TrustManagerBag;
import net.mercadobitcoin.common.security.TrustManagerBag.SslContextTrustManager;
import net.mercadobitcoin.tradeapi.to.MbBalance;
import net.mercadobitcoin.tradeapi.to.MbOperation;
import net.mercadobitcoin.tradeapi.to.MbOrder;
import net.mercadobitcoin.tradeapi.to.MbOrderBook;
import net.mercadobitcoin.tradeapi.to.OrderFilter;
import net.mercadobitcoin.tradeapi.to.MbTicker;
import net.mercadobitcoin.tradeapi.to.Withdrawal;
import net.mercadobitcoin.tradeapi.to.MbOrder.OrderStatus;
import net.mercadobitcoin.util.JsonHashMap;
import net.mercadobitcoin.util.TimestampInterval;
import net.trader.api.ApiService;
import net.trader.beans.Balance;
import net.trader.beans.Operation;
import net.trader.beans.Order;
import net.trader.beans.OrderBook;
import net.trader.beans.RecordSide;
import net.trader.beans.Ticker;
import net.trader.exception.ApiProviderException;
import net.trader.robot.UserConfiguration;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Public API Client service to communicate with Mercado Bitcoin API.
 * Used to retrieve general information about trades and orders in Mercado Bitcoin.
 */
public class MbApiService extends ApiService {
	
	protected enum HttpMethod {
		GET,
		POST
	}
	
	private enum RequestMethod {
		GET_INFO("getInfo"),
		ORDER_LIST("OrderList"),
		TRADE("Trade"),
		CANCEL_ORDER("CancelOrder"),
		WITHDRAWAL_BITCOIN("withdrawal_bitcoin");
		
		public final String value;
		
		private RequestMethod(String requestMethod) {
			this.value = requestMethod;
		}
	}

	private static final String API_PATH = "/api/";
	private static final String TAPI_PATH = "/tapi/";
	private static final String ENCRYPT_ALGORITHM = "HmacSHA512";
	private static final String METHOD_PARAM = "method";
	private static final String DOMAIN = "https://www.mercadobitcoin.net";

	private static long intervalToReadUserCanceledOrders = 1200;
	private static long totalTimeToReadUserCanceledOrders = 43200;
	private static long lastTimeByReadingUserCanceledOrders = 0;
	private static long totalTimeToReadUserCompletedOrders = 43200;
	
	private static List<Order> userCanceledOrders;
	private byte[] mbTapiCodeBytes;

	public MbApiService(UserConfiguration userConfiguration) throws ApiProviderException {
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
	
	protected final boolean usingHttps() {
		return DOMAIN.toUpperCase().startsWith("HTTPS");
	}
	
	protected String getDomain() {
		return DOMAIN + getApiPath();
	}
	
	protected String getDomainTrade() {
		return DOMAIN + getTapiPath();
	}

	public String getApiPath() {
		return API_PATH;
	}
	
	public String getTapiPath() {
		return TAPI_PATH;
	}
	
	private String getCoin() {
		return userConfiguration.getCoin();
	}
	
	private String getCurrency() {
		return userConfiguration.getCurrency();
	}

	@Override
	public Ticker getTicker() throws ApiProviderException {
		String url = assemblyUrl("ticker");
		JsonObject jsonObject = JsonObject.readFrom(invokeApiMethod(url));
		JsonObject ticketJsonObject = jsonObject.get("ticker").asObject();
		return new MbTicker(ticketJsonObject);
	}
	
	@Override
	public Balance getBalance() throws ApiProviderException {
		JsonObject jsonResponse = makeRequest(RequestMethod.GET_INFO.value);
		Balance balance = new MbBalance(jsonResponse, getCoin(), getCurrency());
		return balance;		
	}
	
	@Override
	public OrderBook getOrderBook() throws ApiProviderException {
		String url = assemblyUrl("orderbook");
		JsonObject jsonObject = JsonObject.readFrom(invokeApiMethod(url));
		MbOrderBook orderBook = new MbOrderBook(getCoin(), getCurrency());

		JsonArray asking = jsonObject.get("asks").asArray();
		MbOrder[] asks = new MbOrder[asking.size()];
		for (int i = 0; i < asking.size(); i++) {
			JsonArray pairAmount = asking.get(i).asArray();
			BigDecimal coinAmount = new BigDecimal(pairAmount.get(1).toString());
			BigDecimal currencyPrice = new BigDecimal(pairAmount.get(0).toString());
			MbOrder order = new MbOrder(
				getCoin(), getCurrency(), RecordSide.SELL, coinAmount, currencyPrice
			);
			asks[i] = order;
		}
		orderBook.setAsks(asks);
		
		JsonArray bidding = jsonObject.get("bids").asArray();
		MbOrder[] bids = new MbOrder[bidding.size()];
		for (int i = 0; i < bidding.size(); i++) {
			JsonArray pairAmount = bidding.get(i).asArray();
			BigDecimal coinAmount = new BigDecimal(pairAmount.get(1).toString());
			BigDecimal currencyPrice = new BigDecimal(pairAmount.get(0).toString());
			MbOrder order = new MbOrder(
				getCoin(), getCurrency(), RecordSide.BUY, coinAmount, currencyPrice
			);
			bids[i] = order;
		}
		orderBook.setBids(bids);
		
		return orderBook;
	}
	
	@Override
	public List<Order> getUserActiveOrders() throws ApiProviderException {
		OrderFilter orderFilter = new OrderFilter(getCoin(), getCurrency());
		orderFilter.setStatus(OrderStatus.ACTIVE);
			
		List<Order> orders = getUserOrders(orderFilter);
		Collections.sort(orders);
		return orders;
	}
	
	@Override
	public List<Order> getUserCanceledOrders() throws ApiProviderException {
		long now = (new Date()).getTime() / 1000;
		
		if (userCanceledOrders == null) {
			userCanceledOrders = new ArrayList<Order>();
			
			for (long time = now; time > now - totalTimeToReadUserCanceledOrders; time -= intervalToReadUserCanceledOrders) {
				Long since = time - intervalToReadUserCanceledOrders;
				Long end = time - 1;
				
				OrderFilter orderFilter = new OrderFilter(getCoin(), getCurrency());
				orderFilter.setStatus(OrderStatus.CANCELED);
				
				orderFilter.setSince(since);
				orderFilter.setEnd(end);
				
				List<Order> orders = getUserOrders(orderFilter);
				Collections.sort(orders);
				
				for (Order o: orders) {
					MbOrder order = (MbOrder) o;
					if (order.getOperations() != null && order.getOperations().size() > 0)
						userCanceledOrders.add(order);
				}
			}
			Collections.sort(userCanceledOrders);
		}
		else {
			Long since = lastTimeByReadingUserCanceledOrders + 1;
			Long end = now;
			
			OrderFilter orderFilter = new OrderFilter(getCoin(), getCurrency());
			orderFilter.setStatus(OrderStatus.CANCELED);
			
			orderFilter.setSince(since);
			orderFilter.setEnd(end);
			
			List<Order> orders = getUserOrders(orderFilter);
			
			int i = 0;
			for (Order o: orders) {
				MbOrder order = (MbOrder) o;
				if (order.getOperations() != null && order.getOperations().size() > 0) {
					userCanceledOrders.add(i, order);
					i++;
				}
			}
		}
		lastTimeByReadingUserCanceledOrders = now;
		
		return userCanceledOrders;
	}
	
	@Override
	public List<Order> getUserCompletedOrders() throws ApiProviderException {
		long now = (new Date()).getTime() / 1000;
		Long since = now - totalTimeToReadUserCompletedOrders;
		Long end = now;
		
		OrderFilter orderFilter = new OrderFilter(getCoin(), getCurrency());
		orderFilter.setStatus(OrderStatus.COMPLETED);
		if (since != null)
			orderFilter.setSince(since);
		if (end != null)
			orderFilter.setEnd(end);
		
		List<Order> orders = getUserOrders(orderFilter);
		Collections.sort(orders);
		
		return orders;
	}
	
	@Override
	public List<Operation> getUserOperations() throws ApiProviderException {
		List<Operation> operations = new ArrayList<Operation>();
		for (Order o: getUserOrders()) {
			MbOrder order = (MbOrder) o;
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
		
		JsonObject jsonResponse = makeRequest(getParams((MbOrder) order), RequestMethod.CANCEL_ORDER.value);

		String orderId = jsonResponse.names().get(0);
		MbOrder response = getOrder(jsonResponse.get(orderId).asObject());
		response.setOrderId(Long.valueOf(orderId)); 
		return response;
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
	
	public List<Order> getUserOrders() throws ApiProviderException {
		List<Order> orders = new ArrayList<Order>();
		
		orders.addAll(getUserActiveOrders());
		orders.addAll(getUserCompletedOrders());
		orders.addAll(getUserCanceledOrders());
		Collections.sort(orders);
		
		return orders;
	}
	
	public List<Order> getUserOrders(Long since, Long end) throws ApiProviderException {
		OrderFilter filter = new OrderFilter(getCoin(), getCurrency());
		if (since != null)
			filter.setSince(since);
		if (end != null)
			filter.setEnd(end);
		
		JsonObject jsonResponse = makeRequest(getParams(filter), RequestMethod.ORDER_LIST.value);

		List<Order> orders = new ArrayList<Order>();
		for (String id : jsonResponse.names()) {
			JsonObject jsonObject = jsonResponse.get(id).asObject();
			MbOrder order = getOrder(jsonObject);
			order.setOrderId(Long.valueOf(id));
			orders.add(order);
		}
		
		return orders;
	}

	/**
	 * Get a Ticker with informations about trades since midnight.
	 * 
	 * @return Ticker object with the information retrieved.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public MbTicker tickerOfToday() throws ApiProviderException {
		String url = assemblyUrl("v1/ticker");
		JsonObject jsonObject = JsonObject.readFrom(invokeApiMethod(url));
		JsonObject ticketJsonObject = jsonObject.get("ticker").asObject();
		return new MbTicker(ticketJsonObject);
	}
	
	/**
	 * Get the list of executed trades. 
	 * 
	 * @return Trades object with an Array of the operations.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public Operation[] tradeList() throws ApiProviderException {
		return tradeList(getCoin(), getCurrency(), "");
	}

	/**
	 * Get the list of executed trades since the initial timestamp. 
	 * 
	 * @return Trades object with an Array of the operations.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public Operation[] tradeList(long initialTid) throws ApiProviderException {
		if (initialTid < 0) {
			throw new ApiProviderException("Invalid initial tid.");
		}
		return tradeList(getCoin(), getCurrency(), String.valueOf("?tid=" + initialTid));
	}
	
	/**
	 * Get the list of executed trades from the initial timestamp to the final timestamp. 
	 * 
	 * @return Trades object with an Array of the operations.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public Operation[] tradeList(TimestampInterval interval) throws ApiProviderException {
		if (interval == null) {
			throw new ApiProviderException("Invalid date interval.");
		}
		
		List<String> paths = new ArrayList<String>();
		paths.add(interval.getFromTimestamp() + "/");
		
		if (interval.getToTimestamp() != null) {
			paths.add(interval.getToTimestamp() + "/");
		}
		
		return tradeList(paths.toArray(new String[0]));
	}

	private Operation[] tradeList(String ... complements) throws ApiProviderException {
		String url = assemblyUrl("trades", complements);
		String response = invokeApiMethod(url.toString());
		JsonArray jsonArray = JsonArray.readFrom(response);

		//Convert Json response to object
		Operation[] operationList = new Operation[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jsonObject = jsonArray.get(i).asObject();
			MbOperation operation = new MbOperation();
			operation.setCreated(Integer.valueOf(jsonObject.get("date").toString()));
			operation.setCoinAmount(new BigDecimal(jsonObject.get("amount").toString()));
			operation.setCurrencyPrice(new BigDecimal(jsonObject.get("price").toString()));
			operation.setOperationId(jsonObject.get("tid").asLong());
			operation.setSide(
				RecordSide.valueOf(jsonObject.get("side").asString().toUpperCase())
			);

			operation.setRate(null);
			
			operation.setCreatedDate(Calendar.getInstance());
			operation.getCreatedDate().setTimeInMillis((long) operation.getCreated() * 1000);
			
			operationList[i] = operation;
		}
		return operationList;
	}
	
	/*
	 * Assembly url to be invoked based on coin pair and parameters
	 */
	private String assemblyUrl(String method, String ... pathParams) throws ApiProviderException {
		if (getCoin() == null || getCurrency() == null) {
			throw new ApiProviderException("Invalid coin pair.");
		}

		StringBuffer url = new StringBuffer(method);
		
		if (getCoin().equals("LTC")) {
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

	/**
	 * Request a list of the client's orders, with these parameters as optional filters:
	 * - <b>pair</b>: 'btc_brl' or 'ltc_brl'.
	 * - <b>type</b>: 'buy' or 'sell'.
	 * - <b>status</b>: 'active', 'canceled' or 'completed'.
	 * - <b>from_id</b>: Starting ID to list the orders
	 * - <b>end_id</b>: Final ID to list the orders
	 * - <b>since</b>: Starting Unix Time date to list the orders
	 * - <b>end</b>: Final Unix Time date to list the orders
	 * 
	 * @param filter OrderFilter object with the set parameters to make a request to the server.
	 * @return List of Orders requested by the user.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 * @throws NetworkErrorException 
	 */
	
	public List<Order> getUserOrders(OrderFilter filter) throws ApiProviderException {
		if (filter == null) {
			throw new ApiProviderException("Invalid filter.");
		}
		JsonObject jsonResponse = makeRequest(getParams(filter), RequestMethod.ORDER_LIST.value);

		List<Order> orders = new ArrayList<Order>();
		for (String id : jsonResponse.names()) {
			MbOrder order = getOrder(jsonResponse.get(id).asObject());
			order.setOrderId(Long.valueOf(id)); 
			orders.add(order);
		}
		
		return orders;
	}
	
	private Order createOrder(Order order) throws ApiProviderException {
		if (order == null) {
			throw new ApiProviderException("Invalid order.");
		}
		
		MbOrder mbOrder = new MbOrder(
			order.getCoin(), order.getCurrency(), order.getSide(), 
			order.getCoinAmount(), order.getCurrencyPrice()
		);
		
		JsonObject jsonResponse = makeRequest(getParams(mbOrder), RequestMethod.TRADE.value);
		String orderId = jsonResponse.names().get(0);
		MbOrder response = getOrder(jsonResponse.get(orderId).asObject());
		response.setOrderId(Long.valueOf(orderId));
		return response;
	}

	/**
	 * Performs a Bitcoin withdrawal to trusted address.
	 * 
	 * @param bitcoinAddress Bitcoin address that will receive the withdrawal
	 * @param volume Amount that will be withdrawal
	 * @throws NetworkErrorException 
	 */
	public Withdrawal withdrawalBtcBrl(String bitcoinAddress, BigDecimal volume) throws ApiProviderException {
		if (bitcoinAddress == null) {
			throw new ApiProviderException("Invalid Bitcoin address.");
		}

		if (volume == null || volume.compareTo(BigDecimal.ZERO) == -1) {
			throw new ApiProviderException("Invalid volume.");
		}
		
		JsonHashMap params = new JsonHashMap();
		params.put("bitcoin_address", bitcoinAddress);
		params.put("volume", volume.toString());
		
		JsonObject jsonResponse = makeRequest(params, RequestMethod.WITHDRAWAL_BITCOIN.value);
		Withdrawal withdrawal = new Withdrawal(jsonResponse, "BTC", "BRL");
		
		return withdrawal;
	}
	
	public JsonHashMap getParams(MbOrder order) throws ApiProviderException {
		JsonHashMap hashMap = new JsonHashMap();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			
			if (getCoin() != null && getCurrency() != null)
				params.put("pair", getCoin().toLowerCase() + "_" + getCurrency().toLowerCase());
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
			if (order.getOrderId() != null)
				params.put("order_id", order.getOrderId());
			if (order.getStatus() != null)
				params.put("status", order.getStatus());
			if (order.getCreationDate() != null)
				params.put("created", order.getCreationDate().getTime());
			
			hashMap.putAll(params);
		} catch (Throwable e) {
			throw new ApiProviderException("Internal error: Unable to transform the parameters in a request.");
		}
		return hashMap;
	}
	
	public JsonHashMap getParams(OrderFilter filter) throws ApiProviderException {
		JsonHashMap hashMap = new JsonHashMap();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			
			if (filter.getCoin() != null && filter.getCurrency() != null)
				params.put(
					"pair", 
					filter.getCoin().toLowerCase() + "_" 
					+ filter.getCurrency().toLowerCase()
				);
			if (filter.getSide() != null)
				params.put(
					"type", 
					filter.getSide() == RecordSide.BUY? "buy": 
					(filter.getSide() == RecordSide.SELL? "sell": null)
				);
			if (filter.getStatus() != null)
				params.put("status", filter.getStatus().getValue());
			if (filter.getFromId() != null)
				params.put("from_id", filter.getFromId());
			if (filter.getEndId() != null)
				params.put("end_id", filter.getEndId());
			if (filter.getSince() != null)
				params.put("since", filter.getSince());
			if (filter.getEnd() != null)
				params.put("end", filter.getEnd());
			
			hashMap.putAll(params);
		} catch (Throwable e) {
			throw new ApiProviderException("Internal error: Unable to transform the parameters in a request.");
		}
		return hashMap;
	}
	
	public MbOrder getOrder(JsonObject jsonObject) {
		MbOrder order = new MbOrder();
		String coinPair = jsonObject.get("pair").asString().toUpperCase();
		order.setCoin(coinPair.substring(0, 3).toUpperCase());
		order.setCurrency(coinPair.substring(4, 7).toUpperCase());
		order.setSide(RecordSide.valueOf(jsonObject.get("type").asString().toUpperCase()));
		order.setCoinAmount(new BigDecimal(jsonObject.get("volume").asString()));
		order.setCurrencyPrice(new BigDecimal(jsonObject.get("price").asString()));
		order.setStatus(jsonObject.get("status").asString());
		
		List<Operation> operations = new ArrayList<Operation>();
		for (String operationId: jsonObject.get("operations").asObject().names()) {
			if (operationId.matches("-?\\d+(\\.\\d+)?")) {
				MbOperation operation = getOperation(
					jsonObject.get("operations").asObject().get(operationId).asObject()
				);
				operation.setOperationId(Long.valueOf(operationId));
				operations.add(operation);
			}
		}
		order.setOperations(operations);
		
		long created = Integer.valueOf(jsonObject.get("created").asString());
		order.setCreationDate(Calendar.getInstance());
		order.getCreationDate().setTimeInMillis((long)created * 1000);
		
		return order;
	}
	
	public MbOperation getOperation(JsonObject jsonObject) {
		MbOperation operation = new MbOperation();
		if (jsonObject.get("date") != null && !jsonObject.get("date").toString().equals("null")) {
			operation.setCreated(Integer.valueOf(jsonObject.get("date").toString()));
			operation.setCoinAmount(new BigDecimal(jsonObject.get("amount").toString()));
			operation.setCurrencyPrice(new BigDecimal(jsonObject.get("price").toString()));
			operation.setOperationId(jsonObject.get("tid").asLong());
			operation.setSide(
				RecordSide.valueOf(jsonObject.get("side").asString().toUpperCase())
			);
			operation.setRate(null);
			operation.setCreatedDate(Calendar.getInstance());
			operation.getCreatedDate().setTimeInMillis((long) operation.getCreated() * 1000);
		}
		else {
			operation.setCoinAmount(new BigDecimal(jsonObject.get("volume").asString()));
			operation.setCurrencyPrice(new BigDecimal(jsonObject.get("price").asString()));
			operation.setRate(new BigDecimal(jsonObject.get("rate").asString()));
			operation.setCreated(Integer.valueOf(jsonObject.get("created").asString()));
			operation.setRate(null);
			operation.setCreatedDate(Calendar.getInstance());
			operation.getCreatedDate().setTimeInMillis((long) operation.getCreated() * 1000);
		}
		
		return operation;
	}
	
	private JsonObject makeRequest(String method) throws ApiProviderException {
		return makeRequest(new JsonHashMap(), method);
	}
	
	private JsonObject makeRequest(JsonHashMap params, String method) throws ApiProviderException {
		params.put(METHOD_PARAM, method);
		params.put("tonce", generateTonce());

		String jsonResponse = invokeTapiMethod(params);
		
		if (jsonResponse == null) {
			throw new ApiProviderException("Internal error: null response from the server.");
		}
		
		JsonObject jsonObject = JsonObject.readFrom(jsonResponse);
		if (jsonObject.get("success").asInt() == 0) {
			throw new ApiProviderException(jsonObject.get("error").asString());
		}
		
		JsonValue returnData = jsonObject.get("return");
		
		// putting delay time
		try {
			TimeUnit.MILLISECONDS.sleep(1010);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return (returnData == null) ? null : returnData.asObject();
	}
	
	private String invokeTapiMethod(JsonHashMap params) throws ApiProviderException {
		try {
			String jsonParams = params.toUrlEncoded();
			String signature = generateSignature(jsonParams);
			URL url = generateTapiUrl();
			HttpURLConnection conn = getHttpPostConnection(url, signature);
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

	private URL generateTapiUrl() throws MalformedURLException {
		URL url = new URL(getDomainTrade());
		return url;
	}
	
	private HttpURLConnection getHttpPostConnection(URL url, String signature) throws IOException {
		HttpURLConnection conn;
		if (usingHttps()) {
			conn = (HttpsURLConnection) url.openConnection();
		} else {
			conn = (HttpURLConnection) url.openConnection();
		}
		
		conn.setRequestMethod(HttpMethod.POST.name());
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Key", userConfiguration.getKey());
		conn.setRequestProperty("Sign", signature);
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
	
	
	
	
	/**
	 * Setup SSL Context to perform HTTPS communication.
	 * 
	 * @param sctm Selected way to validate certificates
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 * @throws Exception 
	 */
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
	
	protected static final String encodeHexString(byte[] bytes) {
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

	protected static final long generateTonce() {
		long unixTime = System.currentTimeMillis() / 1000L;
		return unixTime;
	}
	
}