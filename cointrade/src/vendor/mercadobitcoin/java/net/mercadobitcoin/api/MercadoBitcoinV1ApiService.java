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

import com.google.gson.JsonParser;

import net.trader.api.ApiService;
import net.trader.beans.Balance;
import net.trader.beans.Coin;
import net.trader.beans.Currency;
import net.trader.beans.RecordFilter;
import net.trader.beans.UserConfiguration;
import net.trader.beans.Withdrawal;
import net.trader.beans.OrderStatus;
import net.trader.beans.Operation;
import net.trader.beans.Order;
import net.trader.beans.OrderBook;
import net.trader.beans.RecordSide;
import net.trader.beans.Ticker;
import net.trader.exception.ApiProviderException;
import net.trader.utils.HostnameVerifierBag;
import net.trader.utils.JsonHashMap;
import net.trader.utils.TimestampInterval;
import net.trader.utils.TrustManagerBag;
import net.trader.utils.TrustManagerBag.SslContextTrustManager;

/**
 * Public API Client service to communicate with Mercado Bitcoin API.
 * Used to retrieve general information about trades and orders in Mercado Bitcoin.
 */
public class MercadoBitcoinV1ApiService {
	
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

	public static final BigDecimal MINIMUM_VOLUME = new BigDecimal(0.01);
	public static final BigDecimal BITCOIN_24H_WITHDRAWAL_LIMIT = new BigDecimal(25);
	public static final int BITCOIN_DEPOSIT_CONFIRMATIONS = 6;
	
	public static final BigDecimal LITECOIN_24H_WITHDRAWAL_LIMIT = new BigDecimal(25);
	public static final int LITECOIN_DEPOSIT_CONFIRMATIONS = 15;
	
	private byte[] mbTapiCodeBytes;
	
	private UserConfiguration userConfiguration;

	public MercadoBitcoinV1ApiService(UserConfiguration userConfiguration) throws ApiProviderException {
		this.userConfiguration = userConfiguration;
		
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
	
	private Coin getCoin() {
		return userConfiguration.getCoin();
	}
	
	private Currency getCurrency() {
		return userConfiguration.getCurrency();
	}
	
	public Order cancelOrder(Order order) throws ApiProviderException {
		if (order == null) {
			throw new ApiProviderException("Invalid filter.");
		}
		
		makeRequest(getParams((Order) order), RequestMethod.CANCEL_ORDER.value);
		return null;
	}
	
	public Order createOrder(Order order) throws ApiProviderException {
		if (order == null) {
			throw new ApiProviderException("Invalid order.");
		}
		
		Order mbOrder = new Order(
			order.getCoin(), order.getCurrency(), order.getSide(), 
			order.getCoinAmount(), order.getCurrencyPrice()
		);
		
		makeRequest(getParams(mbOrder), RequestMethod.TRADE.value);
		return null;
	}
	
	public Order createBuyOrder(Order order) throws ApiProviderException {
		RecordSide side = RecordSide.BUY;
		order.setSide(side);
		return createOrder(order);
	}
	
	public Order createSellOrder(Order order) throws ApiProviderException {
		RecordSide side = RecordSide.SELL;
		order.setSide(side);
		
		return createOrder(order);
	}
	
	/*
	 * Assembly url to be invoked based on coin pair and parameters
	 */
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
	
	public JsonHashMap getParams(Order order) throws ApiProviderException {
		JsonHashMap hashMap = new JsonHashMap();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			
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
			
			hashMap.putAll(params);
		} catch (Throwable e) {
			throw new ApiProviderException("Internal error: Unable to transform the parameters in a request.");
		}
		return hashMap;
	}
	
	public JsonHashMap getParams(RecordFilter filter) throws ApiProviderException {
		JsonHashMap hashMap = new JsonHashMap();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			
			if (filter.getCoin() != null && filter.getCurrency() != null)
				params.put(
					"pair", 
					filter.getCoin().getValue().toLowerCase() + "_" 
					+ filter.getCurrency().getValue().toLowerCase()
				);
			if (filter.getSide() != null)
				params.put(
					"type", 
					filter.getSide() == RecordSide.BUY? "buy": 
					(filter.getSide() == RecordSide.SELL? "sell": null)
				);
			if (filter.getStatus() != null)
				params.put("status", filter.getStatus().getValue().toLowerCase());
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
	
	private com.google.gson.JsonObject makeRequest(String method) throws ApiProviderException {
		return makeRequest(new JsonHashMap(), method);
	}
	
	private com.google.gson.JsonObject makeRequest(JsonHashMap params, String method) throws ApiProviderException {
		params.put(METHOD_PARAM, method);
		params.put("tonce", generateTonce());

		String jsonResponse = invokeTapiMethod(params);
		
		if (jsonResponse == null) {
			throw new ApiProviderException("Internal error: null response from the server.");
		}
		
		JsonParser jsonParser = new JsonParser();
		com.google.gson.JsonObject jsonObject = (com.google.gson.JsonObject)jsonParser.parse(jsonResponse);
		if (jsonObject.get("success").getAsInt() == 0) {
			throw new ApiProviderException(jsonObject.get("error").getAsString());
		}
		
		com.google.gson.JsonObject returnData = jsonObject.getAsJsonObject("return");
		// putting delay time
		try {
			TimeUnit.MILLISECONDS.sleep(1010);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return (returnData == null) ? null : returnData;
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