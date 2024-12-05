package org.nucleodevel.cointrader.beans;

import org.nucleodevel.cointrader.api.foxbit.FoxbitApiService;
import org.nucleodevel.cointrader.api.mercadobitcoin.MercadoBitcoinApiService;
import org.nucleodevel.cointrader.api.poloniex.PoloniexApiService;

public enum Provider {

	FOXBIT("FOXBIT", FoxbitApiService.class.getCanonicalName()),
	MERCADO_BITCOIN("MERCADO_BITCOIN", MercadoBitcoinApiService.class.getCanonicalName()),
	POLONIEX("POLONIEX", PoloniexApiService.class.getCanonicalName());

	private final String value;
	private final String implementer;

	private Provider(String value, String implementer) {
		this.value = value;
		this.implementer = implementer;
	}

	public String getValue() {
		return this.value;
	}

	public String getImplementer() {
		return implementer;
	}

}
