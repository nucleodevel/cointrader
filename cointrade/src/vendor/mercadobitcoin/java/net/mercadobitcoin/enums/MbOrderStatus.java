package net.mercadobitcoin.enums;


public enum MbOrderStatus implements EnumValue {
	ACTIVE("active"),
	CANCELED("canceled"),
	COMPLETED("completed");
	private final String value;

	private MbOrderStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}