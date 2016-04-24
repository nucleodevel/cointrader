package net.trader.beans;

public enum OrderSide implements EnumValue {
	BUY("buy"),
	SELL("sell");
	private final String value;

	private OrderSide(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}