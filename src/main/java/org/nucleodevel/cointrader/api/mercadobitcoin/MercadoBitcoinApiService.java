/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package org.nucleodevel.cointrader.api.mercadobitcoin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import org.nucleodevel.cointrader.api.ApiService;
import org.nucleodevel.cointrader.beans.Balance;
import org.nucleodevel.cointrader.beans.Coin;
import org.nucleodevel.cointrader.beans.CoinCurrencyPair;
import org.nucleodevel.cointrader.beans.Currency;
import org.nucleodevel.cointrader.beans.Operation;
import org.nucleodevel.cointrader.beans.Order;
import org.nucleodevel.cointrader.beans.OrderBook;
import org.nucleodevel.cointrader.beans.OrderStatus;
import org.nucleodevel.cointrader.beans.RecordFilter;
import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.beans.Ticker;
import org.nucleodevel.cointrader.beans.UserConfiguration;
import org.nucleodevel.cointrader.exception.ApiProviderException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MercadoBitcoinApiService extends ApiService {

	private static Authorized authorized;
	private static Account account;

	// --------------------- Constructor

	public MercadoBitcoinApiService(UserConfiguration userConfiguration, CoinCurrencyPair coinCurrencyPair)
			throws ApiProviderException {
		super(userConfiguration, coinCurrencyPair);
	}

	// --------------------- Getters and setters

	@Override
	protected String getDomain() {
		return "https://api.mercadobitcoin.net";
	}

	@Override
	protected String getPublicApiUrl() {
		return getDomain() + getPublicApiPath() + "/";
	}

	@Override
	protected String getPrivateApiUrl() {
		return getDomain() + getPrivateApiPath();
	}

	@Override
	protected String getPublicApiPath() {
		return "/api/v4/";
	}

	@Override
	protected String getPrivateApiPath() {
		return "/api/v4/accounts/" + account.getId() + "/";
	}

	@Override
	public TimeZone getTimeZone() {
		return TimeZone.getTimeZone("GMT-03:00");
	}

	@Override
	protected void makeActionInConstructor() throws ApiProviderException {

		if (userConfiguration.getSecret() == null) {
			throw new ApiProviderException("Null code.");
		}

		if (userConfiguration.getKey() == null) {
			throw new ApiProviderException("Null key.");
		}
	}

	private String getCoinCurrencyPairUrlString() {
		return getCoin().getValue().toUpperCase() + "-" + getCurrency().getValue().toUpperCase();
	}

	// --------------------- Overrided methods

	@Override
	public Ticker getTicker() throws ApiProviderException {
		Map<String, String> queryParamMap = Map.of("symbols", getCoinCurrencyPairUrlString());
		JsonArray tickerJsonArray = (JsonArray) makePublicRequest("tickers", queryParamMap);
		return getTicker(tickerJsonArray);
	}

	@Override
	public Balance getBalance() throws ApiProviderException {
		JsonElement jsonElement = makePrivateRequest("balances", "GET", null);
		if (jsonElement == null)
			throw new ApiProviderException("Balance is not available!");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		return getBalance(jsonArray);
	}

	@Override
	public OrderBook getOrderBook() throws ApiProviderException {
		Map<String, String> queryParamMap = Map.of("limit", "100");
		JsonObject orderBookJsonArray = (JsonObject) makePublicRequest(getCoinCurrencyPairUrlString() + "/orderbook",
				queryParamMap);
		return getOrderBook(orderBookJsonArray);
	}

	@Override
	public List<Operation> getOperationList(Calendar from, Calendar to) throws ApiProviderException {
		return List.of();
	}

	private List<Order> getUserOrders(OrderStatus status) throws ApiProviderException {

		Map<String, String> queryParamMap = new HashMap<>();
		if (status != null) {
			String statusStr = status == OrderStatus.ACTIVE ? "working"
					: (status == OrderStatus.CANCELED ? "cancelled"
							: (status == OrderStatus.COMPLETED ? "filled" : null));
			queryParamMap.put("status", statusStr);
		}

		JsonElement jsonElement = makePrivateRequest(getCoinCurrencyPairUrlString() + "/orders", "GET", queryParamMap);
		if (jsonElement == null)
			throw new ApiProviderException("List of user active orders is not available!");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		List<Order> orders = new ArrayList<Order>();
		for (JsonElement jsonOrder : jsonArray) {
			Order order = getOrder(jsonOrder.getAsJsonObject());
			orders.add(order);
		}
		return orders;
	}

	@Override
	public List<Order> getUserActiveOrders() throws ApiProviderException {
		return getUserOrders(OrderStatus.ACTIVE);
	}

	@Override
	public List<Operation> getUserOperations() throws ApiProviderException {
		RecordFilter orderFilter = new RecordFilter(getCoin(), getCurrency());
		orderFilter.setHasFills(true);

		List<Order> completedOrders = getUserOrders(OrderStatus.COMPLETED);
		Collections.sort(completedOrders);

		List<Operation> operations = new ArrayList<Operation>();
		for (Order o : completedOrders) {
			Order order = (Order) o;
			if (order.getOperations() != null)
				for (Operation operation : order.getOperations()) {
					operation.setSide(order.getSide());
					operations.add(operation);
				}
		}
		return operations;
	}

	@Override
	public Order cancelOrder(Order order) throws ApiProviderException {
		if (order == null)
			throw new ApiProviderException("Invalid order.");

		makePrivateRequest(getCoinCurrencyPairUrlString() + "/orders/" + order.getId(), "DELETE", null);
		order.setStatus(OrderStatus.CANCELED);
		return order;
	}

	@Override
	public Order createOrder(Order order) throws ApiProviderException {
		if (order == null)
			throw new ApiProviderException("Invalid order.");

		JsonElement jsonResponse = makePrivateRequest(getParams(order), getCoinCurrencyPairUrlString() + "/orders",
				"POST", null);

		JsonObject jsonObject = jsonResponse.getAsJsonObject();
		String id = jsonObject.getAsJsonPrimitive("orderId").getAsString();
		order.setId(id);

		return order;
	}

	// --------------------- Request methods

	private void authorize() throws ApiProviderException {

		if (authorized == null || authorized.isExpired()) {
			URL url;
			try {
				url = new URL(getDomain() + "/api/v4/authorize");

				HttpURLConnection conn = (HttpsURLConnection) url.openConnection();

				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setRequestProperty("Accept", "application/json");

				conn.setDoOutput(true);

				JsonObject bodyJsonObject = new JsonObject();
				bodyJsonObject.addProperty("login", userConfiguration.getKey());
				bodyJsonObject.addProperty("password", userConfiguration.getSecret());

				String body = bodyJsonObject.toString();

				try (OutputStream os = conn.getOutputStream()) {
					byte[] input = body.getBytes("utf-8");
					os.write(input, 0, input.length);
				}
				try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
					StringBuilder response = new StringBuilder();
					String responseLine = null;
					while ((responseLine = br.readLine()) != null) {
						response.append(responseLine.trim());
					}

					JsonObject jsonResponse = (JsonObject) JsonParser.parseString(response.toString());
					String accessToken = jsonResponse.getAsJsonPrimitive("access_token").getAsString();

					if (accessToken != null && !accessToken.isEmpty()) {
						long expiration = jsonResponse.getAsJsonPrimitive("expiration").getAsLong();

						authorized = new Authorized(accessToken, expiration);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new ApiProviderException("Internal error: Failure in connection.");
			}
		}
	}

	private Account getAccount() throws ApiProviderException {

		if (authorized == null || authorized.isExpired()) {
			authorize();
		}

		if (account == null) {
			URL url;
			try {
				url = new URL(getDomain() + "/api/v4/accounts");

				HttpURLConnection conn = (HttpsURLConnection) url.openConnection();

				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setRequestProperty("Authorization", "Bearer " + authorized.getAccessToken());

				try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
					StringBuilder response = new StringBuilder();
					String responseLine = null;
					while ((responseLine = br.readLine()) != null) {
						response.append(responseLine.trim());
					}

					JsonArray jsonResponse = (JsonArray) JsonParser.parseString(response.toString());
					if (jsonResponse != null && jsonResponse.size() > 0) {
						JsonObject jsonObject = jsonResponse.get(0).getAsJsonObject();

						String id = jsonObject.getAsJsonPrimitive("id").getAsString();
						String name = jsonObject.getAsJsonPrimitive("name").getAsString();
						String type = jsonObject.getAsJsonPrimitive("type").getAsString();
						String currency = jsonObject.getAsJsonPrimitive("currency").getAsString();
						String currencySign = jsonObject.getAsJsonPrimitive("currency").getAsString();

						account = new Account(id, name, type, currency, currencySign);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new ApiProviderException("Internal error: Failure in connection.");
			}
		}

		return account;
	}

	private JsonElement makePublicRequest(String method, Map<String, String> params) throws ApiProviderException {

		// putting delay time
		try {
			TimeUnit.MILLISECONDS.sleep(1010);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (getCoin() == null || getCurrency() == null) {
			throw new ApiProviderException("Invalid coin pair.");
		}

		StringBuffer url = new StringBuffer(method);

		StringBuilder queryString = new StringBuilder();
		if (params != null) {
			queryString.append("?");
			for (Map.Entry<String, String> param : params.entrySet()) {
				if (queryString.length() > 0) {
					queryString.append("&");
				}
				queryString.append(param.getKey()).append("=").append(param.getValue());
			}
		}

		url.append(queryString);

		try {
			URL urlVar = new URL(getPublicApiUrl() + url.toString());
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
			JsonElement jsonElement = JsonParser.parseString(response);

			return jsonElement;
		} catch (MalformedURLException e) {
			throw new ApiProviderException("Internal error: Invalid URL.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new ApiProviderException("Internal error: Failure in connection.");
		}
	}

	private JsonElement makePrivateRequest(String method, String requestMethod, Map<String, String> queryParams)
			throws ApiProviderException {
		return makePrivateRequest(null, method, requestMethod, queryParams);
	}

	private JsonElement makePrivateRequest(JsonObject bodyJsonObject, String method, String requestMethod,
			Map<String, String> queryParams) throws ApiProviderException {

		// putting delay time
		try {
			TimeUnit.MILLISECONDS.sleep(1010);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (account == null)
			getAccount();

		String responseStr = null;

		try {

			StringBuilder queryString = new StringBuilder();
			if (queryParams != null && !queryParams.isEmpty()) {
				queryString.append("?");
				for (Map.Entry<String, String> param : queryParams.entrySet()) {
					if (queryString.length() > 0) {
						queryString.append("&");
					}
					queryString.append(param.getKey()).append("=").append(param.getValue());
				}
			}

			URL url = new URL(getPrivateApiUrl() + method + queryString);

			HttpURLConnection conn;
			conn = (HttpsURLConnection) url.openConnection();

			conn.setRequestMethod(requestMethod);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", "Bearer " + authorized.getAccessToken());

			if (bodyJsonObject != null) {
				conn.setRequestProperty("Accept", "application/json");

				conn.setDoOutput(true);

				String body = bodyJsonObject.toString();
				try (OutputStream os = conn.getOutputStream()) {
					byte[] input = body.getBytes("utf-8");
					os.write(input, 0, input.length);
				}
			}

			BufferedReader reader = null;
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}

			responseStr = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ApiProviderException("Internal error: Failure in connection.");
		}

		if (responseStr == null) {
			throw new ApiProviderException("Internal error: null response from the server.");
		}

		JsonElement jsonResponse = JsonParser.parseString(responseStr);

		return (jsonResponse == null) ? null : jsonResponse;
	}

	// --------------------- Object to json

	private JsonObject getParams(Order order) throws ApiProviderException {
		JsonObject jsonObject = new JsonObject();
		try {
			Double incDecPrice = Math.abs(userConfiguration.getIncDecPrice(order.getSide()));
			Double log10 = Math.log10(incDecPrice);
			int scale = Math.abs((int) Math.round(log10));

			jsonObject.addProperty("async", false);
			jsonObject.addProperty("type", "limit");
			if (order.getSide() != null)
				jsonObject.addProperty("side", order.getSide().getValue().toLowerCase());
			if (order.getCoinAmount() != null)
				jsonObject.addProperty("qty", "" + order.getCoinAmount());
			if (order.getCurrencyPrice() != null) {
				BigDecimal currencyPrice = order.getCurrencyPrice().setScale(scale, RoundingMode.HALF_UP);
				jsonObject.addProperty("limitPrice", currencyPrice);
			}

		} catch (Throwable e) {
			throw new ApiProviderException("Internal error: Unable to transform the parameters in a request.");
		}
		return jsonObject;
	}

	// --------------------- Json to object

	private Ticker getTicker(JsonArray tickerJsonArray) {
		Coin coin = getCoin();
		Currency currency = getCurrency();

		Ticker ticker = new Ticker(coin, currency);

		if (tickerJsonArray != null && !tickerJsonArray.isEmpty()) {
			JsonObject tickerJsonObject = tickerJsonArray.get(0).getAsJsonObject();

			ticker.setHigh(tickerJsonObject.getAsJsonPrimitive("high").getAsBigDecimal());
			ticker.setLow(tickerJsonObject.getAsJsonPrimitive("low").getAsBigDecimal());
			ticker.setVol(tickerJsonObject.getAsJsonPrimitive("vol").getAsBigDecimal());

			ticker.setLast3HourVolume(ticker.getVol().divide(BigDecimal.valueOf(8L)));
		}

		return ticker;
	}

	private Balance getBalance(JsonArray balanceJsonArray) {
		Balance balance = new Balance(getCoin(), getCurrency());

		for (JsonElement balanceJsonElement : balanceJsonArray) {
			JsonObject balanceJsonObject = balanceJsonElement.getAsJsonObject();

			String symbol = balanceJsonObject.getAsJsonPrimitive("symbol").getAsString();
			if (symbol.equals(getCurrency().getValue().toUpperCase())) {
				BigDecimal currencyAmount = new BigDecimal(balanceJsonObject.getAsJsonPrimitive("total").getAsString());
				BigDecimal currencyLocked = new BigDecimal(
						balanceJsonObject.getAsJsonPrimitive("on_hold").getAsString());

				balance.setCurrencyAmount(currencyAmount);
				balance.setCurrencyLocked(currencyLocked);
			} else if (symbol.equals(getCoin().getValue().toUpperCase())) {
				BigDecimal coinAmount = new BigDecimal(balanceJsonObject.getAsJsonPrimitive("total").getAsString());
				BigDecimal coinLocked = new BigDecimal(balanceJsonObject.getAsJsonPrimitive("on_hold").getAsString());

				balance.setCoinAmount(coinAmount);
				balance.setCoinLocked(coinLocked);
			}

			if (balance.getCurrencyAmount() != null && balance.getCoinAmount() != null)
				break;
		}

		return balance;
	}

	private OrderBook getOrderBook(JsonObject orderBookJsonObject) throws ApiProviderException {
		OrderBook orderBook = new OrderBook(getCoin(), getCurrency());

		JsonArray asking = orderBookJsonObject.getAsJsonArray("asks");
		ArrayList<Order> askOrders = new ArrayList<Order>();
		for (int i = 0; i < asking.size(); i++) {
			JsonArray pair = asking.get(i).getAsJsonArray();
			String amountStr = pair.get(1).getAsString();
			String priceStr = pair.get(0).getAsString();
			BigDecimal coinAmount = new BigDecimal(amountStr);
			BigDecimal currencyPrice = new BigDecimal(priceStr);
			Order order = new Order(getCoin(), getCurrency(), RecordSide.SELL, coinAmount, currencyPrice);
			order.setStatus(OrderStatus.ACTIVE);
			order.setPosition(i + 1);
			askOrders.add(order);
		}
		orderBook.setAskOrders(askOrders);

		JsonArray bidding = orderBookJsonObject.get("bids").getAsJsonArray();
		ArrayList<Order> bidOrders = new ArrayList<Order>();
		for (int i = 0; i < bidding.size(); i++) {
			JsonArray pair = bidding.get(i).getAsJsonArray();
			String amountStr = pair.get(1).getAsString();
			String priceStr = pair.get(0).getAsString();
			BigDecimal coinAmount = new BigDecimal(amountStr);
			BigDecimal currencyPrice = new BigDecimal(priceStr);
			Order order = new Order(getCoin(), getCurrency(), RecordSide.BUY, coinAmount, currencyPrice);
			order.setStatus(OrderStatus.ACTIVE);
			order.setPosition(i + 1);
			bidOrders.add(order);
		}
		orderBook.setBidOrders(bidOrders);

		return orderBook;
	}

	private Order getOrder(JsonObject jsonObject) {
		String coinPair = jsonObject.get("instrument").getAsString().toUpperCase();

		Coin coin = Coin.valueOf(coinPair.substring(0, 3).toUpperCase());
		Currency currency = Currency.valueOf(coinPair.substring(4, 7).toUpperCase());

		String sideStr = jsonObject.get("side").getAsString();
		RecordSide side = sideStr.equals("buy") ? RecordSide.BUY : (sideStr.equals("sell") ? RecordSide.SELL : null);
		BigDecimal coinAmount = new BigDecimal(jsonObject.get("qty").getAsString());
		BigDecimal currencyPrice = new BigDecimal(jsonObject.get("limitPrice").getAsString());

		Order order = new Order(coin, currency, side, coinAmount, currencyPrice);
		order.setId((jsonObject.get("id").getAsString()));

		String statusStr = jsonObject.get("status").getAsString();
		OrderStatus status = statusStr.equals("working") ? OrderStatus.ACTIVE
				: (statusStr.equals("cancelled") ? OrderStatus.CANCELED
						: (statusStr.equals("filled") ? OrderStatus.COMPLETED : null));
		order.setStatus(status);

		long created = jsonObject.get("created_at").getAsLong();
		order.setCreationDate(Calendar.getInstance());
		order.getCreationDate().setTimeInMillis((long) created * 1000);

		List<Operation> operations = new ArrayList<Operation>();
		JsonArray jsonOperationArray = jsonObject.get("executions") == null ? new JsonArray()
				: jsonObject.get("executions").getAsJsonArray();

		for (JsonElement jsonOperation : jsonOperationArray) {
			Operation operation = getOperation(jsonOperation.getAsJsonObject(), side);
			operation.setSide(order.getSide());
			operations.add(operation);
		}
		order.setOperations(operations);

		return order;
	}

	private Operation getOperation(JsonObject jsonObject, RecordSide side) {
		Operation operation;

		long created = Long.valueOf(jsonObject.get("executed_at").getAsString());
		BigDecimal coinAmount = new BigDecimal(jsonObject.get("qty").getAsString());
		BigDecimal currencyPrice = new BigDecimal(jsonObject.get("price").getAsString());

		operation = new Operation(getCoin(), getCurrency(), side, coinAmount, currencyPrice);

		operation.setId(jsonObject.get("id").getAsString());
		operation.setRate(new BigDecimal(jsonObject.get("fee_rate").getAsString()));
		operation.setCreationDate(Calendar.getInstance());
		operation.getCreationDate().setTimeInMillis(created * 1000);

		return operation;
	}

}