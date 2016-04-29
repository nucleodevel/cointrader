package net.trader.blinktrade;

import java.math.BigDecimal;
import java.util.List;

import net.trader.beans.Balance;
import net.trader.beans.Operation;
import net.trader.beans.Order;
import net.trader.beans.OrderBook;
import net.trader.exception.ApiProviderException;
import net.trader.robot.RobotReport;
import net.trader.robot.UserConfiguration;
import br.eti.claudiney.blinktrade.api.BtApiService;
import br.eti.claudiney.blinktrade.enums.BlinktradeBroker;

public class BlinktradeReport extends RobotReport {
	
	private BtApiService apiService;
	
	public BlinktradeReport(UserConfiguration userConfiguration, String coin, String currency) {
		super(userConfiguration, coin, currency);
	}
	
	@Override
	public Balance getBalance() throws ApiProviderException {
		if (balance == null)
			balance = getApiService().getBalance(getCurrency(), getCoin());
		return balance;
	}
	
	public OrderBook getOrderBook() throws ApiProviderException {
		if (orderBook == null)
			orderBook = getApiService().getOrderBook(getCurrency(), getCoin());
		return orderBook;
	}

	@Override
	public List<Order> getMyActiveOrders() throws ApiProviderException {
		if (myActiveOrders == null)
			myActiveOrders = getApiService().getUserActiveOrders(getCoin(), getCurrency());
		return myActiveOrders;
		
	}

	@Override
	public List<Order> getMyCanceledOrders() throws ApiProviderException {
		return null;
	}

	@Override
	public List<Order> getMyCompletedOrders() throws ApiProviderException {
		if (myCompletedOrders == null)
			myCompletedOrders = getApiService().getUserCompletedOrders(getCoin(), getCurrency(), null, null);
		return myCompletedOrders;
	}

	@Override
	public List<Operation> getMyOperations() throws ApiProviderException {
		if (myOperations == null)
			myOperations = getApiService().getUserOperations(getCoin(), getCurrency(), null, null);
		return myOperations;
	}
	
	@Override
	public void cancelOrder(Order order) throws ApiProviderException {
		getApiService().cancelOrder(order);
	}
	
	@Override
	public void createBuyOrder(BigDecimal coinAmount, BigDecimal currencyPrice) throws ApiProviderException {
		getApiService().createBuyOrder(getCoin(), getCurrency(), coinAmount, currencyPrice);
	}
	
	@Override
	public void createSellOrder(BigDecimal coinAmount, BigDecimal currencyPrice) throws ApiProviderException {
		getApiService().createSellOrder(getCoin(), getCurrency(), coinAmount, currencyPrice);
	}
	
	private BtApiService getApiService() throws ApiProviderException {
		if (apiService == null) {
			BlinktradeBroker broker = getUserConfiguration().getBroker().equals("Foxbit")?
				BlinktradeBroker.FOXBIT: null;
			apiService = new BtApiService(
				getUserConfiguration().getKey(), getUserConfiguration().getSecret(), broker
				
			);
		}
		return apiService;
	}

}