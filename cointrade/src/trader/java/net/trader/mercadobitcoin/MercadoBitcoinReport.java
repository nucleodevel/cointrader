package net.trader.mercadobitcoin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.mercadobitcoin.tradeapi.service.MbApiService;
import net.mercadobitcoin.tradeapi.to.MbOrder;
import net.mercadobitcoin.tradeapi.to.Ticker;
import net.trader.beans.Balance;
import net.trader.beans.Operation;
import net.trader.beans.Order;
import net.trader.beans.OrderBook;
import net.trader.exception.ApiProviderException;
import net.trader.robot.RobotReport;
import net.trader.robot.UserConfiguration;

public class MercadoBitcoinReport extends RobotReport {

	private static long intervalToReadMyCanceledOrders = 1200;
	private static long totalTimeToReadMyCanceledOrders = 86400;
	private static long lastTimeByReadingMyCanceledOrders = 0;
	private static long totalTimeToReadMyCompletedOrders = 43200;
	
	private MbApiService apiService;
	
	private Ticker ticker24h;
	
	protected static List<Order> myCanceledOrders;
	
	public MercadoBitcoinReport(UserConfiguration userConfiguration, String coin, String currency) {
		super(userConfiguration, coin, currency);
	}
	
	@Override
	public Balance getBalance() throws ApiProviderException {
		if (balance == null)
			balance = getApiService().getBalance(getCurrency(), getCoin());
		return balance;
	}

	@Override
	public OrderBook getOrderBook() throws ApiProviderException {
		if (orderBook == null)
			orderBook = getApiService().getOrderBook(getCoin(), getCurrency());
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
		long now = (new Date()).getTime() / 1000;
		
		
		
		if (myCanceledOrders == null) {
			myCanceledOrders = new ArrayList<Order>();
			
			for (long time = now; time > now - totalTimeToReadMyCanceledOrders; time -= intervalToReadMyCanceledOrders) {
				Long since = time - intervalToReadMyCanceledOrders;
				Long end = time - 1;
				List<Order> orders = getApiService().getUserCanceledOrders(getCoin(), getCurrency(), since, end);
				for (Order o: orders) {
					MbOrder order = (MbOrder) o;
					if (order.getOperations() != null && order.getOperations().size() > 0)
						myCanceledOrders.add(order);
				}
			}
			Collections.sort(myCanceledOrders);
		}
		else {
			Long since = lastTimeByReadingMyCanceledOrders + 1;
			Long end = now;
			
			List<Order> orders = getApiService().getUserCanceledOrders(getCoin(), getCurrency(), since, end);
			int i = 0;
			for (Order o: orders) {
				MbOrder order = (MbOrder) o;
				if (order.getOperations() != null && order.getOperations().size() > 0) {
					myCanceledOrders.add(i, order);
					i++;
				}
			}
		}
		lastTimeByReadingMyCanceledOrders = now;
		
		return myCanceledOrders;
	}
	
	@Override
	public List<Order> getMyCompletedOrders() throws ApiProviderException {
		if (myCompletedOrders == null) {
			
			// lê uma semana de ordens completas
			long now = (new Date()).getTime() / 1000;
			Long since = now - totalTimeToReadMyCompletedOrders;
			Long end = now;
			
			myCompletedOrders = getApiService().getUserCompletedOrders(
				getCoin(), getCurrency(), since, end
			);
			
		}
		return myCompletedOrders;
	}

	@Override
	public List<Operation> getMyOperations() throws ApiProviderException {
		if (myOperations == null) {
			myOperations = getApiService().getUserOperations(
				getCoin(), getCurrency(), null, null
			);
		}
		return myOperations;
	}
	
	@Override
	public void cancelOrder(Order order) throws ApiProviderException {
		getApiService().cancelOrder((MbOrder) order);
	}
	
	@Override
	public void createBuyOrder(BigDecimal coinAmount, BigDecimal currencyPrice) throws ApiProviderException {
		getApiService().createBuyOrder(getCoin(), getCurrency(), coinAmount, currencyPrice);
	}

	@Override
	public void createSellOrder(BigDecimal coinAmount, BigDecimal currencyPrice) throws ApiProviderException {
		getApiService().createSellOrder(getCoin(), getCurrency(), coinAmount, currencyPrice);
	}
	
	private MbApiService getApiService() throws ApiProviderException {
		if (apiService == null)
			apiService = new MbApiService(
				getUserConfiguration().getSecret(), getUserConfiguration().getKey()
			);
		return apiService;
	}

	public Ticker getTicker24h() throws ApiProviderException {
		if (ticker24h == null)
			ticker24h = getApiService().ticker24h(getCoin(), getCurrency());
		return ticker24h;
	}

}