package net.trader.api;

import java.util.List;

import net.trader.beans.Balance;
import net.trader.beans.Operation;
import net.trader.beans.Order;
import net.trader.beans.OrderBook;
import net.trader.beans.Ticker;
import net.trader.beans.UserConfiguration;
import net.trader.exception.ApiProviderException;

public abstract class ApiService {
	
	protected UserConfiguration userConfiguration;
	
	public ApiService(UserConfiguration userConfiguration) {
		this.userConfiguration = userConfiguration;
	}

	public abstract Ticker getTicker() throws ApiProviderException;

	public abstract Balance getBalance() throws ApiProviderException;
	
	public abstract OrderBook getOrderBook() throws ApiProviderException;
	
	public abstract List<Order> getUserActiveOrders() throws ApiProviderException;
	
	public abstract List<Order> getUserCanceledOrders() throws ApiProviderException;
	
	public abstract List<Order> getUserCompletedOrders() throws ApiProviderException;
	
	public abstract List<Operation> getUserOperations() throws ApiProviderException;
	
	public abstract Order cancelOrder(Order order) throws ApiProviderException;
	
	public abstract Order createBuyOrder(Order order) throws ApiProviderException;
	
	public abstract Order createSellOrder(Order order) throws ApiProviderException;
	
}
