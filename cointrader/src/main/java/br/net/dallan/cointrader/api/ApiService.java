package br.net.dallan.cointrader.api;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import br.net.dallan.cointrader.beans.Balance;
import br.net.dallan.cointrader.beans.Coin;
import br.net.dallan.cointrader.beans.Currency;
import br.net.dallan.cointrader.beans.Operation;
import br.net.dallan.cointrader.beans.Order;
import br.net.dallan.cointrader.beans.OrderBook;
import br.net.dallan.cointrader.beans.Ticker;
import br.net.dallan.cointrader.beans.UserConfiguration;
import br.net.dallan.cointrader.exception.ApiProviderException;

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
