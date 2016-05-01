package net.trader.beans;

public enum RecordSide {
	BUY("BUY"),
	SELL("SELL");
	private final String value;

	private RecordSide(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}