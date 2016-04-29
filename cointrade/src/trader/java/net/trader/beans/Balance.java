package net.trader.beans;

import java.math.BigDecimal;

import net.trader.exception.ApiProviderException;

public abstract class Balance {

	protected String coin;
	protected String currency;
	
	public Balance(String coin, String currency) {
		this.coin = coin;
	}

	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public abstract BigDecimal getCoinAmount() throws ApiProviderException;

	public abstract BigDecimal getCurrencyAmount() throws ApiProviderException;

}
