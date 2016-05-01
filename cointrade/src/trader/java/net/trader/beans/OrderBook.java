package net.trader.beans;

import java.util.ArrayList;
import java.util.List;

public class OrderBook {
	
	protected String coin;
	protected String currency;
	protected List<Order> bidOrders;
	protected List<Order> askOrders;
	
	public OrderBook(String coin, String currency) {
		this.coin = coin;
		this.currency = currency;
		this.bidOrders = new ArrayList<Order>();
		this.askOrders = new ArrayList<Order>();
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
