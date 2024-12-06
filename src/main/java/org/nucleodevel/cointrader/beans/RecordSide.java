package org.nucleodevel.cointrader.beans;

import java.math.BigDecimal;

public enum RecordSide {
	BUY("BUY", BigDecimal.valueOf(-1)), SELL("SELL", BigDecimal.valueOf(1));

	private final String value;
	private final BigDecimal multiplierFactorForRates;

	private RecordSide(String value, BigDecimal multiplierFactorForRates) {
		this.value = value;
		this.multiplierFactorForRates = multiplierFactorForRates;
	}

	public String getValue() {
		return this.value;
	}

	public BigDecimal getMultiplierFactorForRates() {
		return multiplierFactorForRates;
	}

	public RecordSide getOther() {
		if (this == BUY)
			return SELL;
		else if (this == SELL)
			return BUY;
		return null;
	}
}