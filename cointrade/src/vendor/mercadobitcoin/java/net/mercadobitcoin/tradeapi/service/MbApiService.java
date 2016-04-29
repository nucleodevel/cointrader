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
import java.util.Collections;
import java.util.List;
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
import net.mercadobitcoin.tradeapi.to.Ticker;
import net.mercadobitcoin.tradeapi.to.Withdrawal;
import net.mercadobitcoin.tradeapi.to.MbOrder.OrderStatus;
import net.mercadobitcoin.util.JsonHashMap;
import net.mercadobitcoin.util.TimestampInterval;
import net.trader.api.ApiService;
import net.trader.beans.Balance;
import net.trader.beans.Operation;
import net.trader.beans.Order;
import net.trader.beans.OrderBook;
import net.trader.beans.OrderSide;
import net.trader.exception.ApiProviderException;

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
	
	private byte[] mbTapiCodeBytes;
	private String mbTapiKey;

	/**
	 * Constructor. Main object of API Client, grant access to account information and order handling.
	 * 
	 * @param mbTapiCode Personal code given by Mercado Bitcoin to have access to the Trade API.
	 * @param mbTapiKey Personal key given by Mercado Bitcoin to have access to the Trade API.
	 * @throws ApiProviderException 
	 * @throws Exception 
	 */
	public MbApiService(String mbTapiCode, String mbTapiKey) throws ApiProviderException {
		try {
			if (usingHttps()) {
				setSslContext(SslContextTrustManager.DEFAULT);
			}
		} catch (KeyManagementException e) {
			throw new ApiProviderException("Internal error: Invalid SSL Connection.");
		} catch (NoSuchAlgorithmException e) {
			throw new ApiProviderException("Internal error: Invalid SSL Algorithm.");
		}
		
		if (mbTapiCode == null) {
			throw new ApiProviderException("Null code.");
		}
		
		if (mbTapiKey == null) {
			throw new ApiProviderException("Null key.");
		}
		
		this.mbTapiCodeBytes = mbTapiCode.getBytes();
		this.mbTapiKey = mbTapiKey;
	}
	
	protected final boolean usingHttps() {
		return DOMAIN.toUpperCase().startsWith("HTTPS");
	}
	
	protected String getDomain() {
		return DOMAIN + getApiPath();
	}

	public String getApiPath() {
		return API_PATH;
	}
	
	public String getTapiPath() {
		return TAPI_PATH;
	}
	
	@Override
	public Balance getBalance(String coin, String currency) throws ApiProviderException {
		JsonObject jsonResponse = makeRequest(RequestMethod.GET_INFO.value);
		Balance balance = new MbBalance(jsonResponse, coin, currency);
		return balance;		
	}
	
	@Override
	public OrderBook getOrderBook(String coin, String currency) throws ApiProviderException {
		String url = assemblyUrl(coin, currency, "orderbook");
		JsonObject jsonObject = JsonObject.readFrom(invokeApiMethod(url));
		return new MbOrderBook(jsonObject, coin, currency);
	}
	
	@Override
	public List<Order> getUserActiveOrders(String coin, String currency) throws ApiProviderException {
		OrderFilter orderFilter = new OrderFilter(coin, currency);
		orderFilter.setStatus(OrderStatus.COMPLETED);
			
		List<Order> orders = getUserOrders(orderFilter);
		Collections.sort(orders);
		return orders;
	}
	
	@Override
	public List<Order> getUserCanceledOrders(String coin, String currency, Long since, Long end) throws ApiProviderException {
		OrderFilter orderFilter = new OrderFilter(coin, currency);
		orderFilter.setStatus(OrderStatus.CANCELED);
		
		orderFilter.setSince(since);
		orderFilter.setEnd(end);
		
		List<Order> orders = getUserOrders(orderFilter);
		Collections.sort(orders);
		return orders;
	}
	
	@Override
	public List<Order> getUserCompletedOrders(String coin, String currency, Long since, Long end) throws ApiProviderException {
		OrderFilter orderFilter = new OrderFilter(coin, currency);
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
	public List<Operation> getUserOperations(String coin, String currency, Long since, Long end) throws ApiProviderException {
		List<Operation>operations = new ArrayList<Operation>();
		for (Order o: getUserOrders(coin, currency, since, end)) {
			MbOrder order = (MbOrder) o;
			if (order.getOperations() != null)
				for (MbOperation operation: order.getOperations()) {
					operation.setSide(order.getSide());
					operations.add(operation);
				}
		}
		return operations;
	}
	
	public List<Order> getUserOrders(String coin, String currency, Long since, Long end) throws ApiProviderException {
		OrderFilter filter = new OrderFilter(coin, currency);
		if (since != null)
			filter.setSince(since);
		if (end != null)
			filter.setEnd(end);
		
		JsonObject jsonResponse = makeRequest(filter.toParams(), RequestMethod.ORDER_LIST.value);

		List<Order> orders = new ArrayList<Order>();
		for (String id : jsonResponse.names()) {
			orders.add(new MbOrder(Long.valueOf(id), jsonResponse.get(id).asObject()));
		}
		
		return orders;
	}

	/**
	 * Get a Ticker with informations about trades of the last 24 hours.
	 * 
	 * @param coinPair Pair of coins to be used
	 * @return Ticker object with the information retrieved.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public Ticker ticker24h(String coin, String currency) throws ApiProviderException {
		String url = assemblyUrl(coin, currency, "ticker");
		JsonObject jsonObject = JsonObject.readFrom(invokeApiMethod(url));
		JsonObject ticketJsonObject = jsonObject.get("ticker").asObject();
		return new Ticker(ticketJsonObject);
	}

	/**
	 * Get a Ticker with informations about trades since midnight.
	 * 
	 * @return Ticker object with the information retrieved.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public Ticker tickerOfToday(String coin, String currency) throws ApiProviderException {
		String url = assemblyUrl(coin, currency, "v1/ticker");
		JsonObject jsonObject = JsonObject.readFrom(invokeApiMethod(url));
		JsonObject ticketJsonObject = jsonObject.get("ticker").asObject();
		return new Ticker(ticketJsonObject);
	}
	
	/**
	 * Get the list of executed trades. 
	 * 
	 * @return Trades object with an Array of the operations.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public Operation[] tradeList(String coin, String currency) throws ApiProviderException {
		return tradeList(coin, currency, "");
	}

	/**
	 * Get the list of executed trades since the initial timestamp. 
	 * 
	 * @return Trades object with an Array of the operations.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public Operation[] tradeList(String coin, String currency, long initialTid) throws ApiProviderException {
		if (initialTid < 0) {
			throw new ApiProviderException("Invalid initial tid.");
		}
		return tradeList(coin, currency, String.valueOf("?tid=" + initialTid));
	}
	
	/**
	 * Get the list of executed trades from the initial timestamp to the final timestamp. 
	 * 
	 * @return Trades object with an Array of the operations.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public Operation[] tradeList(String coin, String currency, TimestampInterval interval) throws ApiProviderException {
		if (interval == null) {
			throw new ApiProviderException("Invalid date interval.");
		}
		
		List<String> paths = new ArrayList<String>();
		paths.add(interval.getFromTimestamp() + "/");
		
		if (interval.getToTimestamp() != null) {
			paths.add(interval.getToTimestamp() + "/");
		}
		
		return tradeList(coin, currency, paths.toArray(new String[0]));
	}

	private Operation[] tradeList(String coin, String currency, String ... complements) throws ApiProviderException {
		String url = assemblyUrl(coin, currency, "trades", complements);
		String response = invokeApiMethod(url.toString());
		JsonArray jsonObject = JsonArray.readFrom(response);

		//Convert Json response to object
		Operation[] operationList = new Operation[jsonObject.size()];
		for (int i = 0; i < jsonObject.size(); i++) {
			operationList[i] = new MbOperation(jsonObject.get(i).asObject());
		}
		return operationList;
	}
	
	/*
	 * Assembly url to be invoked based on coin pair and parameters
	 */
	private String assemblyUrl(String coin, String currency, String method, String ... pathParams) throws ApiProviderException {
		if (coin == null || currency == null) {
			throw new ApiProviderException("Invalid coin pair.");
		}

		StringBuffer url = new StringBuffer(method);
		
		if (coin.equals("LTC")) {
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
		JsonObject jsonResponse = makeRequest(filter.toParams(), RequestMethod.ORDER_LIST.value);

		List<Order> orders = new ArrayList<Order>();
		for (String id : jsonResponse.names()) {
			orders.add(new MbOrder(Long.valueOf(id), jsonResponse.get(id).asObject()));
		}
		
		return orders;
	}
	
	private Order createOrder(Order order) throws ApiProviderException {
		if (order == null) {
			throw new ApiProviderException("Invalid order.");
		}
		
		JsonObject jsonResponse = makeRequest(((MbOrder)order).toParams(), RequestMethod.TRADE.value);
		String orderId = jsonResponse.names().get(0);
		Order response = new MbOrder(Long.valueOf(orderId), jsonResponse.get(orderId).asObject());
		return response;
	}
	
	/**
	 * Create a Buy Order, with the details of it set in the parameters.
	 * 
	 * @param coin The Coin to be bought,'btc_brl' or 'ltc_brl'.
	 * @param volume The volume of the Coin to be bought.
	 * @param price Define the price for the order.
	 * @return The information about the new Order.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 * @throws NetworkErrorException 
	 */
	public Order createBuyOrder(String coin, String currency, BigDecimal coinAmount, BigDecimal currencyPrice) throws ApiProviderException {
		OrderSide side = OrderSide.BUY;
		Order order = new MbOrder(coin, currency, side, coinAmount, currencyPrice);
		return createOrder(order);
		
	}
	
	/**
	 * Create a Sell Order, with the details of it set in the parameters.
	 * 
	 * @param coin The Coin to be sold,'btc_brl' or 'ltc_brl'.
	 * @param volume The volume of the Coin to be sold.
	 * @param price Define the price for the order.
	 * @return The information about the new Order.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 * @throws NetworkErrorException 
	 */
	public Order createSellOrder(String coin, String currency, BigDecimal coinAmount, BigDecimal currencyPrice) throws ApiProviderException {
		OrderSide side = OrderSide.SELL;
		Order order = new MbOrder(coin, currency, side, coinAmount, currencyPrice);
		return createOrder(order);
		
	}
	
	/**
	 * Cancel the Order sent as parameter, defined by the OrderId and the Coin Pair.
	 * 
	 * @param order Order object with the OrderId and pair defined.
	 * @return Order that was canceled.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 * @throws NetworkErrorException 
	 */
	public Order cancelOrder(Order order) throws ApiProviderException {
		if (order == null) {
			throw new ApiProviderException("Invalid filter.");
		}
		
		JsonObject jsonResponse = makeRequest(((MbOrder) order).toParams(), RequestMethod.CANCEL_ORDER.value);

		String orderId = jsonResponse.names().get(0);
		Order response = new MbOrder(Long.valueOf(orderId), jsonResponse.get(orderId).asObject());
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
		URL url = new URL(getDomain());
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
		conn.setRequestProperty("Key", mbTapiKey);
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