package net.trader.cointrader.api;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import net.trader.cointrader.beans.Balance;
import net.trader.cointrader.beans.Coin;
import net.trader.cointrader.beans.Currency;
import net.trader.cointrader.beans.Operation;
import net.trader.cointrader.beans.Order;
import net.trader.cointrader.beans.OrderBook;
import net.trader.cointrader.beans.Ticker;
import net.trader.cointrader.beans.UserConfiguration;
import net.trader.cointrader.exception.ApiProviderException;

public abstract class ApiService {
	
	protected UserConfiguration userConfiguration;
	
	// --------------------- Constructors
	
	public ApiService(UserConfiguration userConfiguration) throws ApiProviderException {
		this.userConfiguration = userConfiguration;
		makeActionInConstructor();
	}
	
	protected Coin getCoin() {
		return userConfiguration.getCoin();
	}
	
	protected Currency getCurrency() {
		return userConfiguration.getCurrency();
	}
	
	protected abstract String getDomain();
	
	protected abstract String getPublicApiUrl();
	
	protected abstract String getPrivateApiUrl();
	
	protected abstract String getPublicApiPath();
	
	protected abstract String getPrivateApiPath();
	
	protected abstract void makeActionInConstructor() throws ApiProviderException;
	
	public abstract Ticker getTicker() throws ApiProviderException;

	public abstract Balance getBalance() throws ApiProviderException;
	
	public abstract OrderBook getOrderBook() throws ApiProviderException;
	
	public abstract List<Operation> getOperationList(Calendar from, Calendar to) throws ApiProviderException;
	
	public abstract List<Order> getUserActiveOrders() throws ApiProviderException;
	
	public abstract List<Operation> getUserOperations() throws ApiProviderException;
	
	public abstract Order cancelOrder(Order order) throws ApiProviderException;
	
	public abstract Order createOrder(Order order) throws ApiProviderException;
	
	public abstract TimeZone getTimeZone();
	
}
