package net.trader.beans;

import java.math.BigDecimal;

import net.trader.exception.ApiProviderException;

public abstract class Balance {

	protected String currency;
	protected String coin;
	
	public Balance(String currency, String coin) {
		this.currency = currency;
		this.coin = coin;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

	public abstract BigDecimal getCurrencyAmount() throws ApiProviderException;
	
	public abstract BigDecimal getCoinAmount() throws ApiProviderException;

}
