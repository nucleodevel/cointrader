package net.trader.beans;

public class CoinCurrencyPair {
	
	protected Coin coin;
	protected Currency currency;

	public CoinCurrencyPair() {
		
	}

	public CoinCurrencyPair(Coin coin, Currency currency) {
		this.coin = coin;
		this.currency = currency;
	}
	
	public Coin getCoin() {
		return coin;
	}

	public void setCoin(Coin coin) {
		this.coin = coin;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

}
