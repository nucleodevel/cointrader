package net.trader.beans;

public enum OrderType {
	
	MARKET("MARKET"),
	LIMITED("LIMITED");
	
	private final String value;

	private OrderType(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

}
