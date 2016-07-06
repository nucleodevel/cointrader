package net.trader.beans;

public enum Coin {
	BTC("BTC"),
	LTC("LTC"),
	ETH("ETH");
	private final String value;

	private Coin(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}