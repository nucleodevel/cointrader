package net.trader.beans;

import java.math.BigDecimal;

public class Operation extends Record {

	protected BigDecimal rate;

	public Operation(
		Coin coin, Currency currency, RecordSide side,
		BigDecimal coinAmount, BigDecimal currencyPrice
	) {
		super(coin, currency, side, coinAmount, currencyPrice);
	}
	
	public Operation(Operation another) {
		super(
			another.getCoin(), another.getCurrency(), another.side, 
			another.coinAmount, another.currencyPrice
		);
		this.rate = another.getRate();
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}
	
}
