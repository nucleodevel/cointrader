/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.trader.beans.Order;
import net.trader.beans.OrderBook;

/**
 * Mercado Bitcoin order book, contains open Orders, 'asks' for SELL orders and 'bids' for BUY orders.
 */
public class MbOrderBook extends OrderBook {
	
	private String coin;
	private String currency;
	
	private MbOrder[] asks;
	private MbOrder[] bids;
	
	/**
	 * Constructor based on JSON response.
	 * 
	 * @param jsonObject Trade API JSON response
	 * @param pair Side of coins for the relationship, for instance, Bitcoin and Real.
	 */
	public MbOrderBook(String coin, String currency) {
		this.coin = coin;
		this.currency = currency;
	}
	
	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	@Override
	public List<Order> getAsks() {
		ArrayList<Order> orders = new ArrayList<Order>();
		for (Order ask: asks)
			orders.add(ask);
		return orders;
	}

	public void setAsks(MbOrder[] asks) {
		this.asks = asks;
	}

	@Override
	public List<Order> getBids() {
		ArrayList<Order> orders = new ArrayList<Order>();
		for (Order bid: bids)
			orders.add(bid);
		return orders;
	}

	public void setBids(MbOrder[] bids) {
		this.bids = bids;
	}

	@Override
	public String toString() {
		return "Orderbook [asks=" + Arrays.toString(asks) + "\nbids="
				+ Arrays.toString(bids) + "]";
	}

}
