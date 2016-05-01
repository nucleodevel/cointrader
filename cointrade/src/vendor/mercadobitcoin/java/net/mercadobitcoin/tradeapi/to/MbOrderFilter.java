/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.io.Serializable;

import net.mercadobitcoin.enums.MbOrderStatus;
import net.trader.beans.RecordSide;

/**
 * Filter object to be used on order list request.
 */
public class MbOrderFilter implements Serializable {

	private static final long serialVersionUID = 6302408184251869680L;

	private String coin;
	private String currency;
	private RecordSide side;
	private MbOrderStatus status;
	private Long fromId;
	private Long endId;
	private Long since;
	private Long end;

	/**
	 * Constructor for the Object sent to request a list of Orders, with the defined parameters as filters.
	 * 
	 * @param pair Define the Coin Pair to filter the list.
	 */
	public MbOrderFilter(String coin, String currency) {
		super();
		this.coin =  coin;
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
	public void setSide(RecordSide side) { 
		if (side != null) {
			this.side = side;
		}
	}
	
	public RecordSide getSide() {
		return side;
	}

	/**
	 * @param status Filter the list by "Active", "Canceled" or "Completed" status.
	 */
	public void setStatus(MbOrderStatus status) { 
		if (status != null) {
			this.status = status;
		}
	}

	public MbOrderStatus getStatus() {
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
	
}