/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.math.BigDecimal;

import net.trader.beans.Ticker;

/**
 * Information about trades in Mercado Bitcoin. Contains: 
 * <b>high</b>: Operations highest value in the time interval, in Brazilian Real.
 * <b>low</b>: Operations lowest value in the time interval, in Brazilian Real.
 * <b>vol</b>: Coin volume dealt with in the time interval.
 * <b>last</b>: Last operation's unit price, in Brazilian Real.
 * <b>buy</b>: Buy order's highest value, in Brazilian Real.
 * <b>sell</b>: Sell order's lowest value, in Brazilian Real.
 * <b>date</b>: Unix time of Ticker's last update.
 */
public class MbTicker extends Ticker {
	
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal vol;
	private BigDecimal last;
	private BigDecimal buy;
	private BigDecimal sell;
	private BigDecimal date;
	
	/**
	 * Constructor based on JSON response.
	 * 
	 * @param jsonObject Trade API JSON response
	 */
	public MbTicker() {
		
	}

	public BigDecimal getHigh() {
		return high;
	}

	public void setHigh(BigDecimal high) {
		this.high = high;
	}

	public BigDecimal getLow() {
		return low;
	}

	public void setLow(BigDecimal low) {
		this.low = low;
	}

	public BigDecimal getVol() {
		return vol;
	}

	public void setVol(BigDecimal vol) {
		this.vol = vol;
	}

	public BigDecimal getLast() {
		return last;
	}

	public void setLast(BigDecimal last) {
		this.last = last;
	}

	public BigDecimal getBuy() {
		return buy;
	}

	public void setBuy(BigDecimal buy) {
		this.buy = buy;
	}

	public BigDecimal getSell() {
		return sell;
	}

	public void setSell(BigDecimal sell) {
		this.sell = sell;
	}

	public BigDecimal getDate() {
		return date;
	}

	public void setDate(BigDecimal date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "Ticker [high=" + high + ", low=" + low + ", vol=" + vol
				+ ", last=" + last + ", buy=" + buy + ", sell=" + sell
				+ ", date=" + date + "]";
	}
	
}
