/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.blinktrade.tradeapi.to;

import java.util.Arrays;

import net.blinktrade.tradeapi.to.Order.CoinPair;
import net.blinktrade.tradeapi.to.Order.OrderType;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

/**
 * Mercado Bitcoin order book, contains open Orders, 'asks' for SELL orders and 'bids' for BUY orders.
 */
public class Orderbook {
	private Order[] asks;
	private Order[] bids;
	
	/**
	 * Constructor based on JSON response.
	 * 
	 * @param jsonObject Trade API JSON response
	 * @param pair Type of coins for the relationship, for instance, Bitcoin and Real.
	 */
	public Orderbook(JsonObject jsonObject, CoinPair pair) {
		JsonArray asking = jsonObject.get("asks").asArray();
		asks = new Order[asking.size()];
		for (int i = 0; i < asking.size(); i++) {
			asks[i] = new Order(asking.get(i).asArray(), pair, OrderType.SELL);
		}
		
		JsonArray bidding = jsonObject.get("bids").asArray();
		bids = new Order[bidding.size()];
		for (int i = 0; i < bidding.size(); i++) {
			bids[i] = new Order(bidding.get(i).asArray(), pair, OrderType.BUY);
		}
	}

	public Order[] getAsks() {
		return asks;
	}

	public Order[] getBids() {
		return bids;
	}

	@Override
	public String toString() {
		return "Orderbook [asks=" + Arrays.toString(asks) + "\nbids="
				+ Arrays.toString(bids) + "]";
	}

}
