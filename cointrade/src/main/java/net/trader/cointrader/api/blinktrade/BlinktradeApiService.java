package net.trader.cointrader.api.blinktrade;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.trader.cointrader.api.ApiService;
import net.trader.cointrader.beans.Balance;
import net.trader.cointrader.beans.Broker;
import net.trader.cointrader.beans.Coin;
import net.trader.cointrader.beans.Currency;
import net.trader.cointrader.beans.Operation;
import net.trader.cointrader.beans.Order;
import net.trader.cointrader.beans.OrderBook;
import net.trader.cointrader.beans.OrderStatus;
import net.trader.cointrader.beans.OrderType;
import net.trader.cointrader.beans.RecordSide;
import net.trader.cointrader.beans.Ticker;
import net.trader.cointrader.beans.UserConfiguration;
import net.trader.cointrader.exception.ApiProviderException;
import net.trader.cointrader.utils.Utils;

public class BlinktradeApiService extends ApiService {
	
	// --------------------- Constructor
	
	public BlinktradeApiService(UserConfiguration userConfiguration)
			throws ApiProviderException {
		super(userConfiguration);
	}
	
	// --------------------- Getters and setters
	
	@Override
	protected String getDomain() {
		return "https://api.blinktrade.com";
	}
	
	@Override
	protected  String getPublicApiUrl() {
		return getDomain() + getPublicApiPath();
	}
	
	@Override
	protected  String getPrivateApiUrl() {
		return getDomain() + getPrivateApiPath();
	}
	
	@Override
	protected  String getPublicApiPath() {
		return "/api/v1/" + getCurrency().getValue().toUpperCase() + "/";
	}
	
	@Override
	protected  String getPrivateApiPath() {
		return "/tapi/v1/message";
	}
	
	@Override
	public TimeZone getTimeZone() {
		return TimeZone.getTimeZone("GMT-03:00");
	}

	@Override
	protected  void makeActionInConstructor() throws ApiProviderException {
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
	
	// --------------------- Overrided methods
	
	@Override
	public Ticker getTicker() throws ApiProviderException {
		/*Ticker ticker = new Ticker(getCoin(), getCurrency());
		
		BigDecimal high = new BigDecimal(0);
		BigDecimal low = new BigDecimal(Double.MAX_VALUE);
		BigDecimal vol = new BigDecimal(0);
		
		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		
		from.setTime(new Date());
		from.add(Calendar.HOUR, -24);
		to.setTime(new Date());
		
		List<Operation> operations = getOperationList(from, to);
		
		for (Operation operation: operations) {
			vol = vol.add(operation.getCoinAmount());
			if (operation.getCurrencyPrice().compareTo(high) == 1)
				high = operation.getCurrencyPrice();
			if (operation.getCurrencyPrice().compareTo(low) == -1)
				low = operation.getCurrencyPrice();
		}
		
		System.out.println(
			operations.get(0).getCreationDate().getTime() + "-" + operations.get(operations.size() - 1).getCreationDate().getTime()
		);
		
		ticker.setHigh(high);
		ticker.setLow(low);
		ticker.setVol(vol);

		from.setTime(new Date());
		from.add(Calendar.HOUR, -3);
		to.setTime(new Date());
		BigDecimal last3HourVolume = new BigDecimal(0);
		List<Operation> last3HourOperations = getOperationList(from, to);
		
		for (Operation operation: last3HourOperations) 
			last3HourVolume = last3HourVolume.add(operation.getCoinAmount());
		
		ticker.setLast3HourVolume(last3HourVolume);
		
		return ticker;*/
		return null;
	}

	@Override
	public Balance getBalance() throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "U2");
		request.put("BalanceReqID", new Integer((int)(System.currentTimeMillis()/1000)));

		String response = makePrivateRequest(GSON.toJson(request));
		
		JsonParser jsonParser = new JsonParser();
        JsonObject balanceJsonObject = (JsonObject)jsonParser.parse(response);
        
        return getBalance(balanceJsonObject);
	}

	@Override
	public OrderBook getOrderBook() throws ApiProviderException {

		String responseMessage = makePublicRequest("orderbook");
		
		JsonParser jsonParser = new JsonParser();
        JsonObject orderBookJsonObject = (JsonObject)jsonParser.parse(responseMessage);
        
        return getOrderBook(orderBookJsonObject);
	}
	
	@Override
	public List<Operation> getOperationList(Calendar from, Calendar to) throws ApiProviderException {
		String responseMessage = makePublicRequest("trades");
		
		JsonParser jsonParser = new JsonParser();
		JsonElement operationListJsonObject = (JsonElement)jsonParser.parse(responseMessage);
		JsonArray operationListJsonArray = operationListJsonObject.getAsJsonArray();
		
		Operation[] operationList = new Operation[operationListJsonArray.size()];
		for (int i = 0; i < operationListJsonArray.size(); i++) {
			JsonObject jsonObject = operationListJsonArray.get(i).getAsJsonObject();
			
			long created = Integer.valueOf(jsonObject.get("date").toString());
			BigDecimal coinAmount = new BigDecimal(jsonObject.get("amount").toString());
			BigDecimal currencyPrice = new BigDecimal(jsonObject.get("price").toString());
			
			String sideString = jsonObject.get("side").getAsString();
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
		
		List<Operation> operations = Arrays.asList(operationList);
		
		return operations;
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
			throws ApiProviderException {
		URL url = null;
		URLConnection http = null;

		try {
			url = new URL(getPublicApiUrl() + requestUrl);
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
			responseMessage = IOUtils.toString(is, "UTF-8");
		} catch (Exception e) {
			throw new ApiProviderException("API response retrieve fail", e);
		}
		
		// putting delay time
		try {
			TimeUnit.MILLISECONDS.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
			url = new URL(getPrivateApiUrl());
			http = url.openConnection();

		} catch (Exception e) {
			throw new ApiProviderException("API URL initialization fail", e);
		}

		try {
			Method setRequestMethod = http.getClass().getMethod(
				"setRequestMethod", String.class
			);
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
			responseMessage = IOUtils.toString(is, "UTF-8");
		} catch (Exception e) {
			throw new ApiProviderException("API response retrieve fail", e);
		}
		

		// putting delay time
		try {
			TimeUnit.MILLISECONDS.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return responseMessage;

	}
	
	// --------------------- Json to object
	
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
	
	// --------------------- Custom
	
	private static final long SATOSHI_BASE = 100000000;
	
	private static final Gson GSON = new Gson();
	
	private String getBrokerId() {
		if (userConfiguration.getBroker() == Broker.FOXBIT)
			return "4";
		return null;
	}

}
