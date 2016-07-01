package net.trader.beans;

public enum Currency {
	BRL("BRL"),
	USDT("USDT");
	private final String value;

	private Currency(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}