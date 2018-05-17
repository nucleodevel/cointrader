package org.nucleodev.cointrader.beans;

public enum Coin {
	AMP("AMP"),
	BTC("BTC"),
	BURST("BURST"),
	CLAM("CLAM"),
	CURE("CURE"),
	DASH("DASH"),
	EMC2("EMC2"),
	ETC("ETC"),
	ETH("ETH"),
	FCT("FCT"),
	FLDC("FLDC"),
	GRC("GRC"),
	LBC("LBC"),
	LTBC("LTBC"),
	LTC("LTC"),
	MAID("MAID"),
	NAV("NAV"),
	NEOS("NEOS"),
	NOBL("NOBL"),
	NSR("NSR"),
	SC("SC"),
	SDC("SDC"),
	XMR("XMR"),
	XRP("XRP");
	private final String value;

	private Coin(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}