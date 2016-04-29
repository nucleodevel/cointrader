package br.eti.claudiney.blinktrade.api;

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
import net.trader.beans.Operation;
import net.trader.beans.Order;
import net.trader.beans.OrderSide;
import net.trader.exception.ApiProviderException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import br.eti.claudiney.blinktrade.api.beans.BlinktradeCurrency;
import br.eti.claudiney.blinktrade.api.beans.BtBalance;
import br.eti.claudiney.blinktrade.api.beans.BtOpenOrder;
import br.eti.claudiney.blinktrade.api.beans.BtOperation;
import br.eti.claudiney.blinktrade.api.beans.BtOrderBook;
import br.eti.claudiney.blinktrade.enums.BlinktradeBroker;
import br.eti.claudiney.blinktrade.enums.BlinktradeOrderType;
import br.eti.claudiney.blinktrade.utils.Utils;

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
public class BtApiService extends ApiService {

	private static final String BLINKTRADE_API_PRODUCAO_URL = "https://api.blinktrade.com/tapi/v1/message";
	private static final String BLINKTRADE_API_TESTNET_URL = "https://api.testnet.blinktrade.com/tapi/v1/message";
	
	private static final String BLINKTRADE_PUBLIC_API_ORDERBOOK = "https://api.blinktrade.com/api/v1/BRL/orderbook";
	//private static final String BLINKTRADE_PUBLIC_API_TRADES = "https://api.blinktrade.com/api/v1/BRL/trades";
	
	private static final long SATOSHI_BASE = 100000000;
	
	private String apiKey;
	private String apiSecret;
	private BlinktradeBroker broker;
	
	private static final Gson GSON = new Gson();
	
	/**
	 * Initialize API.
	 * 
	 * @param apiKey
	 *            API KEY GENERATED IN API MODULE.
	 * @param apiSecret
	 *            SECRET KEY GENERATED IN API MODULE.
	 * @param broker
	 *            Broker (exchange) ID.
	 */
	public BtApiService(String apiKey, String apiSecret, BlinktradeBroker broker) 
		throws ApiProviderException {

		if (apiKey == null) {
			throw new ApiProviderException("APIKey cannot be null");
		}

		if (apiSecret == null) {
			throw new ApiProviderException("APISecret cannot be null");
		}

		if (broker == null) {
			throw new ApiProviderException("Broker cannot be null");
		}

		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
		this.broker = broker;

	}

	@Override
	public Balance getBalance(String coin, String currency) throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "U2");
		request.put("BalanceReqID", new Integer((int)(System.currentTimeMillis()/1000))); // new Integer(1)

		String response = sendMessage(GSON.toJson(request));
		
		BtBalance balance = new BtBalance(coin, currency);
		JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject)jsonParser.parse(response);
        
        balance.setClientID(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonPrimitive("ClientID").getAsString());
        balance.setBalanceRequestID(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonPrimitive("BalanceReqID").getAsInt());
        
        balance.setCurrencyAmount(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive(currency).getAsBigDecimal().divide(BlinktradeCurrency.getCurrencyBySimbol(currency).getRate()));
        balance.setCurrencyLocked(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive(currency + "_locked").getAsBigDecimal().divide(BlinktradeCurrency.getCurrencyBySimbol(currency).getRate()));
        balance.setBtcAmount(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive("BTC").getAsBigDecimal().divide(BlinktradeCurrency.getCurrencyBySimbol(currency).getRate()));
        balance.setBtcLocked(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive("BTC_locked").getAsBigDecimal().divide(BlinktradeCurrency.getCurrencyBySimbol(currency).getRate()));
        return balance;

	}

	@Override
	public BtOrderBook getOrderBook(String coin, String currency) throws ApiProviderException {

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

		return GSON.fromJson(responseMessage, BtOrderBook.class);

	}

	@Override
	public List<Order> getUserActiveOrders(String coin, String currency) throws ApiProviderException {

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
		JsonArray openOrdListGrp = jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonArray("OrdListGrp");
		List<Order> activeOrders = new ArrayList<Order>();
		if(openOrdListGrp != null) {
			for (JsonElement o: openOrdListGrp) {
				if (o != null) {
					BtOpenOrder oo = new BtOpenOrder();
					activeOrders.add(oo);
					JsonArray objArray = o.getAsJsonArray();
					oo.setClientCustomOrderID(objArray.get(0).getAsBigInteger());
					oo.setOrderID(objArray.get(1).getAsString());
					oo.setCumQty(objArray.get(2).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE)));
					oo.setOrdStatus(objArray.get(3).getAsString());
					oo.setLeavesQty(objArray.get(4).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE)));
					oo.setCxlQty(objArray.get(5).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE)));
					oo.setAvgPx(objArray.get(6).getAsBigDecimal());
					oo.setCoin(objArray.get(7).getAsString().substring(0, 3).toUpperCase());
					oo.setCurrency(objArray.get(7).getAsString().substring(3, 6).toUpperCase());
					String sideString = objArray.get(8).getAsString();
					OrderSide side = sideString.equals("1")? OrderSide.BUY:
						(sideString.equals("2")? OrderSide.SELL: null);
					oo.setSide(side);
					oo.setOrdType(objArray.get(9).getAsString());
					oo.setOrderQty(objArray.get(10).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE)));
					oo.setCurrencyPrice(objArray.get(11).getAsBigDecimal().divide(
						BlinktradeCurrency.getCurrencyBySimbol(oo.getCurrency()).getRate()
					));
					oo.setCreationDate( Utils.getCalendar(objArray.get(12).getAsString()));
					oo.setVolume(objArray.get(13).getAsBigDecimal());
					oo.setTimeInForce(objArray.get(14).getAsString());
					oo.setCoinAmount(oo.getCumQty().add(oo.getLeavesQty()));
				}
			}
		}
		return activeOrders;
	}
	
	@Override
	public List<Order> getUserCanceledOrders(String coin, String currency, Long since, Long end) throws ApiProviderException {
		return null;
	}

	@Override
	public List<Order> getUserCompletedOrders(String coin, String currency, Long since, Long end) throws ApiProviderException {

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
		if(completedOrdListGrp != null) {
			for (JsonElement o: completedOrdListGrp) {
				if (o != null) {
					BtOpenOrder oo = new BtOpenOrder();
					completedOrders.add(oo);
					JsonArray objArray = o.getAsJsonArray();
					oo.setClientCustomOrderID(objArray.get(0).getAsBigInteger());
					oo.setOrderID(objArray.get(1).getAsString());
					oo.setCumQty(objArray.get(2).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE)));
					oo.setOrdStatus(objArray.get(3).getAsString());
					oo.setLeavesQty(objArray.get(4).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE)));
					oo.setCxlQty(objArray.get(5).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE)));
					oo.setAvgPx(objArray.get(6).getAsBigDecimal());
					oo.setCoin(objArray.get(7).getAsString().substring(0, 3).toUpperCase());
					oo.setCurrency(objArray.get(7).getAsString().substring(3, 6).toUpperCase());
					String sideString = objArray.get(8).getAsString();
					OrderSide side = sideString.equals("1")? OrderSide.BUY:
						(sideString.equals("2")? OrderSide.SELL: null);
					oo.setSide(side);
					oo.setOrdType(objArray.get(9).getAsString());
					oo.setOrderQty(objArray.get(10).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE)));
					
					oo.setCurrencyPrice(objArray.get(11).getAsBigDecimal().divide(
						BlinktradeCurrency.getCurrencyBySimbol(oo.getCurrency()).getRate()
					));
					oo.setCreationDate( Utils.getCalendar(objArray.get(12).getAsString()));
					oo.setVolume(objArray.get(13).getAsBigDecimal());
					oo.setTimeInForce(objArray.get(14).getAsString());
					oo.setCoinAmount(oo.getCumQty().add(oo.getLeavesQty()));
				}
			}
		}
		return completedOrders;
	}
	
	@Override
	public List<Operation> getUserOperations(String coin, String currency, Long since, Long end) throws ApiProviderException {

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
		if(completedOrdListGrp != null) {
			for (JsonElement o: completedOrdListGrp) {
				if (o != null) {
					BtOperation oo = new BtOperation();
					clientOperations.add(oo);
					JsonArray objArray = o.getAsJsonArray();
					oo.setClientCustomOrderID(objArray.get(0).getAsBigInteger());
					oo.setOrderID(objArray.get(1).getAsString());
					oo.setCumQty(objArray.get(2).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE)));
					oo.setOrdStatus(objArray.get(3).getAsString());
					oo.setLeavesQty(objArray.get(4).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE)));
					oo.setCxlQty(objArray.get(5).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE)));
					oo.setAvgPx(objArray.get(6).getAsBigDecimal());
					oo.setCoin(objArray.get(7).getAsString().substring(0, 3).toUpperCase());
					oo.setCurrency(objArray.get(7).getAsString().substring(3, 6).toUpperCase());
					String sideString = objArray.get(8).getAsString();
					OrderSide side = sideString.equals("1")? OrderSide.BUY:
						(sideString.equals("2")? OrderSide.SELL: null);
					oo.setSide(side);
					oo.setOrdType(objArray.get(9).getAsString());
					oo.setOrderQty(objArray.get(10).getAsBigDecimal().divide(new BigDecimal(SATOSHI_BASE)));
					
					oo.setCurrencyPrice(objArray.get(11).getAsBigDecimal().divide(
						BlinktradeCurrency.getCurrencyBySimbol(oo.getCurrency()).getRate()
					));
					oo.setCreationDate( Utils.getCalendar(objArray.get(12).getAsString()));
					oo.setVolume(objArray.get(13).getAsBigDecimal());
					oo.setTimeInForce(objArray.get(14).getAsString());
					oo.setCoinAmount(oo.getCumQty().add(oo.getLeavesQty()));
				}
			}
		}
		return clientOperations;
	}

	/**
	 * Generating a bitcoin deposit address.
	 * 
	 * @param depositRequestID
	 *            An ID assigned by you. It can be any number. The response
	 *            message associated with this request will contain the same ID.
	 * 
	 * @return JSON Message which contains information about bitcoin address
	 *         generated.
	 * 
	 * @throws ApiProviderException
	 *             Throws an exception if some error occurs.
	 */
	public String createBitcoinAddressForDeposit(Integer depositRequestID)
			throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "U18");
		request.put("DepositReqID", depositRequestID);
		request.put("Currency", "BTC");
		request.put("BrokerID", broker.getBrokerID());

		return sendMessage(GSON.toJson(request));

	}

	/**
	 * Send trade Order (buy/sell).
	 * 
	 * @param clientOrderId
	 *            Client Order ID. Must be unique.
	 * @param symbol
	 *            Trade Symbol (BTC???, where '???' must be a valid Currency
	 *            Symbol).
	 * @param side
	 *            Order Side (Buy/Sell)
	 * @param type
	 *            Order Type (Market/Limited)
	 * @param currencyPrice
	 *            Price, with decimal symbol, without integer separator
	 *            (example: 5.00, 10.45, 230.4567).
	 * @param satoshiAmount
	 *            Amount of bitcoin to be bought/sold in satoshi (example:
	 *            12345678)
	 *            
	 * @return JSON message which contains information about performed order.
	 * 
	 * @throws ApiProviderException
	 *             Throws an exception if some error occurs.
	 * 
	 */
	public String sendNewOrder(Integer clientOrderId, String coin,
			String currency, OrderSide side, BlinktradeOrderType type,
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

		currencyPrice = currencyPrice.multiply(BlinktradeCurrency.getCurrencyBySimbol(currency).getRate().add(new BigDecimal(1)));
		coinAmount = coinAmount.multiply(new BigDecimal(SATOSHI_BASE));

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "D");
		request.put("ClOrdID", clientOrderId);
		request.put("Symbol", coin + currency);
		request.put("Side", side == OrderSide.BUY? "1": (side == OrderSide.SELL? "2": null));
		request.put("OrdType", type.getOrderType());
		request.put("Price", currencyPrice.toBigInteger());
		request.put("OrderQty", coinAmount.toBigInteger());
		request.put("BrokerID", broker.getBrokerID());

		return sendMessage(GSON.toJson(request));

	}
	
	public void createBuyOrder(
		String coin, String currency, BigDecimal coinAmount, BigDecimal currencyPrice
	) throws ApiProviderException {
		sendNewOrder(
			new Integer((int)(System.currentTimeMillis()/1000)),
			coin, currency, OrderSide.BUY,
			BlinktradeOrderType.LIMITED,
			coinAmount, currencyPrice
		);
	}
	
	public void createSellOrder(
		String coin, String currency, BigDecimal coinAmount, BigDecimal currencyPrice
	) throws ApiProviderException {
		sendNewOrder(
			new Integer((int)(System.currentTimeMillis()/1000)),
			coin, currency, OrderSide.SELL,
			BlinktradeOrderType.LIMITED,
			coinAmount, currencyPrice
		);
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
		request.put("BrokerID", broker.getBrokerID());

		return sendMessage(GSON.toJson(request));

	}
	
	public String cancelOrder(Order order) throws ApiProviderException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "F");
		request.put("ClOrdID", ((BtOpenOrder) order).getClientCustomOrderID());
		request.put("BrokerID", broker.getBrokerID());

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
			signature = hash(apiSecret, nonce);
		} catch (Exception e) {
			throw new ApiProviderException("Message signature fail", e);
		}

		/*
		 * API URL initialzation
		 */

		URL url = null;
		URLConnection http = null;

		try {

			if (BlinktradeBroker.TESTNET.equals(broker)) {
				url = new URL(BLINKTRADE_API_TESTNET_URL);
			} else {
				url = new URL(BLINKTRADE_API_PRODUCAO_URL);
			}

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
		http.setRequestProperty("APIKey", apiKey);
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

}
