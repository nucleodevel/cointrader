/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.blinktrade.tradeapi.to;

import net.blinktrade.tradeapi.to.Order.CoinPair;
import net.blinktrade.tradeapi.to.Order.OrderStatus;
import net.blinktrade.tradeapi.to.Order.OrderType;

/**
 * Filter object to be used on order list request.
 */
public class OrderFilter extends TapiBase {

	private static final long serialVersionUID = 6302408184251869680L;

	private CoinPair pair;
	private OrderType type;
	private OrderStatus status;
	private Long fromId;
	private Long endId;
	private Long since;
	private Long end;

	/**
	 * Constructor for the Object sent to request a list of Orders, with the defined parameters as filters.
	 * 
	 * @param pair Define the Coin Pair to filter the list.
	 */
	public OrderFilter(CoinPair pair) {
		super();
		this.pair =  pair;
	}

	/**
	 * @param pair Set the Coin Pair ("btc_brl" or "ltc_brl") filter.
	 */
	public void setPair(CoinPair pair) { 
		if (pair != null) {
			this.pair = pair;
		}
	}

	public CoinPair getPair() {
		return pair;
	}
	
	/**
	 * @param type Set the Order Type ("buy" or "sell") filter.
	 */
	public void setType(OrderType type) { 
		if (type != null) {
			this.type = type;
		}
	}
	
	public OrderType getType() {
		return type;
	}

	/**
	 * @param status Filter the list by "Active", "Canceled" or "Completed" status.
	 */
	public void setStatus(OrderStatus status) { 
		if (status != null) {
			this.status = status;
		}
	}

	public OrderStatus getStatus() {
		return status;
	}

	/**
	 * @param fromId Add an initial Id for the list.
	 */
	public void setFromId(Long fromId) { 
		if (fromId != null) {
			this.fromId = fromId;
		}
	}

	public Long getFromId() {
		return fromId;
	}

	/**
	 * @param endId Add a final Id for the list.
	 */
	public void setEndId(Long endId) { 
		if (endId != null) {
			this.endId = endId;
		}
	}

	public Long getEndId() {
		return endId;
	}

	/**
	 * @param since Add a starting Unix Time for the list.
	 */
	public void setSince(Long since) { 
		if (since != null) {
			this.since = since;
		}
	}

	public Long getSince() {
		return since;
	}

	/**
	 * @param end Add a final Unix Time for the list.
	 */
	public void setEnd(Long end) { 
		if (end != null) {
			this.end = end;
		}
	}	

	public Long getEnd() {
		return end;
	}

	@Override
	public String toString() {
		return "OrderFilter [pair=" + pair + ", type=" + type + ", status="
				+ status + ", fromId=" + fromId + ", endId=" + endId
				+ ", since=" + since + ", end=" + end + "]";
	}
}