package org.nucleodevel.cointrader.beans;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum RecordSide {
	BUY("BUY", BigDecimal.valueOf(-1)), SELL("SELL", BigDecimal.valueOf(1));

	private final String value;
	private final BigDecimal multiplierFactor;

	private RecordSide(String value, BigDecimal multiplierFactor) {
		this.value = value;
		this.multiplierFactor = multiplierFactor;
	}

	public String getValue() {
		return this.value;
	}

	public BigDecimal getMultiplierFactor() {
		return multiplierFactor;
	}

	public RecordSide getOther() {
		return this == BUY ? SELL : (this == SELL ? BUY : null);
	}

	public boolean isAGoodRecordByRecordCurrencyPriceAndLastRelevantPrice(BigDecimal recordCurrencyPrice,
			BigDecimal lastRelevantPrice) {
		return this == BUY ? recordCurrencyPrice.compareTo(lastRelevantPrice) < 0
				: (this == RecordSide.SELL ? recordCurrencyPrice.compareTo(lastRelevantPrice) > 0 : null);
	}

	public BigDecimal getEstimatedCoinAmountByAmountAndPrice(BigDecimal amount, BigDecimal price) {
		return this == RecordSide.BUY ? amount.divide(price, 8, RoundingMode.HALF_EVEN)
				: (this == RecordSide.SELL ? amount : null);
	}

}