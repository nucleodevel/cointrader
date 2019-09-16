package org.nucleodevel.cointrader.api;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.nucleodevel.cointrader.beans.Balance;
import org.nucleodevel.cointrader.beans.Broker;
import org.nucleodevel.cointrader.beans.Coin;
import org.nucleodevel.cointrader.beans.CoinCurrencyPair;
import org.nucleodevel.cointrader.beans.Currency;
import org.nucleodevel.cointrader.beans.Operation;
import org.nucleodevel.cointrader.beans.Order;
import org.nucleodevel.cointrader.beans.OrderBook;
import org.nucleodevel.cointrader.beans.Provider;
import org.nucleodevel.cointrader.beans.Ticker;
import org.nucleodevel.cointrader.exception.ApiProviderException;

public abstract class ApiService {
	
	protected CoinCurrencyPair coinCurrencyPair;

	protected Provider provider;
	protected Broker broker;
	protected String key;
	protected String secret;
	
	// --------------------- Constructors
	
	public ApiService(CoinCurrencyPair coinCurrencyPair, Provider provider, Broker broker, String key, String secret) 
		throws ApiProviderException {
		
		this.coinCurrencyPair = coinCurrencyPair;
		this.provider = provider;
		this.broker = broker;
		this.key = key;
		this.secret = secret;
		
		makeActionInConstructor();
	}
	
	protected Coin getCoin() {
		return coinCurrencyPair.getCoin();
	}
	
	protected Currency getCurrency() {
		return coinCurrencyPair.getCurrency();
	}
	
	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public Broker getBroker() {
		return broker;
	}

	public void setBroker(Broker broker) {
		this.broker = broker;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
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
