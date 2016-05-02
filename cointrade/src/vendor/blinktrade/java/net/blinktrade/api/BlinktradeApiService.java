package net.blinktrade.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import net.trader.api.ApiService;
import net.trader.beans.Balance;
import net.trader.beans.Broker;
import net.trader.beans.Coin;
import net.trader.beans.Currency;
import net.trader.beans.Operation;
import net.trader.beans.Order;
import net.trader.beans.OrderBook;
import net.trader.beans.OrderType;
import net.trader.beans.RecordSide;
import net.trader.beans.Ticker;
import net.trader.beans.UserConfiguration;
import net.trader.exception.ApiProviderException;
import net.trader.utils.Utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Comprise main Blinktrade API operations. <br/>
 * <br/>
 * 
 * The original python-based code, can be found here: <br/>
 * <br/>
 * 
 * https://gist.github.com/pinhopro/60b1fd213b36d576505e
 * 
 * @author Claudiney Nascimento e Silva (Java translator)
 * 
 * @version 1.0 2015-09-27 Alpha
 * 
 * @since September/2015
 * 
 */
public class BlinktradeApiService extends ApiService {

	private static final String BLINKTRADE_API_PRODUCAO_URL = "https://api.blinktrade.com/tapi/v1/message";
	
	private static final String BLINKTRADE_PUBLIC_API_ORDERBOOK = "https://api.blinktrade.com/api/v1/BRL/orderbook";
	//private static final String BLINKTRADE_PUBLIC_API_TRADES = "https://api.blinktrade.com/api/v1/BRL/trades";
	
	private static final long SATOSHI_BASE = 100000000;
	
	private static final Gson GSON = new Gson();
	
	public BlinktradeApiService(UserConfiguration userConfiguration) throws ApiProviderException {
		
		super(userConfiguration);

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
	
	private String getBrokerId() {
		if (userConfiguration.getBroker() == Broker.FOXBIT)
			return "4";
		return null;
	}
	
	@Override
	public Ticker getTicker() throws ApiProviderException {
		return null;
	}

	@Override
	public Balance getBalance() throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "U2");
		request.put("BalanceReqID", new Integer((int)(System.currentTimeMillis()/1000))); // new Integer(1)

		String response = sendMessage(GSON.toJson(request));
		
		Coin coin = userConfiguration.getCoin();
		Currency currency = userConfiguration.getCurrency();
		
		Balance balance = new Balance(coin, currency);
		JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject)jsonParser.parse(response);
        JsonObject balanceJsonObject = jo.getAsJsonArray("Responses").get(0).getAsJsonObject();
        
        balance.setClientId(balanceJsonObject.getAsJsonPrimitive("ClientID").getAsString());
        
        balance.setCoinAmount(balanceJsonObject.getAsJsonObject("4").
        	getAsJsonPrimitive(coin.getValue()).getAsBigDecimal().
        		divide(new BigDecimal(currency == Currency.BRL? SATOSHI_BASE: 1)));
	    balance.setCoinLocked(balanceJsonObject.getAsJsonObject("4").
	    	getAsJsonPrimitive(coin.getValue() + "_locked").getAsBigDecimal().
	    		divide(new BigDecimal(currency == Currency.BRL? SATOSHI_BASE: 1)));
        
        balance.setCurrencyAmount(balanceJsonObject.getAsJsonObject("4").
        	getAsJsonPrimitive(currency.getValue()).getAsBigDecimal().
        		divide(new BigDecimal(currency == Currency.BRL? SATOSHI_BASE: 1)));
        balance.setCurrencyLocked(balanceJsonObject.getAsJsonObject("4").
        	getAsJsonPrimitive(currency.getValue() + "_locked").getAsBigDecimal().
        		divide(new BigDecimal(currency == Currency.BRL? SATOSHI_BASE: 1)));
        return balance;

	}

	@Override
	public OrderBook getOrderBook() throws ApiProviderException {

		/*
		 * API URL initialzation
		 */

		URL url = null;
		URLConnection http = null;

		try {
			url = new URL(BLINKTRADE_PUBLIC_API_ORDERBOOK);
			http = url.openConnection();
		} catch (Exception e) {
			throw new ApiProviderException("API URL initialization fail", e);
		}

		/*
		 * Required headers initialization
		 */
		http.setRequestProperty("Content-Type", "application/json");
		
		http.setDoInput(true);

		InputStream is = null;

		/*
		 * Retrieve response
		 */
		String responseMessage = null;

		try {
			is = http.getInputStream();
			responseMessage = IOUtils.toString(is);
		} catch (Exception e) {
			throw new ApiProviderException("API response retrieve fail", e);
		}
		
		JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject)jsonParser.parse(responseMessage);
        
        JsonArray bidArray = jo.getAsJsonArray("bids");
        JsonArray askArray = jo.getAsJsonArray("asks");
        
        List<Order> bidOrders = new ArrayList<Order>();
        for (JsonElement row: bidArray) {
        	JsonArray rowArray = row.getAsJsonArray();
        	Order bidOrder = new Order(
        		userConfiguration.getCoin(), userConfiguration.getCurrency(), 
        		RecordSide.BUY, rowArray.get(1).getAsBigDecimal(), 
        		rowArray.get(0).getAsBigDecimal()
        	);
        	bidOrder.setClientId(rowArray.get(2).getAsBigInteger());
        	bidOrders.add(bidOrder);
        }
        
        List<Order> askOrders = new ArrayList<Order>();
        for (JsonElement row: askArray) {
        	JsonArray rowArray = row.getAsJsonArray();
        	Order askOrder = new Order(
        		userConfiguration.getCoin(), userConfiguration.getCurrency(), 
        		RecordSide.SELL, rowArray.get(1).getAsBigDecimal(), 
        		rowArray.get(0).getAsBigDecimal()
        	);
        	askOrder.setClientId(rowArray.get(2).getAsBigInteger());
        	askOrders.add(askOrder);
        }
        
        OrderBook orderBook = new OrderBook(
        	userConfiguration.getCoin(), userConfiguration.getCurrency()
        );
        orderBook.setBidOrders(bidOrders);
        orderBook.setAskOrders(askOrders);
		
		return orderBook;

	}

	@Override
	public List<Order> getUserActiveOrders() throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		List<String> filters = new ArrayList<String>(1);
		filters.add("has_leaves_qty eq 1");

		request.put("MsgType", "U4");
		request.put("OrdersReqID", new Integer((int)(System.currentTimeMillis()/1000)));
		request.put("Page", new Integer(0));
		request.put("PageSize", new Integer(100));
		request.put("Filter", filters);

		String response = sendMessage(GSON.toJson(request));
		
		JsonParser jsonParser = new JsonParser();
		JsonObject jo = (JsonObject) jsonParser.parse(response);
		JsonArray activeOrdListGrp = jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonArray("OrdListGrp");
		List<Order> activeOrders = new ArrayList<Order>();
		if(activeOrdListGrp != null)
			for (JsonElement jsonElement: activeOrdListGrp)
				if (jsonElement != null) {
					JsonArray jsonArray = jsonElement.getAsJsonArray();
					Order order = getOrder(jsonArray);
					activeOrders.add(order);
				}
		return activeOrders;
	}
	
	@Override
	public List<Order> getUserCanceledOrders() throws ApiProviderException {
		return null;
	}

	@Override
	public List<Order> getUserCompletedOrders() throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		List<String> filters = new ArrayList<String>(1);
		filters.add("has_cum_qty eq 1");

		request.put("MsgType", "U4");
		request.put("OrdersReqID", new Integer((int)(System.currentTimeMillis()/1000)));
		request.put("Page", new Integer(0));
		request.put("PageSize", new Integer(100));
		request.put("Filter", filters);

		String response = sendMessage(GSON.toJson(request));
		
		JsonParser jsonParser = new JsonParser();
		JsonObject jo = (JsonObject) jsonParser.parse(response);
		JsonArray completedOrdListGrp = jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonArray("OrdListGrp");
		List<Order> completedOrders = new ArrayList<Order>();
		if(completedOrdListGrp != null)
			for (JsonElement jsonElement: completedOrdListGrp)
				if (jsonElement != null) {
					JsonArray jsonArray = jsonElement.getAsJsonArray();
					Order order = getOrder(jsonArray);
					completedOrders.add(order);
				}
		return completedOrders;
	}
	
	@Override
	public List<Operation> getUserOperations() throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		List<String> filters = new ArrayList<String>(1);
		filters.add("has_cum_qty eq 1");

		request.put("MsgType", "U4");
		request.put("OrdersReqID", new Integer((int)(System.currentTimeMillis()/1000)));
		request.put("Page", new Integer(0));
		request.put("PageSize", new Integer(100));
		request.put("Filter", filters);

		String response = sendMessage(GSON.toJson(request));
		
		JsonParser jsonParser = new JsonParser();
		JsonObject jo = (JsonObject) jsonParser.parse(response);
		JsonArray completedOrdListGrp = jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonArray("OrdListGrp");
		List<Operation> clientOperations = new ArrayList<Operation>();
		if(completedOrdListGrp != null)
			for (JsonElement jsonElement: completedOrdListGrp)
				if (jsonElement != null) {
					JsonArray jsonArray = jsonElement.getAsJsonArray();
					Operation operation = getOperation(jsonArray);
					clientOperations.add(operation);
				}
		return clientOperations;
	}
	
	@Override
	public Order createBuyOrder(Order order) throws ApiProviderException {
		sendNewOrder(
			new Integer((int)(System.currentTimeMillis()/1000)),
			order.getCoin(), order.getCurrency(), RecordSide.BUY,
			OrderType.LIMITED,
			order.getCoinAmount(), order.getCurrencyPrice()
		);
		
		return null;
	}
	
	@Override
	public Order createSellOrder(Order order) throws ApiProviderException {
		sendNewOrder(
			new Integer((int)(System.currentTimeMillis()/1000)),
			order.getCoin(), order.getCurrency(), RecordSide.SELL,
			OrderType.LIMITED,
			order.getCoinAmount(), order.getCurrencyPrice()
		);
		
		return null;
	}
	
	@Override
	public Order cancelOrder(Order order) throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "F");
		request.put("ClOrdID", ((Order) order).getClientId());
		request.put("BrokerID", getBrokerId());

		sendMessage(GSON.toJson(request));
		
		return null;

	}

	public String createCoinAddressForDeposit(Integer depositRequestID)
			throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "U18");
		request.put("DepositReqID", depositRequestID);
		request.put("Currency", userConfiguration.getCoin());
		request.put("BrokerID", getBrokerId());

		return sendMessage(GSON.toJson(request));

	}

	public String sendNewOrder(Integer clientOrderId, Coin coin,
			Currency currency, RecordSide side, OrderType type,
			BigDecimal coinAmount, BigDecimal currencyPrice)
			throws ApiProviderException {

		if (clientOrderId == null) {
			throw new ApiProviderException("ClientOrderID  cannot be null");
		}

		if (coin == null || currency == null) {
			throw new ApiProviderException("Symbol (currency) cannot be null");
		}

		if (side == null) {
			throw new ApiProviderException("Side (buy/sell) cannot be null");
		}

		if (type == null) {
			throw new ApiProviderException(
					"Type (market/limited) cannot be null");
		}

		if (currencyPrice == null) {
			throw new ApiProviderException("Price cannot be null");
		}

		if (coinAmount == null) {
			throw new ApiProviderException("Amount (satoshi) cannot be null");
		}

		coinAmount = coinAmount.multiply(new BigDecimal(SATOSHI_BASE));
		currencyPrice = currencyPrice.multiply(new BigDecimal(currency == Currency.BRL? SATOSHI_BASE + 1: 1));
		
		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "D");
		request.put("ClOrdID", clientOrderId);
		request.put("Symbol", coin.getValue() + currency.getValue());
		request.put("Side", side == RecordSide.BUY? "1": (side == RecordSide.SELL? "2": null));
		request.put("OrdType", type == OrderType.MARKET? "1": (type == OrderType.LIMITED? "2": null));
		request.put("OrderQty", coinAmount.toBigInteger());
		request.put("Price", currencyPrice.toBigInteger());
		request.put("BrokerID", getBrokerId());

		return sendMessage(GSON.toJson(request));

	}

	/**
	 * Cancel a previous order.
	 * 
	 * @param clientOrderId
	 *            Client Order ID. Must be unique.
	 *            
	 * @return JSON message which contains information about order cancelled.
	 * 
	 * @throws ApiProviderException
	 *             Throws an exception if some error occurs.
	 */
	public String cancelOrder(Integer clientOrderId)
			throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "F");
		request.put("ClOrdID", clientOrderId);
		request.put("BrokerID", getBrokerId());

		return sendMessage(GSON.toJson(request));

	}

	/*
	 * Perform API requests.
	 */
	private String sendMessage(String requestMessage)
			throws ApiProviderException {

		/*
		 * Generate unique nonce
		 */
		String nonce = Long.toString(System.currentTimeMillis());

		/*
		 * 'nonce' signature
		 */
		String signature = null;
		try {
			signature = hash(userConfiguration.getSecret(), nonce);
		} catch (Exception e) {
			throw new ApiProviderException("Message signature fail", e);
		}

		/*
		 * API URL initialzation
		 */

		URL url = null;
		URLConnection http = null;

		try {

			url = new URL(BLINKTRADE_API_PRODUCAO_URL);

			http = url.openConnection();

		} catch (Exception e) {
			throw new ApiProviderException("API URL initialization fail", e);
		}

		/*
		 * Prepare HTTP 'POST' requests.
		 */
		try {
			Method setRequestMethod = http.getClass().getMethod(
					"setRequestMethod", String.class);
			setRequestMethod.invoke(http, "POST");
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * Required headers initialization
		 */
		http.setRequestProperty("Content-Type", "application/json");
		http.setRequestProperty("APIKey", userConfiguration.getKey());
		http.setRequestProperty("Nonce", nonce);
		http.setRequestProperty("Signature", signature);

		http.setDoOutput(true);
		http.setDoInput(true);

		OutputStream os = null;
		InputStream is = null;

		/*
		 * Make request calls
		 */

		try {
			os = http.getOutputStream();
			os.write(requestMessage.getBytes());
			os.flush();
		} catch (Exception e) {
			throw new ApiProviderException("API Request fail", e);
		}

		/*
		 * Retrieve response
		 */
		String responseMessage = null;

		try {
			is = http.getInputStream();
			responseMessage = IOUtils.toString(is);
		} catch (Exception e) {
			throw new ApiProviderException("API response retrieve fail", e);
		}

		return responseMessage;

	}

	/*
	 * API Message signatures using HMAC-SHA256.
	 */
	private static String hash(String secret, String message) {

		final String ALGORITHM = "HmacSHA256";

		try {

			Mac sha_HMAC = Mac.getInstance(ALGORITHM);
			SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(),
					ALGORITHM);

			sha_HMAC.init(secret_key);

			byte encoded[] = sha_HMAC.doFinal(message.getBytes());

			String hash = Hex.encodeHexString(encoded);

			return hash;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public Order getOrder(JsonArray jsonArray) {
		Order order = new Order();
		
		order.setClientId(jsonArray.get(0).getAsBigInteger());
		order.setId(jsonArray.get(1).getAsBigInteger());
		BigDecimal cumQty = jsonArray.get(2).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE));
		//order.setOrdStatus(jsonArray.get(3).getAsString());
		BigDecimal leavesQty = jsonArray.get(4).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE));
		/*order.setCxlQty(jsonArray.get(5).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE)));
		order.setAvgPx(jsonArray.get(6).getAsBigDecimal());*/
		order.setCoin(Coin.valueOf(jsonArray.get(7).getAsString().substring(0, 3).toUpperCase()));
		order.setCurrency(Currency.valueOf(jsonArray.get(7).getAsString().substring(3, 6).toUpperCase()));
		String sideString = jsonArray.get(8).getAsString();
		RecordSide side = sideString.equals("1")? RecordSide.BUY:
			(sideString.equals("2")? RecordSide.SELL: null);
		order.setSide(side);
		order.setCreationDate( Utils.getCalendar(jsonArray.get(12).getAsString()));
		/*order.setVolume(jsonArray.get(13).getAsBigDecimal());
		order.setTimeInForce(jsonArray.get(14).getAsString());*/
		order.setCoinAmount(cumQty.add(leavesQty));
		order.setCurrencyPrice(jsonArray.get(11).getAsBigDecimal().divide(
			new BigDecimal(userConfiguration.getCurrency() == Currency.BRL? SATOSHI_BASE: 1))
		);
		
		return order;
	}
	
	public Operation getOperation(JsonArray jsonArray) {
		Operation operation = new Operation();
		
		operation.setClientId(jsonArray.get(0).getAsBigInteger());
		operation.setId(jsonArray.get(1).getAsBigInteger());
		BigDecimal cumQty = jsonArray.get(2).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE));
		//operation.setOrdStatus(jsonArray.get(3).getAsString());
		BigDecimal leavesQty = jsonArray.get(4).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE));
		/*operation.setCxlQty(jsonArray.get(5).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE)));
		operation.setAvgPx(jsonArray.get(6).getAsBigDecimal());*/
		operation.setCoin(Coin.valueOf(jsonArray.get(7).getAsString().substring(0, 3).toUpperCase()));
		operation.setCurrency(Currency.valueOf(jsonArray.get(7).getAsString().substring(3, 6).toUpperCase()));
		String sideString = jsonArray.get(8).getAsString();
		RecordSide side = sideString.equals("1")? RecordSide.BUY:
			(sideString.equals("2")? RecordSide.SELL: null);
		operation.setSide(side);
		operation.setCreationDate( Utils.getCalendar(jsonArray.get(12).getAsString()));
		/*operation.setVolume(jsonArray.get(13).getAsBigDecimal());
		operation.setTimeInForce(jsonArray.get(14).getAsString());*/
		operation.setCoinAmount(cumQty.add(leavesQty));
		operation.setCurrencyPrice(jsonArray.get(11).getAsBigDecimal().divide(
			new BigDecimal(userConfiguration.getCurrency() == Currency.BRL? SATOSHI_BASE: 1))
		);
		
		return operation;
	}

}
