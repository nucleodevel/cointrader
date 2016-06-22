package net.blinktrade.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
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
import net.trader.beans.OrderStatus;
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

public class BlinktradeApiService extends ApiService {
	
	// --------------------- Constants and attributes
	
	private static final String DOMAIN = "https://api.blinktrade.com";
	private static final String API_PATH = "/api/";
	private static final String TAPI_PATH = "/tapi/";

	private static final String BLINKTRADE_API_PRODUCAO_URL = DOMAIN + TAPI_PATH + "v1/message";
	
	private static final String BLINKTRADE_PUBLIC_API_ORDERBOOK = DOMAIN + API_PATH + "v1/BRL/orderbook";
	//private static final String BLINKTRADE_PUBLIC_API_TRADES = "https://api.blinktrade.com/api/v1/BRL/trades";
	
	private static final long SATOSHI_BASE = 100000000;
	
	private static final Gson GSON = new Gson();
	
	// --------------------- Constructors
	
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
	
	// --------------------- Getters and setters
	
	private String getBrokerId() {
		if (userConfiguration.getBroker() == Broker.FOXBIT)
			return "4";
		return null;
	}
	
	// --------------------- Overrided methods
	
	@Override
	public Ticker getTicker() throws ApiProviderException {
		return getTicker(null);
	}

	@Override
	public Balance getBalance() throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "U2");
		request.put("BalanceReqID", new Integer((int)(System.currentTimeMillis()/1000))); // new Integer(1)

		String response = makePrivateRequest(GSON.toJson(request));
		
		JsonParser jsonParser = new JsonParser();
        JsonObject balanceJsonObject = (JsonObject)jsonParser.parse(response);
        
        return getBalance(balanceJsonObject);
	}

	@Override
	public OrderBook getOrderBook() throws ApiProviderException {

		String responseMessage = makePublicRequest(BLINKTRADE_PUBLIC_API_ORDERBOOK);
		
		JsonParser jsonParser = new JsonParser();
        JsonObject orderBookJsonObject = (JsonObject)jsonParser.parse(responseMessage);
        
        return getOrderBook(orderBookJsonObject);
	}
	
	@Override
	public List<Operation> getOperationList(Calendar from, Calendar to) throws ApiProviderException {
		return null;
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

		String response = makePrivateRequest(GSON.toJson(request));
		
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
	public List<Operation> getUserOperations() throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		List<String> filters = new ArrayList<String>(1);
		filters.add("has_cum_qty eq 1");

		request.put("MsgType", "U4");
		request.put("OrdersReqID", new Integer((int)(System.currentTimeMillis()/1000)));
		request.put("Page", new Integer(0));
		request.put("PageSize", new Integer(100));
		request.put("Filter", filters);

		String response = makePrivateRequest(GSON.toJson(request));
		
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
		
		Integer clientOrderId = new Integer((int)(System.currentTimeMillis()/1000));
		Coin coin = order.getCoin();
		Currency currency = order.getCurrency();
		RecordSide side = order.getSide();
		OrderType type = OrderType.LIMITED;
		BigDecimal coinAmount = order.getCoinAmount();
		BigDecimal currencyPrice = order.getCurrencyPrice();

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

		makePrivateRequest(GSON.toJson(request));
		
		return null;
	}
	
	// --------------------- Request methods
	
	private String makePublicRequest(String requestUrl)
			throws ApiProviderException {URL url = null;
		URLConnection http = null;

		try {
			url = new URL(requestUrl);
			http = url.openConnection();
		} catch (Exception e) {
			throw new ApiProviderException("API URL initialization fail", e);
		}

		http.setRequestProperty("Content-Type", "application/json");
		
		http.setDoInput(true);

		InputStream is = null;

		String responseMessage = null;

		try {
			is = http.getInputStream();
			responseMessage = IOUtils.toString(is);
		} catch (Exception e) {
			throw new ApiProviderException("API response retrieve fail", e);
		}
		
		return responseMessage;
	
	}

	private String makePrivateRequest(String requestMessage)
			throws ApiProviderException {

		String nonce = Long.toString(System.currentTimeMillis());

		String signature = null;
		try {
			final String ALGORITHM = "HmacSHA256";

			try {

				Mac sha_HMAC = Mac.getInstance(ALGORITHM);
				SecretKeySpec secret_key = new SecretKeySpec(
					userConfiguration.getSecret().getBytes(), ALGORITHM
				);

				sha_HMAC.init(secret_key);

				byte encoded[] = sha_HMAC.doFinal(nonce.getBytes());

				signature = Hex.encodeHexString(encoded);

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} catch (Exception e) {
			throw new ApiProviderException("Message signature fail", e);
		}

		URL url = null;
		URLConnection http = null;

		try {

			url = new URL(BLINKTRADE_API_PRODUCAO_URL);

			http = url.openConnection();

		} catch (Exception e) {
			throw new ApiProviderException("API URL initialization fail", e);
		}

		try {
			Method setRequestMethod = http.getClass().getMethod(
					"setRequestMethod", String.class);
			setRequestMethod.invoke(http, "POST");
		} catch (Exception e) {
			e.printStackTrace();
		}

		http.setRequestProperty("Content-Type", "application/json");
		http.setRequestProperty("APIKey", userConfiguration.getKey());
		http.setRequestProperty("Nonce", nonce);
		http.setRequestProperty("Signature", signature);

		http.setDoOutput(true);
		http.setDoInput(true);

		OutputStream os = null;
		InputStream is = null;

		try {
			os = http.getOutputStream();
			os.write(requestMessage.getBytes());
			os.flush();
		} catch (Exception e) {
			throw new ApiProviderException("API Request fail", e);
		}

		String responseMessage = null;

		try {
			is = http.getInputStream();
			responseMessage = IOUtils.toString(is);
		} catch (Exception e) {
			throw new ApiProviderException("API response retrieve fail", e);
		}

		return responseMessage;

	}
	
	// --------------------- Json to object
	
	private Ticker getTicker(JsonObject tickerJsonObject) {
		return null;
	}
	
	private Balance getBalance(JsonObject balanceJsonObject) {
		Coin coin = getCoin();
		Currency currency = getCurrency();
		
		Balance balance = new Balance(coin, currency);
		
        balanceJsonObject = balanceJsonObject.getAsJsonArray("Responses").get(0).getAsJsonObject();
        
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
	
	private OrderBook getOrderBook(JsonObject orderBookJsonObject) {
        JsonArray bidArray = orderBookJsonObject.getAsJsonArray("bids");
        JsonArray askArray = orderBookJsonObject.getAsJsonArray("asks");
        
        List<Order> bidOrders = new ArrayList<Order>();
        for (JsonElement row: bidArray) {
        	JsonArray rowArray = row.getAsJsonArray();
        	Order bidOrder = new Order(
        		userConfiguration.getCoin(), userConfiguration.getCurrency(), 
        		RecordSide.BUY, rowArray.get(1).getAsBigDecimal(), 
        		rowArray.get(0).getAsBigDecimal()
        	);
        	bidOrder.setClientId(rowArray.get(2).getAsBigInteger());
			bidOrder.setStatus(OrderStatus.ACTIVE);
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
			askOrder.setStatus(OrderStatus.ACTIVE);
        	askOrders.add(askOrder);
        }
        
        OrderBook orderBook = new OrderBook(
        	userConfiguration.getCoin(), userConfiguration.getCurrency()
        );
        orderBook.setBidOrders(bidOrders);
        orderBook.setAskOrders(askOrders);
		
		return orderBook;
	}
	
	public Order getOrder(JsonArray jsonArray) {
		String sideString = jsonArray.get(8).getAsString();
		RecordSide side = sideString.equals("1")? RecordSide.BUY:
			(sideString.equals("2")? RecordSide.SELL: null);
		BigDecimal cumQty = jsonArray.get(2).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE));
		BigDecimal leavesQty = jsonArray.get(4).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE));
		BigDecimal coinAmount = cumQty.add(leavesQty);
		BigDecimal currencyPrice = jsonArray.get(11).getAsBigDecimal().divide(
			new BigDecimal(userConfiguration.getCurrency() == Currency.BRL? SATOSHI_BASE: 1));
		
		Order order = new Order(getCoin(), getCurrency(), side, coinAmount, currencyPrice);
		
		order.setClientId(jsonArray.get(0).getAsBigInteger());
		order.setId(jsonArray.get(1).getAsBigInteger());
		
		
		order.setCreationDate( Utils.getCalendar(jsonArray.get(12).getAsString()));
		
		return order;
	}
	
	public Operation getOperation(JsonArray jsonArray) {
		String sideString = jsonArray.get(8).getAsString();
		RecordSide side = sideString.equals("1")? RecordSide.BUY:
			(sideString.equals("2")? RecordSide.SELL: null);
		BigDecimal cumQty = jsonArray.get(2).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE));
		BigDecimal leavesQty = jsonArray.get(4).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE));
		BigDecimal coinAmount = cumQty.add(leavesQty);
		BigDecimal currencyPrice = jsonArray.get(11).getAsBigDecimal().divide(
			new BigDecimal(userConfiguration.getCurrency() == Currency.BRL? SATOSHI_BASE: 1)
		);
		
		Operation operation = new Operation(getCoin(), getCurrency(), side, coinAmount, currencyPrice);
		
		operation.setClientId(jsonArray.get(0).getAsBigInteger());
		operation.setId(jsonArray.get(1).getAsBigInteger());
		
		
		operation.setCreationDate( Utils.getCalendar(jsonArray.get(12).getAsString()));
		
		return operation;
	}

}
