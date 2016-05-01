package net.trader.beans;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Order extends Record {

	public Order() {
		super();
	}

	public Order(BigInteger id) {
		super(id);
	}

	public Order(
		String coin, String currency, RecordSide side,
		BigDecimal coinAmount, BigDecimal currencyPrice
	) {
		super(coin, currency, side, coinAmount, currencyPrice);
	}

}
