package org.nucleodev.cointrader.beans;

import java.math.BigDecimal;

public class Operation extends Record {

	private BigDecimal rate;

	public Operation(
		Coin coin, Currency currency, RecordSide side,
		BigDecimal coinAmount, BigDecimal currencyPrice
	) {
		super(coin, currency, side, coinAmount, currencyPrice);
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}
	
}
