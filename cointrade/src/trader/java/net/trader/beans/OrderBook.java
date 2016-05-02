package net.trader.beans;

import java.util.ArrayList;
import java.util.List;

public class OrderBook {
	
	protected Coin coin;
	protected Currency currency;
	protected List<Order> bidOrders;
	protected List<Order> askOrders;
	
	public OrderBook(Coin coin, Currency currency) {
		this.coin = coin;
		this.currency = currency;
		this.bidOrders = new ArrayList<Order>();
		this.askOrders = new ArrayList<Order>();
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
