package net.trader.beans;

public enum Broker {

	MERCADO_BITCOIN("MERCADO_BITCOIN"),
	FOXBIT("FOXBIT"),
	POLONIEX("POLONIEX");
	
	private final String value;

	private Broker(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

}
