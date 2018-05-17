package org.nucleodev.cointrader.beans;

import java.math.BigDecimal;

public class Ticker {
	
	private CoinCurrencyPair coinCurrencyPair;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal vol;
	private BigDecimal last3HourVolume;
	
	public Ticker(Coin coin, Currency currency) {
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

	public BigDecimal getLast3HourVolume() {
		return last3HourVolume;
	}

	public void setLast3HourVolume(BigDecimal last3HourVolume) {
		this.last3HourVolume = last3HourVolume;
	}

	@Override
	public String toString() {
		return "Ticker [high=" + high + ", low=" + low + ", vol=" + vol + "]";
	}

}
