package net.trader.beans;

import java.util.List;

public abstract class OrderBook {
	
	public abstract List<Order> getBids();
	
	public abstract List<Order> getAsks();

}
