package net.trader.beans;

public enum Provider {

	MERCADO_BITCOIN("MERCADO_BITCOIN"),
	BLINKTRADE("BLINKTRADE");
	
	private final String value;

	private Provider(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

}
