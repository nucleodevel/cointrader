/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.math.BigDecimal;

import com.eclipsesource.json.JsonObject;

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
public class Ticker {
	
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
	public Ticker(JsonObject jsonObject) {
		this.high = new BigDecimal(jsonObject.get("high").toString());
		this.low = new BigDecimal(jsonObject.get("low").toString());
		this.vol = new BigDecimal(jsonObject.get("vol").toString());
		this.last = new BigDecimal(jsonObject.get("last").toString());
		this.buy = new BigDecimal(jsonObject.get("buy").toString());
		this.sell = new BigDecimal(jsonObject.get("sell").toString());
		this.date = new BigDecimal(jsonObject.get("date").toString());
	}
	
	public BigDecimal getHigh() {
		return high;
	}

	public BigDecimal getLow() {
		return low;
	}

	public BigDecimal getVol() {
		return vol;
	}

	public BigDecimal getLast() {
		return last;
	}

	public BigDecimal getBuy() {
		return buy;
	}

	public BigDecimal getSell() {
		return sell;
	}

	public BigDecimal getDate() {
		return date;
	}

	@Override
	public String toString() {
		return "Ticker [high=" + high + ", low=" + low + ", vol=" + vol
				+ ", last=" + last + ", buy=" + buy + ", sell=" + sell
				+ ", date=" + date + "]";
	}
	
}
