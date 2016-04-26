package net.trader.api;

import java.util.List;

import net.trader.beans.Balance;
import net.trader.beans.Order;
import net.trader.beans.OrderBook;
import net.trader.exception.ApiProviderException;

public abstract class ApiService {

	public abstract Balance getBalance(String currency, String coin) throws ApiProviderException;
	
	public abstract OrderBook getOrderBook(String coin, String currency) throws ApiProviderException;
	
	public abstract List<Order> getUserCompletedOrders() throws ApiProviderException;
	
}
