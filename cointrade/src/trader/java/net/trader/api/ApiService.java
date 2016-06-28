package net.trader.api;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.trader.beans.Balance;
import net.trader.beans.Coin;
import net.trader.beans.Currency;
import net.trader.beans.Operation;
import net.trader.beans.Order;
import net.trader.beans.OrderBook;
import net.trader.beans.Ticker;
import net.trader.beans.UserConfiguration;
import net.trader.exception.ApiProviderException;

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
	
	public Ticker getTicker() throws ApiProviderException {
		Ticker ticker = new Ticker(getCoin(), getCurrency());
		
		BigDecimal high = new BigDecimal(0);
		BigDecimal low = new BigDecimal(Double.MAX_VALUE);
		BigDecimal vol = new BigDecimal(0);
		
		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		
		from.setTime(new Date());
		from.add(Calendar.HOUR, -24);
		to.setTime(new Date());
		
		List<Operation> operations = getOperationList(from, to);
		
		for (Operation operation: operations) {
			vol = vol.add(operation.getCoinAmount());
			if (operation.getCurrencyPrice().compareTo(high) == 1)
				high = operation.getCurrencyPrice();
			if (operation.getCurrencyPrice().compareTo(low) == -1)
				low = operation.getCurrencyPrice();
		}
		
		ticker.setHigh(high);
		ticker.setLow(low);
		ticker.setVol(vol);

		from.setTime(new Date());
		from.add(Calendar.HOUR, -3);
		to.setTime(new Date());
		BigDecimal last3HourVolume = new BigDecimal(0);
		List<Operation> last3HourOperations = getOperationList(from, to);
		
		for (Operation operation: last3HourOperations) 
			last3HourVolume = last3HourVolume.add(operation.getCoinAmount());
		
		ticker.setLast3HourVolume(last3HourVolume);
		
		return ticker;
	}

	public abstract Balance getBalance() throws ApiProviderException;
	
	public abstract OrderBook getOrderBook() throws ApiProviderException;
	
	public abstract List<Operation> getOperationList(Calendar from, Calendar to) throws ApiProviderException;
	
	public abstract List<Order> getUserActiveOrders() throws ApiProviderException;
	
	public abstract List<Operation> getUserOperations() throws ApiProviderException;
	
	public abstract Order cancelOrder(Order order) throws ApiProviderException;
	
	public abstract Order createOrder(Order order) throws ApiProviderException;
	
}
