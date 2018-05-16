package br.net.dallan.cointrader.beans;

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
	
	public RecordSide getOther() {
		if (this == BUY)
			return SELL;
		else if (this == SELL)
			return BUY;
		return null;
	}
}