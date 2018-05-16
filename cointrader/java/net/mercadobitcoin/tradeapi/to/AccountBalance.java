/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import com.eclipsesource.json.JsonObject;

import java.io.Serializable;

/**
 * User's information of funds, opened orders and server date/time.
 */
public class AccountBalance implements Serializable {

	private static final long serialVersionUID = 6922034267613306463L;

	private Integer serverTime;
	private Integer openOrders;
	private Funds funds;

	/**
	 * Constructor based on JSON response.
	 * 
	 * @param jsonObject Trade API JSON response
	 */
	public AccountBalance(JsonObject jsonObject) {
		this.serverTime = Integer.valueOf(jsonObject.get("server_time").asString());
		this.openOrders = jsonObject.get("open_orders").asInt();
		this.funds = new Funds(jsonObject.get("funds").asObject());
	}

	public Integer getServerTime() {
		return serverTime;
	}

	public Integer getOpenOrders() {
		return openOrders;
	}

	public Funds getFunds() {
		return funds;
	}

	@Override
	public String toString() {
		return "AccountBalance [serverTime=" + serverTime + ", openOrders="
				+ openOrders + ", funds=" + funds + "]";
	}

}
