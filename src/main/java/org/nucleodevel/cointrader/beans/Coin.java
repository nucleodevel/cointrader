package org.nucleodevel.cointrader.beans;

public enum Coin {
	ADA("ADA"), ALGO("ALGO"), AMP("AMP"), ATOM("ATOM"), AVAX("AVAX"), BCH("BCH"), BTC("BTC"), BURST("BURST"),
	CLAM("CLAM"), CURE("CURE"), DASH("DASH"), DOGE("DOGE"), DOT("DOT"), EMC2("EMC2"), ETC("ETC"), ETH("ETH"),
	FCT("FCT"), FIL("FIL"), FLDC("FLDC"), GRC("GRC"), ICP("ICP"), LBC("LBC"), LTBC("LTBC"), LTC("LTC"), MAID("MAID"),
	NAV("NAV"), NEOS("NEOS"), NOBL("NOBL"), NSR("NSR"), PAXG("PAXG"), SC("SC"), SDC("SDC"), SHIB("SHIB"), SOL("SOL"),
	USDC("USDC"), XMR("XMR"), XRP("XRP");

	private final String value;

	private Coin(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return this.value;
	}
}