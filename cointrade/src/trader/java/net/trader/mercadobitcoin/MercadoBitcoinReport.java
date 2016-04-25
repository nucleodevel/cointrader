package net.trader.mercadobitcoin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.mercadobitcoin.tradeapi.service.ApiService;
import net.mercadobitcoin.tradeapi.service.TradeApiService;
import net.mercadobitcoin.tradeapi.to.MbOperation;
import net.mercadobitcoin.tradeapi.to.MbOrder;
import net.mercadobitcoin.tradeapi.to.MbOrder.OrderStatus;
import net.mercadobitcoin.tradeapi.to.OrderFilter;
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
	
	private ApiService apiService;
	private TradeApiService tradeApiService;
	
	private Ticker ticker24h;
	
	protected static List<Order> myCanceledOrders;
	
	public MercadoBitcoinReport(UserConfiguration userConfiguration, String currency, String coin) {
		super(userConfiguration, currency, coin);
	}
	
	@Override
	public Balance getBalance() throws ApiProviderException {
		if (balance == null)
			balance = getTradeApiService().getBalance(getCurrency(), getCoin());
		return balance;
	}

	@Override
	public OrderBook getOrderBook() throws ApiProviderException {
		if (orderBook == null)
			orderBook = getApiService().getOrderBook(getCoin(), getCurrency());
		return orderBook;
	}
	
	@Override
	public List<Order> getMyCanceledOrders() throws ApiProviderException {
		long now = (new Date()).getTime() / 1000;
		
		OrderFilter orderFilter = new OrderFilter(getCoin(), getCurrency());
		orderFilter.setStatus(OrderStatus.CANCELED);
		
		if (myCanceledOrders == null) {
			myCanceledOrders = new ArrayList<Order>();
			
			for (long time = now; time > now - totalTimeToReadMyCanceledOrders; time -= intervalToReadMyCanceledOrders) {
				orderFilter.setSince(time - intervalToReadMyCanceledOrders);
				orderFilter.setEnd(time - 1);
				List<Order> orders = getTradeApiService().listOrders(orderFilter);
				for (Order o: orders) {
					MbOrder order = (MbOrder) o;
					if (order.getOperations() != null && order.getOperations().size() > 0)
						myCanceledOrders.add(order);
				}
			}
			Collections.sort(myCanceledOrders);
		}
		else {
			orderFilter.setSince(lastTimeByReadingMyCanceledOrders + 1);
			orderFilter.setEnd(now);
			
			List<Order> orders = getTradeApiService().listOrders(orderFilter);
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
			OrderFilter orderFilter = new OrderFilter(getCoin(), getCurrency());
			orderFilter.setStatus(OrderStatus.COMPLETED);
			orderFilter.setSince(now - totalTimeToReadMyCompletedOrders);
			orderFilter.setEnd(now);
			
			myCompletedOrders = getTradeApiService().listOrders(orderFilter);
			Collections.sort(myCompletedOrders);
			
		}
		return myCompletedOrders;
	}

	@Override
	public List<Order> getMyActiveOrders() throws ApiProviderException {
		if (myActiveOrders == null) {
			OrderFilter orderFilter = new OrderFilter(getCoin(), getCurrency());			
			orderFilter.setStatus(OrderStatus.ACTIVE);
			myActiveOrders = getTradeApiService().listOrders(orderFilter);
			Collections.sort(myActiveOrders);
		}
		return myActiveOrders;
	}

	@Override
	public List<Operation> getMyOperations() throws ApiProviderException {
		if (myOperations == null) {
			myOperations = new ArrayList<Operation>();
			for (Order o: getMyOrders()) {
				MbOrder order = (MbOrder) o;
				if (order.getOperations() != null)
					for (MbOperation operation: order.getOperations()) {
						operation.setSide(order.getSide());
						myOperations.add(operation);
					}
			}
		}
		return myOperations;
	}
	
	@Override
	public void cancelOrder(Order order) throws ApiProviderException {
		getTradeApiService().cancelOrder((MbOrder) order);
	}
	
	@Override
	public void createBuyOrder(BigDecimal coinAmount, BigDecimal currencyPrice) throws ApiProviderException {
		getTradeApiService().createBuyOrder(getCoin(), getCurrency(), coinAmount, currencyPrice);
	}

	@Override
	public void createSellOrder(BigDecimal coinAmount, BigDecimal currencyPrice) throws ApiProviderException {
		getTradeApiService().createSellOrder(getCoin(), getCurrency(), coinAmount, currencyPrice);
	}

	private ApiService getApiService() throws ApiProviderException {
		if (apiService == null)
			apiService = new ApiService();
		return apiService;
	}
	
	private TradeApiService getTradeApiService() throws ApiProviderException {
		if (tradeApiService == null)
			tradeApiService = new TradeApiService(
				getUserConfiguration().getSecret(), getUserConfiguration().getKey()
			);
		return tradeApiService;
	}

	public Ticker getTicker24h() throws ApiProviderException {
		if (ticker24h == null)
			ticker24h = getApiService().ticker24h(getCoin(), getCurrency());
		return ticker24h;
	}

}