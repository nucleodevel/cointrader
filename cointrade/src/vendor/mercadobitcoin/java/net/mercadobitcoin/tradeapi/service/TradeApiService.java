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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import net.mercadobitcoin.tradeapi.to.MbBalance;
import net.mercadobitcoin.tradeapi.to.MbOrder;
import net.mercadobitcoin.tradeapi.to.MbOrder.CoinPair;
import net.mercadobitcoin.tradeapi.to.OrderFilter;
import net.mercadobitcoin.tradeapi.to.Withdrawal;
import net.mercadobitcoin.util.JsonHashMap;
import net.trader.beans.Balance;
import net.trader.beans.Order;
import net.trader.beans.OrderSide;
import net.trader.exception.ApiProviderException;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Client service to communicate with Mercado Bitcoin Trade API.
 * Used to trade in Mercado Bitcoin.
 */
public class TradeApiService extends AbstractApiService {

	/* CONSTANTS */
	private static final String TAPI_PATH = "/tapi/";
	private static final String ENCRYPT_ALGORITHM = "HmacSHA512";
	private static final String METHOD_PARAM = "method";

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

	/* ATTRIBUTES */
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
	public TradeApiService(String mbTapiCode, String mbTapiKey) throws ApiProviderException {
		super();
		
		if (mbTapiCode == null) {
			throw new ApiProviderException("Null code.");
		}
		
		if (mbTapiKey == null) {
			throw new ApiProviderException("Null key.");
		}
		
		this.mbTapiCodeBytes = mbTapiCode.getBytes();
		this.mbTapiKey = mbTapiKey;
	}
	
	public String getApiPath() {
		return TAPI_PATH;
	}
	
	/**
	 * Retrieve information about user's account:
	 * - Balances in Brazilian Real, Bitcoin and Litecoin;
	 * - Number of open orders.
	 * Also 'server time' to ease API sync and avoid Time Zone misinterpretation.
	 * 
	 * @return An AccountBalance object containing the user's account information.
	 * @throws ApiProviderException Generic exception to point the cause of any possible problem with the execution.
	 * @throws NetworkErrorException 
	 */
	public Balance getBalance(String currency, String coin) throws ApiProviderException {
		JsonObject jsonResponse = makeRequest(RequestMethod.GET_INFO.value);
		Balance balance = new MbBalance(jsonResponse, currency, coin);
		return balance;		
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
	public List<Order> listOrders(OrderFilter filter) throws ApiProviderException {
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
	public Order createBuyOrder(CoinPair coin, BigDecimal coinAmount, BigDecimal currencyPrice) throws ApiProviderException {
		OrderSide side = OrderSide.BUY;
		Order order = new MbOrder(coin, side, coinAmount, currencyPrice);
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
	public Order createSellOrder(CoinPair coin, BigDecimal coinAmount, BigDecimal currencyPrice) throws ApiProviderException {
		OrderSide side = OrderSide.SELL;
		Order order = new MbOrder(coin, side, coinAmount, currencyPrice);
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
	public Withdrawal withdrawalBitcoin(String bitcoinAddress, BigDecimal volume) throws ApiProviderException {
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
		Withdrawal withdrawal = new Withdrawal(jsonResponse, CoinPair.BTC_BRL);
		
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
	
}