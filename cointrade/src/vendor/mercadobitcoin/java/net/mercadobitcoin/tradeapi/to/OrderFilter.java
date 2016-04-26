/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.util.HashMap;
import java.util.Map;

import net.mercadobitcoin.tradeapi.to.MbOrder.OrderStatus;
import net.mercadobitcoin.util.JsonHashMap;
import net.trader.beans.OrderSide;
import net.trader.exception.ApiProviderException;

/**
 * Filter object to be used on order list request.
 */
public class OrderFilter extends TapiBase {

	private static final long serialVersionUID = 6302408184251869680L;

	private String coin;
	private String currency;
	private OrderSide side;
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
	public OrderFilter(String coin, String currency) {
		super();
		this.coin =  coin;
		this.currency = currency;
	}

	/**
	 * @param pair Set the Coin Pair ("btc_brl" or "ltc_brl") filter.
	 */
	public void setPair(String coin, String currency) { 
		if (coin != null && currency != null) {
			this.coin =  coin;
			this.currency = currency;
		}
	}
	
	/**
	 * @param side Set the Order Side ("buy" or "sell") filter.
	 */
	public void setSide(OrderSide side) { 
		if (side != null) {
			this.side = side;
		}
	}
	
	public OrderSide getSide() {
		return side;
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
		return "OrderFilter [coin=" + coin + ", currency=" + currency + ", side=" + side + ", status="
				+ status + ", fromId=" + fromId + ", endId=" + endId
				+ ", since=" + since + ", end=" + end + "]";
	}

	/**
	 * Get the Parameters of the Object and return them as a list with the name and the value of each parameter.
	 * 
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public JsonHashMap toParams() throws ApiProviderException {
		JsonHashMap hashMap = new JsonHashMap();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			
			if (coin != null && currency != null)
				params.put("pair", coin.toLowerCase() + "_" + currency.toLowerCase());
			if (side != null)
				params.put("type", side == OrderSide.BUY? "buy": (side == OrderSide.SELL? "sell": null));
			if (status != null)
				params.put("status", status.getValue());
			if (fromId != null)
				params.put("from_id", fromId);
			if (endId != null)
				params.put("end_id", endId);
			if (since != null)
				params.put("since", since);
			if (end != null)
				params.put("end", end);
			
			hashMap.putAll(params);
		} catch (Throwable e) {
			throw new ApiProviderException("Internal error: Unable to transform the parameters in a request.");
		}
		return hashMap;
	}
}