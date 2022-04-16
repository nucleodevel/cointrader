package org.nucleodevel.cointrader.beans;

public enum OrderStatus {
	ACTIVE("ACTIVE"), CANCELED("CANCELED"), COMPLETED("COMPLETED");

	private final String value;

	private OrderStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}