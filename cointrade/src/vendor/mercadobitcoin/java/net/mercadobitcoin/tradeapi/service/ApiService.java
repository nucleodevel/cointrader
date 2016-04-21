/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import net.mercadobitcoin.tradeapi.to.Operation;
import net.mercadobitcoin.tradeapi.to.MbOrder.CoinPair;
import net.mercadobitcoin.tradeapi.to.Orderbook;
import net.mercadobitcoin.tradeapi.to.Ticker;
import net.mercadobitcoin.util.TimestampInterval;
import net.trader.exception.ApiProviderException;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

/**
 * Public API Client service to communicate with Mercado Bitcoin API.
 * Used to retrieve general information about trades and orders in Mercado Bitcoin.
 */
public class ApiService extends AbstractApiService {

	private static final String API_PATH = "/api/";

	public ApiService() throws ApiProviderException {
		super();
	}

	public String getApiPath() {
		return API_PATH;
	}

	/**
	 * Get a Ticker with informations about trades of the last 24 hours.
	 * 
	 * @param coinPair Pair of coins to be used
	 * @return Ticker object with the information retrieved.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public Ticker ticker24h(CoinPair coinPair) throws ApiProviderException {
		String url = assemblyUrl(coinPair, "ticker");
		JsonObject jsonObject = JsonObject.readFrom(invokeApiMethod(url));
		JsonObject ticketJsonObject = jsonObject.get("ticker").asObject();
		return new Ticker(ticketJsonObject);
	}

	/**
	 * Get a Ticker with informations about trades since midnight.
	 * 
	 * @return Ticker object with the information retrieved.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public Ticker tickerOfToday(CoinPair coinPair) throws ApiProviderException {
		String url = assemblyUrl(coinPair, "v1/ticker");
		JsonObject jsonObject = JsonObject.readFrom(invokeApiMethod(url));
		JsonObject ticketJsonObject = jsonObject.get("ticker").asObject();
		return new Ticker(ticketJsonObject);
	}
	
	/**
	 * Get the list of the price and volume of the open orders.
	 * 
	 * @return Orderbook object with an Array of the open Orders.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public Orderbook orderbook(CoinPair coinPair) throws ApiProviderException {
		String url = assemblyUrl(coinPair, "orderbook");
		JsonObject jsonObject = JsonObject.readFrom(invokeApiMethod(url));
		return new Orderbook(jsonObject, CoinPair.BTC_BRL);
	}
	
	/**
	 * Get the list of executed trades. 
	 * 
	 * @return Trades object with an Array of the operations.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public Operation[] tradeList(CoinPair coinPair) throws ApiProviderException {
		return tradeList(coinPair, "");
	}

	/**
	 * Get the list of executed trades since the initial timestamp. 
	 * 
	 * @return Trades object with an Array of the operations.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public Operation[] tradeList(CoinPair coinPair, long initialTid) throws ApiProviderException {
		if (initialTid < 0) {
			throw new ApiProviderException("Invalid initial tid.");
		}
		return tradeList(coinPair, String.valueOf("?tid=" + initialTid));
	}
	
	/**
	 * Get the list of executed trades from the initial timestamp to the final timestamp. 
	 * 
	 * @return Trades object with an Array of the operations.
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public Operation[] tradeList(CoinPair coinPair, TimestampInterval interval) throws ApiProviderException {
		if (interval == null) {
			throw new ApiProviderException("Invalid date interval.");
		}
		
		List<String> paths = new ArrayList<String>();
		paths.add(interval.getFromTimestamp() + "/");
		
		if (interval.getToTimestamp() != null) {
			paths.add(interval.getToTimestamp() + "/");
		}
		
		return tradeList(coinPair, paths.toArray(new String[0]));
	}

	private Operation[] tradeList(CoinPair coinPair, String ... complements) throws ApiProviderException {
		String url = assemblyUrl(coinPair, "trades", complements);
		String response = invokeApiMethod(url.toString());
		JsonArray jsonObject = JsonArray.readFrom(response);

		//Convert Json response to object
		Operation[] operationList = new Operation[jsonObject.size()];
		for (int i = 0; i < jsonObject.size(); i++) {
			operationList[i] = new Operation(jsonObject.get(i).asObject());
		}
		return operationList;
	}
	
	/*
	 * Assembly url to be invoked based on coin pair and parameters
	 */
	private String assemblyUrl(CoinPair coinPair, String method, String ... pathParams) throws ApiProviderException {
		if (coinPair == null) {
			throw new ApiProviderException("Invalid coin pair.");
		}

		StringBuffer url = new StringBuffer(method);
		
		if (coinPair.equals(CoinPair.LTC_BRL)) {
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
	
}