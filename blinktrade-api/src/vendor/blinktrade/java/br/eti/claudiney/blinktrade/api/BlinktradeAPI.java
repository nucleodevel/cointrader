package br.eti.claudiney.blinktrade.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import br.eti.claudiney.blinktrade.api.beans.OpenOrder;
import br.eti.claudiney.blinktrade.api.beans.OrderBookResponse;
import br.eti.claudiney.blinktrade.enums.BlinktradeBroker;
import br.eti.claudiney.blinktrade.enums.BlinktradeOrderSide;
import br.eti.claudiney.blinktrade.enums.BlinktradeOrderType;
import br.eti.claudiney.blinktrade.enums.BlinktradeSymbol;
import br.eti.claudiney.blinktrade.exception.BlinktradeAPIException;

import com.google.gson.Gson;

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
public class BlinktradeAPI {

	private static final String BLINKTRADE_API_PRODUCAO_URL = "https://api.blinktrade.com/tapi/v1/message";
	private static final String BLINKTRADE_API_TESTNET_URL = "https://api.testnet.blinktrade.com/tapi/v1/message";
	
	private static final String BLINKTRADE_PUBLIC_API_ORDERBOOK = "https://api.blinktrade.com/api/v1/BRL/orderbook";
	//private static final String BLINKTRADE_PUBLIC_API_TRADES = "https://api.blinktrade.com/api/v1/BRL/trades";
	
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
	public BlinktradeAPI(String apiKey, String apiSecret,
			BlinktradeBroker broker) throws BlinktradeAPIException {

		if (apiKey == null) {
			throw new BlinktradeAPIException("APIKey cannot be null");
		}

		if (apiSecret == null) {
			throw new BlinktradeAPIException("APISecret cannot be null");
		}

		if (broker == null) {
			throw new BlinktradeAPIException("Broker cannot be null");
		}

		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
		this.broker = broker;

	}

	/**
	 * Request Balance.
	 * 
	 * @param balanceRequestID
	 *            An ID assigned by you. It can be any number. The response
	 *            message associated with this request will contain the same ID.
	 * 
	 * @return JSON message which contains information about balance requested.
	 * 
	 * @throws BlinktradeAPIException
	 *             Throws an exception if some error occurs.
	 */
	public String getBalance(Integer balanceRequestID) throws Exception {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "U2");
		request.put("BalanceReqID", balanceRequestID); // new Integer(1)

		return sendMessage(GSON.toJson(request));

	}

	/**
	 * Request Open Orders.
	 * 
	 * @param orderRequestID
	 *            An ID assigned by you. It can be any number. The response
	 *            message associated with this request will contain the same ID.
	 * 
	 * @return JSON Message which contains information about open orders
	 *         requested.
	 * 
	 * @throws BlinktradeAPIException
	 *             Throws an exception if some error occurs.
	 */
	public String requestOpenOrders(Integer orderRequestID)
			throws BlinktradeAPIException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		List<String> filters = new ArrayList<String>(1);
		filters.add("has_leaves_qty eq 1");

		request.put("MsgType", "U4");
		request.put("OrdersReqID", orderRequestID);
		request.put("Page", new Integer(0));
		request.put("PageSize", new Integer(100));
		request.put("Filter", filters);

		return sendMessage(GSON.toJson(request));

	}

	/**
	 * Request Completed Orders.
	 * 
	 * @param orderRequestID
	 *            An ID assigned by you. It can be any number. The response
	 *            message associated with this request will contain the same ID.
	 * 
	 * @return JSON Message which contains information about open orders
	 *         requested.
	 * 
	 * @throws BlinktradeAPIException
	 *             Throws an exception if some error occurs.
	 */
	public String requestCompletedOrders(Integer orderRequestID)
			throws BlinktradeAPIException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		List<String> filters = new ArrayList<String>(1);
		filters.add("has_cum_qty eq 1");

		request.put("MsgType", "U4");
		request.put("OrdersReqID", orderRequestID);
		request.put("Page", new Integer(0));
		request.put("PageSize", new Integer(100));
		request.put("Filter", filters);

		return sendMessage(GSON.toJson(request));

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
	 * @throws BlinktradeAPIException
	 *             Throws an exception if some error occurs.
	 */
	public String createBitcoinAddressForDeposit(Integer depositRequestID)
			throws BlinktradeAPIException {

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
	 * @throws BlinktradeAPIException
	 *             Throws an exception if some error occurs.
	 * 
	 */
	public String sendNewOrder(Integer clientOrderId, BlinktradeSymbol symbol,
			BlinktradeOrderSide side, BlinktradeOrderType type,
			BigDecimal currencyPrice, BigInteger satoshiAmount)
			throws BlinktradeAPIException {

		if (clientOrderId == null) {
			throw new BlinktradeAPIException("ClientOrderID  cannot be null");
		}

		if (symbol == null) {
			throw new BlinktradeAPIException("Symbol (currency) cannot be null");
		}

		if (side == null) {
			throw new BlinktradeAPIException("Side (buy/sell) cannot be null");
		}

		if (type == null) {
			throw new BlinktradeAPIException(
					"Type (market/limited) cannot be null");
		}

		if (currencyPrice == null) {
			throw new BlinktradeAPIException("Price cannot be null");
		}

		if (satoshiAmount == null) {
			throw new BlinktradeAPIException("Amount (satoshi) cannot be null");
		}

		currencyPrice = currencyPrice.multiply(new BigDecimal("100000001"));

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "D");
		request.put("ClOrdID", clientOrderId);
		request.put("Symbol", symbol.getId());
		request.put("Side", side.getSide());
		request.put("OrdType", type.getOrderType());
		request.put("Price", currencyPrice.toBigInteger());
		request.put("OrderQty", satoshiAmount);
		request.put("BrokerID", broker.getBrokerID());

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
	 * @throws BlinktradeAPIException
	 *             Throws an exception if some error occurs.
	 */
	public String cancelOrder(Integer clientOrderId)
			throws BlinktradeAPIException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "F");
		request.put("ClOrdID", clientOrderId);
		request.put("BrokerID", broker.getBrokerID());

		return sendMessage(GSON.toJson(request));

	}
	
	public String cancelOrder(OpenOrder order)
			throws BlinktradeAPIException {

		Map<String, Object> request = new LinkedHashMap<String, Object>();

		request.put("MsgType", "F");
		request.put("ClOrdID", order.getClientCustomOrderID());
		request.put("BrokerID", broker.getBrokerID());

		return sendMessage(GSON.toJson(request));

	}

	/*
	 * Perform API requests.
	 */
	private String sendMessage(String requestMessage)
			throws BlinktradeAPIException {

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
			throw new BlinktradeAPIException("Message signature fail", e);
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
			throw new BlinktradeAPIException("API URL initialization fail", e);
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
			throw new BlinktradeAPIException("API Request fail", e);
		}

		/*
		 * Retrieve response
		 */
		String responseMessage = null;

		try {
			is = http.getInputStream();
			responseMessage = IOUtils.toString(is);
		} catch (Exception e) {
			throw new BlinktradeAPIException("API response retrieve fail", e);
		}

		return responseMessage;

	}

	/*
	 * API Message signatures using HMAC-SHA256.
	 */
	private static String hash(String secret, String message) throws Exception {

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

	public OrderBookResponse getOrderBook() throws BlinktradeAPIException {

		/*
		 * API URL initialzation
		 */

		URL url = null;
		URLConnection http = null;

		try {
			url = new URL(BLINKTRADE_PUBLIC_API_ORDERBOOK);
			http = url.openConnection();
		} catch (Exception e) {
			throw new BlinktradeAPIException("API URL initialization fail", e);
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
			throw new BlinktradeAPIException("API response retrieve fail", e);
		}

		return GSON.fromJson(responseMessage, OrderBookResponse.class);

	}

}
