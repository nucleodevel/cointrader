/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package org.nucleodev.cointrader.api.poloniex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.nucleodev.cointrader.api.ApiService;
import org.nucleodev.cointrader.beans.Balance;
import org.nucleodev.cointrader.beans.Coin;
import org.nucleodev.cointrader.beans.Currency;
import org.nucleodev.cointrader.beans.Operation;
import org.nucleodev.cointrader.beans.Order;
import org.nucleodev.cointrader.beans.OrderBook;
import org.nucleodev.cointrader.beans.OrderStatus;
import org.nucleodev.cointrader.beans.RecordSide;
import org.nucleodev.cointrader.beans.Ticker;
import org.nucleodev.cointrader.beans.UserConfiguration;
import org.nucleodev.cointrader.exception.ApiProviderException;
import org.nucleodev.cointrader.utils.JsonHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PoloniexApiService extends ApiService {
	
	// --------------------- Constructor
	
	public PoloniexApiService(UserConfiguration userConfiguration)
			throws ApiProviderException {
		super(userConfiguration);
		makeActionInConstructor();
	}
	
	// --------------------- Getters and setters
	
	@Override
	protected  String getDomain() {
		return "https://poloniex.com";
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
		return "/public";
	}
	
	@Override
	protected  String getPrivateApiPath() {
		return "/tradingApi";
	}
	
	@Override
	public TimeZone getTimeZone() {
		return TimeZone.getTimeZone("GMT");
	}
	
	@Override
	protected  void makeActionInConstructor() throws ApiProviderException {
		this.mbTapiCodeBytes = userConfiguration.getSecret().getBytes();
	}
	
	// --------------------- Overrided methods
	
	@Override
	public Ticker getTicker() throws ApiProviderException {
		JsonObject tickerJsonObject = (JsonObject) makePublicRequest("returnTicker", new JsonHashMap());
		
		JsonObject jsonObject = tickerJsonObject.getAsJsonObject(
			getCurrency().getValue() + "_" + getCoin().getValue() 
		);
		
		Ticker ticker = new Ticker(getCoin(), getCurrency());
		
		ticker.setVol(jsonObject.getAsJsonPrimitive("quoteVolume").getAsBigDecimal());
		ticker.setHigh(jsonObject.getAsJsonPrimitive("highestBid").getAsBigDecimal());
		ticker.setLow(jsonObject.getAsJsonPrimitive("lowestAsk").getAsBigDecimal());
		
		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();

		from.setTime(new Date());
		from.add(Calendar.HOUR, -3);
		to.setTime(new Date());
		BigDecimal last3HourVolume = new BigDecimal(0);
		List<Operation> last3HourOperations = getOperationList(from, to);
		
		for (Operation operation: last3HourOperations) 
			last3HourVolume = last3HourVolume.add(operation.getCoinAmount());
		
		ticker.setLast3HourVolume(last3HourVolume);
		
		return ticker;
	}
	
	@Override
	public Balance getBalance() throws ApiProviderException {
		JsonHashMap args = new JsonHashMap();
		args.put("command", "returnCompleteBalances");
		
		JsonObject balanceJsonObject = (JsonObject) makePrivateRequest("returnBalances", args);
		return getBalance(balanceJsonObject);	
	}
	
	@Override
	public OrderBook getOrderBook() throws ApiProviderException {
		JsonObject orderBookJsonObject = (JsonObject) makePublicRequest("returnOrderBook", new JsonHashMap()); 
		return getOrderBook(orderBookJsonObject);
	}
	
	@Override
	public List<Operation> getOperationList(Calendar from, Calendar to) throws ApiProviderException {
		JsonHashMap args = new JsonHashMap();
		args.put("start", (Long) (from.getTimeInMillis() / 1000));
		args.put("end", (Long) (to.getTimeInMillis() / 1000));
		
		JsonArray jsonArray = makePublicRequest("returnTradeHistory", args).getAsJsonArray();
		
		//Convert Json response to object
		Operation[] operationList = new Operation[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
			
			BigDecimal coinAmount = new BigDecimal(jsonObject.get("amount").getAsDouble());
			BigDecimal currencyPrice = new BigDecimal(jsonObject.get("rate").getAsDouble());
			
			String sideString = jsonObject.get("type").getAsString();
			RecordSide side = sideString.equals("buy")? RecordSide.BUY: 
				(sideString.equals("sell")? RecordSide.SELL: null);
			
			Operation operation = new Operation(
				getCoin(), getCurrency(), side, coinAmount, currencyPrice
			);

			operation.setId(BigInteger.valueOf(jsonObject.get("tradeID").getAsLong()));
			operation.setRate(null);
			operation.setCreationDate(Calendar.getInstance());
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				operation.getCreationDate().setTime(df.parse(jsonObject.get("date").getAsString()));
			} catch (ParseException e) {
				e.printStackTrace();
			}			

			operationList[i] = operation;
		}
		
		List<Operation> operations = Arrays.asList(operationList);
		
		return operations;
	}
	
	@Override
	public List<Order> getUserActiveOrders() throws ApiProviderException {
		JsonHashMap args = new JsonHashMap();
		args.put("command", "returnOpenOrders");
		args.put("currencyPair", getCurrency() + "_" + getCoin());
		
		JsonArray activeOrdersJsonArray = (JsonArray) makePrivateRequest("returnOpenOrders", args);
		
		List<Order> orders = new ArrayList<Order>();
		for (JsonElement jsonOrder: activeOrdersJsonArray) {
			Order order = getOrder(jsonOrder.getAsJsonObject());
			order.setStatus(OrderStatus.ACTIVE);
			orders.add(order);
		}
		
		return orders;
	}
	
	@Override
	public List<Operation> getUserOperations() throws ApiProviderException {
		JsonHashMap args = new JsonHashMap();
		
		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		from.setTime(new Date());
		from.add(Calendar.HOUR, -48);
		to.setTime(new Date());
		
		args.put("command", "returnTradeHistory");
		args.put("currencyPair", getCurrency() + "_" + getCoin());
		args.put("start", ((Long) (from.getTimeInMillis() / 1000)).toString());
		args.put("end", ((Long) (to.getTimeInMillis() / 1000)).toString());
		
		JsonArray jsonArray = (JsonArray) makePrivateRequest("returnTradeHistory", args);
		
		//Convert Json response to object
		Operation[] operationList = new Operation[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
			
			BigDecimal coinAmount = new BigDecimal(jsonObject.get("amount").getAsDouble());
			BigDecimal currencyPrice = new BigDecimal(jsonObject.get("rate").getAsDouble());
			
			String sideString = jsonObject.get("type").getAsString();
			RecordSide side = sideString.equals("buy")? RecordSide.BUY: 
				(sideString.equals("sell")? RecordSide.SELL: null);
			
			Operation operation = new Operation(
				getCoin(), getCurrency(), side, coinAmount, currencyPrice
			);

			operation.setId(BigInteger.valueOf(jsonObject.get("tradeID").getAsLong()));
			operation.setRate(null);
			operation.setCreationDate(Calendar.getInstance());
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				operation.getCreationDate().setTime(df.parse(jsonObject.get("date").getAsString()));
			} catch (ParseException e) {
				e.printStackTrace();
			}			

			operationList[i] = operation;
		}
		
		List<Operation> operations = Arrays.asList(operationList);
		
		return operations;
	}
	
	@Override
	public Order cancelOrder(Order order) throws ApiProviderException {
		if (order == null) {
			throw new ApiProviderException("Invalid order.");
		}
		
		JsonHashMap args = new JsonHashMap();
		args.put("command", "cancelOrder");
		args.put("currencyPair", getCurrency() + "_" + getCoin());
		args.put("orderNumber", order.getId().toString());
		
		makePrivateRequest("cancelOrder", args);
		
		order.setStatus(OrderStatus.CANCELED);
		return order;
	}
	
	@Override
	public Order createOrder(Order order) throws ApiProviderException {
		if (order == null) {
			throw new ApiProviderException("Invalid order.");
		}
		
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(8);
		
		DecimalFormatSymbols symbols = decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
		JsonHashMap args = new JsonHashMap();
		String command = order.getSide().getValue().toLowerCase();
		
		args.put("command", command);
		args.put("currencyPair", getCurrency() + "_" + getCoin());
		args.put("rate", decFmt.format(order.getCurrencyPrice()));
		args.put("amount", decFmt.format(order.getCoinAmount()));
		
		
		makePrivateRequest(command, args);
		
		return order;
	}
	
	// --------------------- Request methods
	
	private JsonElement makePublicRequest(String method, JsonHashMap args) throws ApiProviderException {
		if (getCoin() == null || getCurrency() == null) {
			throw new ApiProviderException("Invalid coin pair.");
		}

		try {
			// add method and nonce to args
			if (args == null) {
				args = new JsonHashMap();
			}
			
			String argsVar = "";
			for (Map.Entry<String, Object> arg: args.entrySet())
				argsVar += "&" + arg.getKey() + "=" + arg.getValue();
			
			URL urlVar = new URL(
				getPublicApiUrl() + "?command=" + method + "&currencyPair=" 
				+ getCurrency() + "_" + getCoin() + argsVar
			);
			
			HttpsURLConnection conn = (HttpsURLConnection) urlVar.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			
			conn.getResponseCode();
			
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

			String response = sb.toString();
			JsonParser jsonParser = new JsonParser();
			JsonElement jsonElement = jsonParser.parse(response);
			
			// putting delay time
			try {
				TimeUnit.MILLISECONDS.sleep(1010);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			return jsonElement;
		} catch (MalformedURLException e) {
			throw new ApiProviderException("Internal error: Invalid URL.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new ApiProviderException("Internal error: Failure in connection.");
		}
	}
	
	@SuppressWarnings("deprecation")
	private JsonElement makePrivateRequest(String method, JsonHashMap args) 
		throws ApiProviderException {
		
		try {
			setAuthKeys();
		} catch (Exception e1) {
			throw new ApiProviderException();
		}
		if (!initialized) {
			throw new ApiProviderException();
		}
		
		// prep the call
		preAuth() ;

		// add method and nonce to args
		if (args == null) {
			args = new JsonHashMap();
		}
		long nonce = System.currentTimeMillis()*1000;
		args.put("method", method) ;
		args.put("nonce",Long.toString(nonce)) ;
		

		// create url form encoded post data
		String postData = "" ;
		for (Iterator<String> iter = args.keySet().iterator(); iter.hasNext();) {
			String arg = iter.next() ;
			if (postData.length() > 0) {
				postData += "&" ;
			}
			postData += arg + "=" + URLEncoder.encode((String)args.get(arg)) ;
		}

		// create connection
		URLConnection conn = null ;
		StringBuffer response = new StringBuffer() ;
		try {
			URL url = new URL(getPrivateApiUrl());
			conn = url.openConnection() ;
			conn.setUseCaches(false) ;
			conn.setDoOutput(true) ;
			
			conn.setRequestProperty("Key", userConfiguration.getKey()) ;
			conn.setRequestProperty("Sign",toHex(mac.doFinal(postData.getBytes("UTF-8")))) ;
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded") ;
			conn.setRequestProperty("User-Agent",USER_AGENT) ;

			// write post data
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			out.write(postData) ;
			out.close() ;

			// read response
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = null ;
			while ((line = in.readLine()) != null)
				response.append(line) ;
			in.close() ;
		} catch (MalformedURLException e) {
			throw new ApiProviderException("Internal error.",e) ;
		} catch (IOException e) {
			throw new ApiProviderException("Error connecting to BTC-E.",e) ;
		}
		
		JsonParser jsonParser = new JsonParser();
		JsonElement jsonElement = jsonParser.parse(response.toString());
		
		// putting delay time
		try {
			TimeUnit.MILLISECONDS.sleep(1010);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return jsonElement;
	}
	
	// --------------------- Object to json
	
	// --------------------- Json to object
	
	private Balance getBalance(JsonObject balanceJsonObject) {
		Balance balance = new Balance(getCoin(), getCurrency());
		
		JsonObject coinJsonObject = balanceJsonObject.getAsJsonObject(getCoin().getValue());
		BigDecimal coinAvailable = new BigDecimal(coinJsonObject.getAsJsonPrimitive("available").getAsString());
		BigDecimal coinLocked = new BigDecimal(coinJsonObject.getAsJsonPrimitive("onOrders").getAsString());
		BigDecimal coinAmount = coinAvailable.add(coinLocked);
		
		JsonObject currencyJsonObject = balanceJsonObject.getAsJsonObject(getCurrency().getValue());
		BigDecimal currencyAvailable = new BigDecimal(currencyJsonObject.getAsJsonPrimitive("available").getAsString());
		BigDecimal currencyLocked = new BigDecimal(currencyJsonObject.getAsJsonPrimitive("onOrders").getAsString());
		BigDecimal currencyAmount = currencyAvailable.add(currencyLocked);
		
		balance.setCoinAmount(coinAmount);
		balance.setCurrencyAmount(currencyAmount);
		balance.setCoinLocked(coinLocked);
		balance.setCurrencyLocked(currencyLocked);
		
		return balance;
	}
	
	private OrderBook getOrderBook(JsonObject orderBookJsonObject) throws ApiProviderException {
		OrderBook orderBook = new OrderBook(getCoin(), getCurrency());

		JsonArray asking = orderBookJsonObject.getAsJsonArray("asks");
		ArrayList<Order> askOrders = new ArrayList<Order>();
		for (int i = 0; i < asking.size(); i++) {
			JsonArray pairAmount = asking.get(i).getAsJsonArray();
			BigDecimal coinAmount = new BigDecimal(pairAmount.get(1).getAsDouble());
			BigDecimal currencyPrice = new BigDecimal(pairAmount.get(0).getAsDouble());
			Order order = new Order(
				getCoin(), getCurrency(), RecordSide.SELL, coinAmount, currencyPrice
			);
			order.setStatus(OrderStatus.ACTIVE);
			order.setPosition(i + 1);
			askOrders.add(order);
		}
		orderBook.setAskOrders(askOrders);
		
		JsonArray bidding = orderBookJsonObject.get("bids").getAsJsonArray();
		ArrayList<Order> bidOrders = new ArrayList<Order>();
		for (int i = 0; i < bidding.size(); i++) {
			JsonArray pairAmount = bidding.get(i).getAsJsonArray();
			BigDecimal coinAmount = new BigDecimal(pairAmount.get(1).toString());
			BigDecimal currencyPrice = new BigDecimal(pairAmount.get(0).getAsDouble());
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
	
	private Order getOrder(JsonObject jsonObject) {
		Coin coin = getCoin();
		Currency currency = getCurrency();
		String sideString = jsonObject.get("type").getAsString();
		RecordSide side = 
			sideString.equals("buy")? RecordSide.BUY: 
			(sideString.equals("sell")? RecordSide.SELL: null);
		BigDecimal coinAmount = new BigDecimal(jsonObject.get("amount").getAsString());
		BigDecimal currencyPrice = new BigDecimal(jsonObject.get("rate").getAsString());
		
		Order order = new Order(coin, currency, side, coinAmount, currencyPrice);
		order.setId(BigInteger.valueOf(jsonObject.get("orderNumber").getAsLong()));
		
		order.setCreationDate(Calendar.getInstance());
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			order.getCreationDate().setTime(df.parse(jsonObject.get("date").getAsString()));
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		return order;
	}
	
	// --------------------- Auxiliares methods
	
	public void setAuthKeys() throws Exception {
		SecretKeySpec keyspec = null ;
		try {
			keyspec = new SecretKeySpec(userConfiguration.getSecret().getBytes("UTF-8"), "HmacSHA512") ;
		} catch (UnsupportedEncodingException uee) {
			throw new Exception("HMAC-SHA512 doesn't seem to be installed",uee) ;
		}

		try {
			mac = Mac.getInstance("HmacSHA512") ;
		} catch (NoSuchAlgorithmException nsae) {
			throw new Exception("HMAC-SHA512 doesn't seem to be installed",nsae) ;
		}

		try {
			mac.init(keyspec) ;
		} catch (InvalidKeyException ike) {
			throw new Exception("Invalid key for signing request",ike) ;
		}
		initialized = true ;
	}

	@SuppressWarnings("static-access")
	private final void preAuth() {
		long elapsed = System.currentTimeMillis()-auth_last_request ;
		if( elapsed < auth_request_limit ) {
			try {
				Thread.currentThread().sleep(auth_request_limit-elapsed) ;
			} catch (InterruptedException e) {

			}
		}
		auth_last_request = System.currentTimeMillis() ;
	}
	
	private String toHex(byte[] b) throws UnsupportedEncodingException {
	    return String.format("%040x", new BigInteger(1,b));
	}
	
	// --------------------- Constants and attributes

	private static final String USER_AGENT = "Mozilla/5.0 (compatible; BTCE-API/1.0; MSIE 6.0 compatible; +https://github.com/abwaters/btce-api)" ;
	private boolean initialized = false;
	private static long auth_last_request = 0 ;
	private static long auth_request_limit = 1000 ;	// request limit in milliseconds

	@SuppressWarnings("unused")
	private byte[] mbTapiCodeBytes;
	private Mac mac;
	
}