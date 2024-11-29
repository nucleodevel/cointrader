package org.nucleodevel.cointrader.beans;

public enum Provider {

	MERCADO_BITCOIN("MERCADO_BITCOIN"), FOXBIT("FOXBIT"), BLINKTRADE("BLINKTRADE"), POLONIEX("POLONIEX");

	private final String value;

	private Provider(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

}
