/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.mercadobitcoin.tradeapi.to.MbOrder.CoinPair;
import net.trader.beans.Order;
import net.trader.beans.OrderBook;
import net.trader.beans.OrderSide;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

/**
 * Mercado Bitcoin order book, contains open Orders, 'asks' for SELL orders and 'bids' for BUY orders.
 */
public class MbOrderBook extends OrderBook {
	
	private MbOrder[] asks;
	private MbOrder[] bids;
	
	/**
	 * Constructor based on JSON response.
	 * 
	 * @param jsonObject Trade API JSON response
	 * @param pair Side of coins for the relationship, for instance, Bitcoin and Real.
	 */
	public MbOrderBook(JsonObject jsonObject, CoinPair pair) {
		JsonArray asking = jsonObject.get("asks").asArray();
		asks = new MbOrder[asking.size()];
		for (int i = 0; i < asking.size(); i++) {
			asks[i] = new MbOrder(asking.get(i).asArray(), pair, OrderSide.SELL);
		}
		
		JsonArray bidding = jsonObject.get("bids").asArray();
		bids = new MbOrder[bidding.size()];
		for (int i = 0; i < bidding.size(); i++) {
			bids[i] = new MbOrder(bidding.get(i).asArray(), pair, OrderSide.BUY);
		}
	}
	
	@Override
	public List<Order> getAsks() {
		ArrayList<Order> orders = new ArrayList<Order>();
		for (Order ask: asks)
			orders.add(ask);
		return orders;
	}

	@Override
	public List<Order> getBids() {
		ArrayList<Order> orders = new ArrayList<Order>();
		for (Order bid: bids)
			orders.add(bid);
		return orders;
	}

	@Override
	public String toString() {
		return "Orderbook [asks=" + Arrays.toString(asks) + "\nbids="
				+ Arrays.toString(bids) + "]";
	}

}
