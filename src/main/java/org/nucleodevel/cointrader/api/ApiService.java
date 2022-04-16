package org.nucleodevel.cointrader.api;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.nucleodevel.cointrader.beans.Balance;
import org.nucleodevel.cointrader.beans.Coin;
import org.nucleodevel.cointrader.beans.Currency;
import org.nucleodevel.cointrader.beans.Operation;
import org.nucleodevel.cointrader.beans.Order;
import org.nucleodevel.cointrader.beans.OrderBook;
import org.nucleodevel.cointrader.beans.Ticker;
import org.nucleodevel.cointrader.beans.UserConfiguration;
import org.nucleodevel.cointrader.exception.ApiProviderException;

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
