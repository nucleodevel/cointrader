package net.trader.robot;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.trader.beans.Balance;
import net.trader.beans.Operation;
import net.trader.beans.Order;
import net.trader.beans.OrderBook;
import net.trader.beans.OrderSide;
import net.trader.exception.ApiProviderException;

public abstract class RobotReport {
	
	protected static long numOfConsideredOrdersForLastRelevantSellPrice = 5;
	
	private UserConfiguration userConfiguration;
	private String coin;
	private String currency;

	protected Balance balance;
	protected OrderBook orderBook;
	
	protected List<Order> activeBuyOrders;
	protected List<Order> activeSellOrders;
	
	protected List<Operation> operations;
	
	protected Order currentTopBuy;
	protected Order currentTopSell;

	protected List<Order> myActiveBuyOrders;
	protected List<Order> myActiveSellOrders;
	
	protected List<Order> activeOrders;
	protected List<Order> completedOrders;
	
	protected Operation lastBuy;
	protected Operation lastSell;
	
	protected BigDecimal lastRelevantBuyPrice;
	protected BigDecimal lastRelevantSellPrice;
	
	protected List<Order> myOrders;
	protected List<Order> myCompletedOrders;
	protected List<Order> myActiveOrders;
	
	protected List<Operation> myOperations;
	
	public RobotReport(UserConfiguration userConfiguration, String coin, String currency) {
		this.userConfiguration = userConfiguration;
		this.coin = coin;
		this.currency = currency;
	}
	
	public UserConfiguration getUserConfiguration() {
		return userConfiguration;
	}

	public void setUserConfiguration(UserConfiguration userConfiguration) {
		this.userConfiguration = userConfiguration;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}
	
	public abstract Balance getBalance() throws ApiProviderException;
	
	public abstract OrderBook getOrderBook() throws ApiProviderException;
	
	public List<Order> getActiveOrders() throws ApiProviderException {
		if (activeOrders == null) {
			activeOrders = new ArrayList<Order>();
			activeOrders.addAll(getActiveBuyOrders());
			activeOrders.addAll(getActiveSellOrders());
			Collections.sort(activeOrders);
		}
		return activeOrders;
	}

	public List<Order> getActiveBuyOrders() throws ApiProviderException {
		if (activeBuyOrders == null) {
			activeBuyOrders = getOrderBook().getBids();
		}
		return activeBuyOrders;
	}

	public List<Order> getActiveSellOrders() throws ApiProviderException {
		if (activeSellOrders == null) {
			activeSellOrders = getOrderBook().getAsks();
		}
		return activeSellOrders;
	}

	public Order getCurrentTopBuy() throws ApiProviderException {
		if (currentTopBuy == null)
			currentTopBuy = getActiveBuyOrders().get(0);
		return currentTopBuy;
	}

	public Order getCurrentTopSell() throws ApiProviderException {
		if (currentTopSell == null)
			currentTopSell = getActiveSellOrders().get(0);
		return currentTopSell;
	}

	public List<Order> getMyOrders() throws ApiProviderException {
		if (myOrders == null) {
			myOrders = new ArrayList<Order>();
			myOrders.addAll(getMyActiveOrders());
			myOrders.addAll(getMyCompletedOrders());
			myOrders.addAll(getMyCanceledOrders());
			Collections.sort(myOrders);
		}
		return myOrders;
	}
	
	public abstract List<Order> getMyCanceledOrders() throws ApiProviderException;
	
	public abstract List<Order> getMyCompletedOrders() throws ApiProviderException;
	
	public abstract List<Order> getMyActiveOrders() throws ApiProviderException;

	public List<Order> getMyActiveBuyOrders() throws ApiProviderException {
		if (myActiveBuyOrders == null) {
			myActiveBuyOrders = new ArrayList<Order>();
			for (Order order: getMyActiveOrders())
				if (order.getSide() == OrderSide.BUY)
					myActiveBuyOrders.add(order);
		}
		return myActiveBuyOrders;
	}

	public List<Order> getMyActiveSellOrders() throws ApiProviderException {
		if (myActiveSellOrders == null) {
			myActiveSellOrders = new ArrayList<Order>();
			for (Order order: getMyActiveOrders())
				if (order.getSide() == OrderSide.SELL)
					myActiveSellOrders.add(order);
		}
		return myActiveSellOrders;
	}
	
	public abstract List<Operation> getMyOperations() throws ApiProviderException;

	public Operation getLastBuy() throws ApiProviderException {
		if (lastBuy == null) {
			lastBuy = null;
			for (Operation operation: getMyOperations()) {
				if (lastBuy != null)
					break;
				if (operation.getSide() == OrderSide.BUY)
					lastBuy = operation;
			}
		}
		return lastBuy;
	}
	
	public Operation getLastSell() throws ApiProviderException {
		if (lastSell == null) {
			lastSell = null;
			for (Operation operation: getMyOperations()) {
				if (lastSell != null)
					break;
				if (operation.getSide() == OrderSide.SELL)
					lastSell = operation;
			}
		}
		return lastSell;
	}
		
	public BigDecimal getLastRelevantBuyPrice() throws ApiProviderException {
		if (lastRelevantBuyPrice == null) {
			
			lastRelevantBuyPrice = new BigDecimal(0);
			
			double coinWithOpenOrders = getBalance().getCoinAmount().doubleValue();
			
			List<Operation> groupOfOperations = new ArrayList<Operation>(); 
			double sumOfCoin = 0;
			BigDecimal oldCoinAmount = new BigDecimal(0);
			
			for (Operation operation: getMyOperations()) {
				if (operation.getSide() == OrderSide.BUY) {
					if (sumOfCoin + operation.getCoinAmount().doubleValue() <= coinWithOpenOrders) {
						sumOfCoin += operation.getCoinAmount().doubleValue();
						groupOfOperations.add(operation);
					}
					else {
						oldCoinAmount = operation.getCoinAmount();
						operation.setCoinAmount(new BigDecimal(coinWithOpenOrders - sumOfCoin));
						groupOfOperations.add(operation);
						sumOfCoin += coinWithOpenOrders - sumOfCoin;
						break;
					}
				}
			}
			if (sumOfCoin != 0) {
				for (Operation operation: groupOfOperations) {
					lastRelevantBuyPrice = new BigDecimal(
						lastRelevantBuyPrice.doubleValue() +	
						(operation.getCoinAmount().doubleValue() * 
						operation.getCurrencyPrice().doubleValue() / sumOfCoin)
					); 
				}
			}
			System.out.println("Calculating last relevant buy price: ");
			System.out.println("  " + getCoin() + " with open orders: " + coinWithOpenOrders);
			System.out.println("  Considered " + getCoin() + " sum: " + sumOfCoin);
			System.out.println("  Considered buy operations: " + groupOfOperations.size());
			System.out.println("  Last relevant buy price: " + lastRelevantBuyPrice);
			System.out.println("  Considered operations: ");
			for (Operation operation: groupOfOperations)
				System.out.println("    " + operation.toDisplayString()); 
			System.out.println("");
			groupOfOperations.get(groupOfOperations.size() - 1).setCoinAmount(oldCoinAmount);
		}
		return lastRelevantBuyPrice;
	}
	
	public BigDecimal getLastRelevantSellPrice() throws ApiProviderException {
		if (lastRelevantSellPrice == null) {
			
			lastRelevantSellPrice = new BigDecimal(0);
			
			double sumOfCoin = 0;
			double sumOfNumerators = 0;
			
			List<Order> groupOfOrders = new ArrayList<Order>();
			
			for (int i = 0; i < numOfConsideredOrdersForLastRelevantSellPrice; i++) {
				Order order = (Order) getActiveSellOrders().get(i);				
				sumOfCoin +=  order.getCoinAmount().doubleValue();
				sumOfNumerators += 
					order.getCoinAmount().doubleValue() * order.getCurrencyPrice().doubleValue();
				groupOfOrders.add(order);
			}
			
			if (sumOfCoin != 0) {
				lastRelevantSellPrice = new BigDecimal(sumOfNumerators / sumOfCoin);
			}
			
			System.out.println("Calculating last relevant sell price: ");
			System.out.println("  Considered numerator sum: " + sumOfNumerators);
			System.out.println("  Considered denominator sum: " + sumOfCoin);
			System.out.println("  Considered sell orders: " + groupOfOrders.size());
			System.out.println("  Last relevant sell price: " + lastRelevantSellPrice);
			System.out.println("  Considered orders: ");
			for (Order order: groupOfOrders)
				System.out.println("    " + order); 
			System.out.println("");
		}
		return lastRelevantSellPrice;
	}

	public abstract void cancelOrder(Order order) throws ApiProviderException;

	public abstract void createBuyOrder(BigDecimal coinAmount, BigDecimal currencyPrice) throws ApiProviderException;

	public abstract void createSellOrder(BigDecimal coinAmount, BigDecimal currencyPrice) throws ApiProviderException;

}
