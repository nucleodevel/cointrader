package net.trader.beans;

public enum OrderSide {
	BUY("BUY"),
	SELL("SELL");
	private final String value;

	private OrderSide(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}