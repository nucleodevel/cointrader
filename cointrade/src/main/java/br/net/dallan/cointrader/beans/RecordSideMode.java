package br.net.dallan.cointrader.beans;

public enum RecordSideMode {
	NONE("NONE"),
	ORDERS("ORDERS"),
	OPERATIONS("OPERATIONS"),
	OTHER_ORDERS("OTHER_ORDERS"),
	OTHER_OPERATIONS("OTHER_OPERATIONS");
	private final String value;

	private RecordSideMode(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}