package org.nucleodevel.cointrader.beans;

public enum Currency {
	BRL("BRL"), USDT("USDT"), BTC("BTC");

	private final String value;

	private Currency(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}