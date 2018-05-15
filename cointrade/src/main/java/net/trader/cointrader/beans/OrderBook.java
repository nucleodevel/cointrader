package net.trader.cointrader.beans;

import java.util.ArrayList;
import java.util.List;

public class OrderBook {
	
	private CoinCurrencyPair coinCurrencyPair;
	private List<Order> bidOrders;
	private List<Order> askOrders;
	
	public OrderBook(Coin coin, Currency currency) {
		this.coinCurrencyPair = new CoinCurrencyPair(coin, currency);
		this.bidOrders = new ArrayList<Order>();
		this.askOrders = new ArrayList<Order>();
	}

	public CoinCurrencyPair getCoinCurrencyPair() {
		return coinCurrencyPair;
	}

	public void setCoinCurrencyPair(CoinCurrencyPair coinCurrencyPair) {
		this.coinCurrencyPair = coinCurrencyPair;
	}
	
	public Coin getCoin() {
		return coinCurrencyPair.getCoin();
	}

	public void setCoin(Coin coin) {
		coinCurrencyPair.setCoin(coin);
	}

	public Currency getCurrency() {
		return coinCurrencyPair.getCurrency();
	}

	public void setCurrency(Currency currency) {
		coinCurrencyPair.setCurrency(currency);
	}

	public List<Order> getBidOrders() {
		return bidOrders;
	}

	public void setBidOrders(List<Order> bidOrders) {
		this.bidOrders = bidOrders;
	}

	public List<Order> getAskOrders() {
		return askOrders;
	}

	public void setAskOrders(List<Order> askOrders) {
		this.askOrders = askOrders;
	}

}
