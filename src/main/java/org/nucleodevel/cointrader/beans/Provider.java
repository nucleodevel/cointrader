package org.nucleodevel.cointrader.beans;

import org.nucleodevel.cointrader.api.AbstractApiService;
import org.nucleodevel.cointrader.api.foxbit.FoxbitApiService;
import org.nucleodevel.cointrader.api.mercadobitcoin.MercadoBitcoinApiService;
import org.nucleodevel.cointrader.api.poloniex.PoloniexApiService;

public enum Provider {

	FOXBIT("FOXBIT", FoxbitApiService.class), MERCADO_BITCOIN("MERCADO_BITCOIN", MercadoBitcoinApiService.class),
	POLONIEX("POLONIEX", PoloniexApiService.class);

	private final String value;
	private final Class<? extends AbstractApiService> implementer;

	private Provider(String value, Class<? extends AbstractApiService> implementer) {
		this.value = value;
		this.implementer = implementer;
	}

	public String getValue() {
		return this.value;
	}

	public Class<? extends AbstractApiService> getImplementer() {
		return implementer;
	}

}
