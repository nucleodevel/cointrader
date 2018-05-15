/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.trader.cointrader.beans;

import java.io.Serializable;

/**
 * Filter object to be used on order list request.
 */
public class RecordFilter implements Serializable {

	private static final long serialVersionUID = 6302408184251869680L;

	private CoinCurrencyPair coinCurrencyPair;
	private RecordSide side;
	private OrderStatus status;
	private Boolean hasFills;
	private Long fromId;
	private Long toId;
	private Long endId;
	private Long fromTimestamp;
	private Long toTimestamp;
	private Long since;
	private Long end;

	public RecordFilter(Coin coin, Currency currency) {
		this.coinCurrencyPair = new CoinCurrencyPair(coin, currency);
	}

	public CoinCurrencyPair getCoinCurrencyPair() {
		return coinCurrencyPair;
	}

	public void setCoinCurrencyPair(CoinCurrencyPair coinCurrencyPair) {
		this.coinCurrencyPair = coinCurrencyPair;
	}
	
	public Coin getCoin() {
		return coinCurrencyPair.getCoin();
	}

	public void setCoin(Coin coin) {
		coinCurrencyPair.setCoin(coin);
	}

	public Currency getCurrency() {
		return coinCurrencyPair.getCurrency();
	}

	public void setCurrency(Currency currency) {
		coinCurrencyPair.setCurrency(currency);
	}
	
	public RecordSide getSide() {
		return side;
	}
	
	public void setSide(RecordSide side) { 
		this.side = side;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) { 
		this.status = status;
	}

	public Boolean getHasFills() {
		return hasFills;
	}

	public void setHasFills(Boolean hasFills) {
		this.hasFills = hasFills;
	}

	public Long getFromId() {
		return fromId;
	}

	public void setFromId(Long fromId) { 
		this.fromId = fromId;
	}

	public Long getToId() {
		return toId;
	}

	public void setToId(Long toId) {
		this.toId = toId;
	}

	public Long getEndId() {
		return endId;
	}

	public void setEndId(Long endId) { 
		this.endId = endId;
	}

	public Long getSince() {
		return since;
	}

	public Long getFromTimestamp() {
		return fromTimestamp;
	}

	public void setFromTimestamp(Long fromTimestamp) {
		this.fromTimestamp = fromTimestamp;
	}

	public Long getToTimestamp() {
		return toTimestamp;
	}

	public void setToTimestamp(Long toTimestamp) {
		this.toTimestamp = toTimestamp;
	}

	public void setSince(Long since) { 
		this.since = since;
	}

	public Long getEnd() {
		return end;
	}

	public void setEnd(Long end) { 
		this.end = end;
	}	

	@Override
	public String toString() {
		return "OrderFilter [coin=" + getCoin() + ", currency=" + getCurrency() 
				+ ", side=" + side + ", status="
				+ status + ", fromId=" + fromId + ", endId=" + endId
				+ ", since=" + since + ", end=" + end + "]";
	}
	
}