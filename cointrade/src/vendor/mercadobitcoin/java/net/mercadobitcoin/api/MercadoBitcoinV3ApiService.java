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

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Public API Client service to communicate with Mercado Bitcoin API.
 * Used to retrieve general information about trades and orders in Mercado Bitcoin.
 */
public class MercadoBitcoinV3ApiService extends ApiService {
	
	protected enum HttpMethod {
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
		WITHDRAWAL_COIN("withdrawal_coin");
		
		public final String value;
		
		private RequestMethod(String requestMethod) {
			this.value = requestMethod;
		}
	}

	private static final String API_PATH = "/api/";
	private static final String TAPI_PATH = "/tapi/v3/";
	private static final String ENCRYPT_ALGORITHM = "HmacSHA512";
	private static final String METHOD_PARAM = "method";
	private static final String DOMAIN = "https://www.mercadobitcoin.net";

	public static final BigDecimal MINIMUM_VOLUME = new BigDecimal(0.01);
	public static final BigDecimal BITCOIN_24H_WITHDRAWAL_LIMIT = new BigDecimal(25);
	public static final int BITCOIN_DEPOSIT_CONFIRMATIONS = 6;
	
	public static final BigDecimal LITECOIN_24H_WITHDRAWAL_LIMIT = new BigDecimal(25);
	public static final int LITECOIN_DEPOSIT_CONFIRMATIONS = 15;

	private static long intervalToReadUserCanceledOrders = 1200;
	private static long totalTimeToReadUserCanceledOrders = 43200;
	private static long lastTimeByReadingUserCanceledOrders = 0;
	private static long totalTimeToReadUserCompletedOrders = 43200;
	
	private static List<Order> userCanceledOrders;
	private byte[] mbTapiCodeBytes;

	public MercadoBitcoinV3ApiService(UserConfiguration userConfiguration) throws ApiProviderException {
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
	
	private Coin getCoin() {
		return userConfiguration.getCoin();
	}
	
	private Currency getCurrency() {
		return userConfiguration.getCurrency();
	}

	@Override
	public Ticker getTicker() throws ApiProviderException {
		String url = assemblyUrl("ticker");
		JsonObject jsonObject = JsonObject.readFrom(invokeApiMethod(url));
		JsonObject ticketJsonObject = jsonObject.get("ticker").asObject();
		return getTicker(ticketJsonObject);
	}
	
	@Override
	public Balance getBalance() throws ApiProviderException {
		JsonObject jsonObject = makeRequest(RequestMethod.GET_ACCOUNT_INFO.value);
		return getBalance(jsonObject);		
	}
	
	@Override
	public OrderBook getOrderBook() throws ApiProviderException {
		String url = assemblyUrl("orderbook");
		JsonObject jsonObject = JsonObject.readFrom(invokeApiMethod(url));
		OrderBook orderBook = new OrderBook(getCoin(), getCurrency());

		JsonArray asking = jsonObject.get("asks").asArray();
		ArrayList<Order> askOrders = new ArrayList<Order>();
		for (int i = 0; i < asking.size(); i++) {
			JsonArray pairAmount = asking.get(i).asArray();
			BigDecimal coinAmount = new BigDecimal(pairAmount.get(1).toString());
			BigDecimal currencyPrice = new BigDecimal(pairAmount.get(0).toString());
			Order order = new Order(
				getCoin(), getCurrency(), RecordSide.SELL, coinAmount, currencyPrice
			);
			askOrders.add(order);
		}
		orderBook.setAskOrders(askOrders);
		
		JsonArray bidding = jsonObject.get("bids").asArray();
		ArrayList<Order> bidOrders = new ArrayList<Order>();
		for (int i = 0; i < bidding.size(); i++) {
			JsonArray pairAmount = bidding.get(i).asArray();
			BigDecimal coinAmount = new BigDecimal(pairAmount.get(1).toString());
			BigDecimal currencyPrice = new BigDecimal(pairAmount.get(0).toString());
			Order order = new Order(
				getCoin(), getCurrency(), RecordSide.BUY, coinAmount, currencyPrice
			);
			bidOrders.add(order);
		}
		orderBook.setBidOrders(bidOrders);
		
		return orderBook;
	}
	
	@Override
	public List<Order> getUserActiveOrders() throws ApiProviderException {
		RecordFilter orderFilter = new RecordFilter(getCoin(), getCurrency());
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
				
				RecordFilter orderFilter = new RecordFilter(getCoin(), getCurrency());
				orderFilter.setStatus(OrderStatus.CANCELED);
				
				orderFilter.setSince(since);
				orderFilter.setEnd(end);
				
				List<Order> orders = getUserOrders(orderFilter);
				Collections.sort(orders);
				
				for (Order o: orders) {
					Order order = (Order) o;
					if (order.getOperations() != null && order.getOperations().size() > 0)
						userCanceledOrders.add(order);
				}
			}
			Collections.sort(userCanceledOrders);
		}
		else {
			Long since = lastTimeByReadingUserCanceledOrders + 1;
			Long end = now;
			
			RecordFilter orderFilter = new RecordFilter(getCoin(), getCurrency());
			orderFilter.setStatus(OrderStatus.CANCELED);
			
			orderFilter.setSince(since);
			orderFilter.setEnd(end);
			
			List<Order> orders = getUserOrders(orderFilter);
			
			int i = 0;
			for (Order o: orders) {
				Order order = (Order) o;
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
		
		RecordFilter orderFilter = new RecordFilter(getCoin(), getCurrency());
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
		
		JsonObject jsonResponse = makeRequest(getParams((Order) order), RequestMethod.CANCEL_ORDER.value);

		String orderId = jsonResponse.names().get(0);
		Order response = getOrder(jsonResponse.get(orderId).asObject());
		response.setId(new BigInteger(orderId)); 
		return response;
	}
	
	@Override
	public Order createOrder(Order order) throws ApiProviderException {
		if (order == null) {
			throw new ApiProviderException("Invalid order.");
		}
		
		Order mbOrder = new Order(
			order.getCoin(), order.getCurrency(), order.getSide(), 
			order.getCoinAmount(), order.getCurrencyPrice()
		);
		
		JsonObject jsonResponse = null;
		if (order.getSide() == RecordSide.BUY)
			jsonResponse = makeRequest(getParams(mbOrder), RequestMethod.PLACE_BUY_ORDER.value);
		if (order.getSide() == RecordSide.SELL)
			jsonResponse = makeRequest(getParams(mbOrder), RequestMethod.PLACE_SELL_ORDER.value);
		String orderId = jsonResponse.names().get(0);
		Order response = getOrder(jsonResponse.get(orderId).asObject());
		response.setId(new BigInteger(orderId));
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
		RecordFilter filter = new RecordFilter(getCoin(), getCurrency());
		if (since != null)
			filter.setSince(since);
		if (end != null)
			filter.setEnd(end);
		
		JsonObject jsonResponse = makeRequest(getParams(filter), RequestMethod.LIST_ORDERS.value);

		List<Order> orders = new ArrayList<Order>();
		for (String id : jsonResponse.names()) {
			JsonObject jsonObject = jsonResponse.get(id).asObject();
			Order order = getOrder(jsonObject);
			order.setId(new BigInteger(id));
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
	public Ticker tickerOfToday() throws ApiProviderException {
		String url = assemblyUrl("v1/ticker");
		JsonObject jsonObject = JsonObject.readFrom(invokeApiMethod(url));
		JsonObject ticketJsonObject = jsonObject.get("ticker").asObject();
		return getTicker(ticketJsonObject);
	}
	
	/**
	 * Get the list of executed trades. 
	 * 
	 * @return Trades object with an Array of the operations.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public Operation[] tradeList() throws ApiProviderException {
		return tradeList(getCoin().getValue(), getCurrency().getValue(), "");
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
		return tradeList(getCoin().getValue(), getCurrency().getValue(), String.valueOf("?tid=" + initialTid));
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
			
			long created = Integer.valueOf(jsonObject.get("date").toString());
			BigDecimal coinAmount = new BigDecimal(jsonObject.get("amount").toString());
			BigDecimal currencyPrice = new BigDecimal(jsonObject.get("price").toString());
			RecordSide side = 
				RecordSide.valueOf(jsonObject.get("side").asString().toUpperCase());
			
			Operation operation = new Operation(
				getCoin(), getCurrency(), side, coinAmount, currencyPrice
			);

			operation.setId(new BigInteger(jsonObject.get("tid").asString()));
			operation.setRate(null);
			operation.setCreationDate(Calendar.getInstance());
			operation.getCreationDate().setTimeInMillis(created * 1000);			

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
	 * @param filter MbOrderFilter object with the set parameters to make a request to the server.
	 * @return List of Orders requested by the user.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 * @throws NetworkErrorException 
	 */
	
	public List<Order> getUserOrders(RecordFilter filter) throws ApiProviderException {
		if (filter == null) {
			throw new ApiProviderException("Invalid filter.");
		}
		JsonObject jsonResponse = makeRequest(getParams(filter), RequestMethod.LIST_ORDERS.value);

		List<Order> orders = new ArrayList<Order>();
		for (String id : jsonResponse.names()) {
			Order order = getOrder(jsonResponse.get(id).asObject());
			order.setId(new BigInteger(id)); 
			orders.add(order);
		}
		
		return orders;
	}

	/**
	 * Performs a Bitcoin withdrawal to trusted address.
	 * 
	 * @param bitcoinAddress Bitcoin address that will receive the withdrawal
	 * @param volume Amount that will be withdrawal
	 * @throws NetworkErrorException 
	 */
	public Withdrawal withdrawalCoinCurrency(String bitcoinAddress, BigDecimal volume) throws ApiProviderException {
		if (bitcoinAddress == null) {
			throw new ApiProviderException("Invalid Bitcoin address.");
		}

		if (volume == null || volume.compareTo(BigDecimal.ZERO) == -1) {
			throw new ApiProviderException("Invalid volume.");
		}
		
		JsonHashMap params = new JsonHashMap();
		params.put("bitcoin_address", bitcoinAddress);
		params.put("volume", volume.toString());
		
		JsonObject jsonResponse = makeRequest(params, RequestMethod.WITHDRAWAL_COIN.value);
		JsonObject jsonWithdrawal = jsonResponse.get("withdrawal").asObject();
		Withdrawal withdrawal = getWithdrawal(jsonWithdrawal);
		
		return withdrawal;
	}
	
	public JsonHashMap getParams(Order order) throws ApiProviderException {
		JsonHashMap hashMap = new JsonHashMap();
		try {
			Map<String, Object> params = new HashMap<String, Object>();

			if (getCoin() != null && getCurrency() != null)
				params.put("coin_pair", getCoin().getValue().toUpperCase() + getCurrency().getValue().toUpperCase());
			if (order.getCoinAmount() != null)
				params.put("quantity", order.getCoinAmount());
			if (order.getCurrencyPrice() != null)
				params.put("limit_price", order.getCurrencyPrice());

			/*if (order.getId() != null)
				params.put("order_id", order.getId());
			if (order.getSide() != null)
				params.put(
					"order_type", 
					order.getSide() == RecordSide.BUY? "1": 
					(order.getSide() == RecordSide.SELL? "2": null)
				);
			if (order.getStatus() != null)
				params.put(
					"status", 
					order.getStatus() == OrderStatus.ACTIVE? "2": 
					(order.getStatus() == OrderStatus.COMPLETED? "3":
					(order.getStatus() == OrderStatus.CANCELED? "4": null))
				);
			if (order.getCreationDate() != null)
				params.put("created", order.getCreationDate().getTime());*/
			
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
					"coin_pair", 
					filter.getCoin().getValue().toUpperCase() 
					+ filter.getCurrency().getValue().toUpperCase()
				);
			if (filter.getSide() != null)
				params.put(
					"order_type", 
					filter.getSide() == RecordSide.BUY? "1": 
					(filter.getSide() == RecordSide.SELL? "2": null)
				);
			if (filter.getStatus() != null)
				params.put(
					"status",
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
	
	public Balance getBalance(JsonObject jsonObject) {
		Balance balance = new Balance(getCoin(), getCurrency());
		
		JsonObject brlJsonObject = jsonObject.get("brl").asObject();
		BigDecimal brlAvailable = new BigDecimal(brlJsonObject.get("available").asString());
		BigDecimal brlTotal = new BigDecimal(brlJsonObject.get("total").asString());
		
		JsonObject btcJsonObject = jsonObject.get("btc").asObject();
		BigDecimal btcAvailable = new BigDecimal(btcJsonObject.get("available").asString());
		BigDecimal btcTotal = new BigDecimal(btcJsonObject.get("total").asString());
		BigDecimal btcAmountOpenOrders = new BigDecimal(btcJsonObject.get("amount_open_orders").asString());
		
		JsonObject ltcJsonObject = jsonObject.get("ltc").asObject();
		BigDecimal ltcAvailable = new BigDecimal(ltcJsonObject.get("available").asString());
		BigDecimal ltcTotal = new BigDecimal(ltcJsonObject.get("total").asString());
		BigDecimal ltcAmountOpenOrders = new BigDecimal(ltcJsonObject.get("amount_open_orders").asString());
		
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
	
	public Ticker getTicker(JsonObject jsonObject) {
		Ticker ticker = new Ticker(getCoin(), getCurrency());
		
		ticker.setHigh(new BigDecimal(jsonObject.get("high").toString()));
		ticker.setLow(new BigDecimal(jsonObject.get("low").toString()));
		ticker.setVol(new BigDecimal(jsonObject.get("vol").toString()));
		ticker.setLast(new BigDecimal(jsonObject.get("last").toString()));
		ticker.setBuy(new BigDecimal(jsonObject.get("buy").toString()));
		ticker.setSell(new BigDecimal(jsonObject.get("sell").toString()));
		ticker.setDate(new BigDecimal(jsonObject.get("date").toString()));
		
		return ticker;
	}
	
	public Order getOrder(JsonObject jsonObject) {
		String coinPair = jsonObject.get("coin_air").asString().toUpperCase();
		Coin coin = Coin.valueOf(coinPair.substring(0, 3).toUpperCase());
		Currency currency = Currency.valueOf(coinPair.substring(3, 6).toUpperCase());
		String sideString = jsonObject.get("order_type").asString().toUpperCase();
		RecordSide side = sideString.equals("1")? RecordSide.BUY: 
			(sideString.equals("2")? RecordSide.SELL: null);
		BigDecimal coinAmount = new BigDecimal(jsonObject.get("quantity").asString());
		BigDecimal currencyPrice = new BigDecimal(jsonObject.get("limit_price").asString());
		
		Order order = new Order(coin, currency, side, coinAmount, currencyPrice);
		order.setId(BigInteger.valueOf(jsonObject.get("order_id").asLong()));
		String statusString = jsonObject.get("status").asString().toUpperCase();
		OrderStatus status = 
			statusString.equals("2")? OrderStatus.ACTIVE: 
			(statusString.equals("3")? OrderStatus.CANCELED:
			(statusString.equals("4")? OrderStatus.COMPLETED: null));
		order.setStatus(status);
		long created = Integer.valueOf(jsonObject.get("created_timestamp").asString());
		order.setCreationDate(Calendar.getInstance());
		order.getCreationDate().setTimeInMillis((long)created * 1000);
		
		List<Operation> operations = new ArrayList<Operation>();
		for (String operationId: jsonObject.get("operations").asObject().names()) {
			if (operationId.matches("-?\\d+(\\.\\d+)?")) {
				Operation operation = getOperation(
					jsonObject.get("operations").asObject().get(operationId).asObject(), side
				);
				operation.setId(new BigInteger(operationId));
				operation.setSide(order.getSide());
				operations.add(operation);
			}
		}
		order.setOperations(operations);
		
		return order;
	}
	
	public Operation getOperation(JsonObject jsonObject, RecordSide side) {
		Operation operation;
		
		long created = Integer.valueOf(jsonObject.get("execution_timestamp").toString());
		BigDecimal coinAmount = new BigDecimal(jsonObject.get("quantity").toString());
		BigDecimal currencyPrice = new BigDecimal(jsonObject.get("price").toString());
		
		operation = new Operation(
			getCoin(), getCurrency(), side, coinAmount, currencyPrice
		);
		
		operation.setId(new BigInteger(jsonObject.get("tid").asString()));
		operation.setRate(new BigDecimal(jsonObject.get("price").asDouble()));
		operation.setCreationDate(Calendar.getInstance());
		operation.getCreationDate().setTimeInMillis(created * 1000);
		
		return operation;
	}
	
	public Withdrawal getWithdrawal(JsonObject jsonObject) {
		Withdrawal withdrawal = new Withdrawal(getCoin(), getCurrency());
		
		withdrawal.setWithdrawalId(Long.valueOf(jsonObject.getString("id", "0")));
		withdrawal.setVolume(new BigDecimal(jsonObject.getString("volume", "0")));
		withdrawal.setStatus(Long.valueOf(jsonObject.getString("status", "0")).intValue());
		withdrawal.setStatusDescrition(jsonObject.getString("status_description", ""));
		withdrawal.setTransaction(jsonObject.getString("transaction", ""));
		withdrawal.setAddress(jsonObject.getString("bitcoin_address", ""));
		withdrawal.setCreated(Long.valueOf(jsonObject.getString("created_timestamp", "0")));
		withdrawal.setUpdated(Long.valueOf(jsonObject.getString("updated_timestamp", "0")));
		
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