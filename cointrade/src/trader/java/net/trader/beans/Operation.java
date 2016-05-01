package net.trader.beans;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Operation extends Record {

	protected BigDecimal rate;

	public Operation() {
		super();
	}

	public Operation(BigInteger id) {
		super(id);
	}

	public Operation(
		String coin, String currency, RecordSide side,
		BigDecimal coinAmount, BigDecimal currencyPrice
	) {
		super(coin, currency, side, coinAmount, currencyPrice);
	}
	
	public Operation(Operation another) {
		this.coinAmount = another.getCoinAmount();
		this.currencyPrice = another.getCurrencyPrice();
		this.side = another.getSide();
		this.rate = another.getRate();		
		this.creationDate = another.getCreationDate();
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}
	
}
