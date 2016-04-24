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
import br.eti.claudiney.blinktrade.api.BlinktradeAPI;
import br.eti.claudiney.blinktrade.enums.BlinktradeBroker;
import br.eti.claudiney.blinktrade.enums.BlinktradeSymbol;

public class BlinktradeReport extends RobotReport {
	
	private BlinktradeAPI api;
	
	public BlinktradeReport(UserConfiguration userConfiguration, String currency, String coin) {
		super(userConfiguration, currency, coin);
	}
	
	@Override
	public Balance getBalance() throws ApiProviderException {
		if (balance == null)
			balance = getApi().getBalance(getCurrency(), getCoin());
		return balance;
	}
	
	public OrderBook getOrderBook() throws ApiProviderException {
		if (orderBook == null)
			orderBook = getApi().getOrderBook();
		return orderBook;
	}

	@Override
	public List<Order> getMyCanceledOrders() throws ApiProviderException {
		return null;
	}

	@Override
	public List<Order> getMyCompletedOrders() throws ApiProviderException {
		if (myCompletedOrders == null)
			myCompletedOrders = getApi().getClientCompletedOrders();
		return myCompletedOrders;
	}

	@Override
	public List<Order> getMyActiveOrders() throws ApiProviderException {
		if (myActiveOrders == null)
			myActiveOrders = getApi().getClientActiveOrders();
		return myActiveOrders;
		
	}

	@Override
	public List<Operation> getMyOperations() throws ApiProviderException {
		if (myOperations == null)
			myOperations = getApi().getClientOperations();
		return myOperations;
	}
	
	@Override
	public void cancelOrder(Order order) throws ApiProviderException {
		getApi().cancelOrder(order);
	}
	
	@Override
	public void createBuyOrder(BigDecimal currency, BigDecimal coin) throws ApiProviderException {
		getApi().createBuyOrder(getCoinPair(), currency, coin);
	}
	
	@Override
	public void createSellOrder(BigDecimal currency, BigDecimal coin) throws ApiProviderException {
		getApi().createSellOrder(getCoinPair(), currency, coin);
	}
	
	public BlinktradeSymbol getCoinPair() {
		return getCurrency().equals("BRL") && getCoin().equals("BTC")?
			BlinktradeSymbol.BTCBRL: null;
	}
	
	private BlinktradeAPI getApi() throws ApiProviderException {
		if (api == null) {
			BlinktradeBroker broker = getUserConfiguration().getBroker().equals("Foxbit")?
				BlinktradeBroker.FOXBIT: null;
			api = new BlinktradeAPI(
				getUserConfiguration().getKey(), getUserConfiguration().getSecret(), broker
				
			);
		}
		return api;
	}

}