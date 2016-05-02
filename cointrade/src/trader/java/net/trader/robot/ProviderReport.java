package net.trader.robot;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.eti.claudiney.blinktrade.api.BtApiService;
import net.mercadobitcoin.tradeapi.service.MbApiService;
import net.trader.api.ApiService;
import net.trader.beans.Balance;
import net.trader.beans.Coin;
import net.trader.beans.Currency;
import net.trader.beans.Operation;
import net.trader.beans.Order;
import net.trader.beans.OrderBook;
import net.trader.beans.RecordSide;
import net.trader.beans.Ticker;
import net.trader.beans.UserConfiguration;
import net.trader.exception.ApiProviderException;

public class ProviderReport {
	
	private static long numOfConsideredOrdersForLastRelevantSellPrice = 5;
	
	private UserConfiguration userConfiguration;

	private ApiService apiService;
	private Ticker ticker;
	private Balance balance;
	private OrderBook orderBook;
	
	private List<Order> activeBuyOrders;
	private List<Order> activeSellOrders;
	
	private Order currentTopBuy;
	private Order currentTopSell;

	private List<Order> userActiveBuyOrders;
	private List<Order> userActiveSellOrders;
	
	private List<Order> activeOrders;
	
	private Operation lastUserBuyOrder;
	private Operation lastUserSellOrder;
	
	private BigDecimal lastRelevantBuyPrice;
	private BigDecimal lastRelevantSellPrice;
	
	private List<Order> userOrders;
	private List<Order> userCanceledOrders;
	private List<Order> userCompletedOrders;
	private List<Order> userActiveOrders;
	
	private List<Operation> userOperations;
	
	public ProviderReport(UserConfiguration userConfiguration) {
		this.userConfiguration = userConfiguration;
	}
	
	public UserConfiguration getUserConfiguration() {
		return userConfiguration;
	}

	public void setUserConfiguration(UserConfiguration userConfiguration) {
		this.userConfiguration = userConfiguration;
	}

	public Coin getCoin() {
		return userConfiguration.getCoin();
	}

	public Currency getCurrency() {
		return userConfiguration.getCurrency();
	}
	
	private ApiService getApiService() throws ApiProviderException {
		if (apiService == null) {
			if (userConfiguration.getProvider().equals("Blinktrade"))
				apiService = new BtApiService(getUserConfiguration());
			else if (userConfiguration.getProvider().equals("MercadoBitcoin"))
				apiService = new MbApiService(getUserConfiguration());
		}
		return apiService;
	}
	
	public Ticker getTicker() throws ApiProviderException {
		if (ticker == null)
			ticker = getApiService().getTicker();
		return ticker;
	}
	
	public Balance getBalance() throws ApiProviderException {
		if (balance == null)
			balance = getApiService().getBalance();
		return balance;
	}

	public OrderBook getOrderBook() throws ApiProviderException {
		if (orderBook == null)
			orderBook = getApiService().getOrderBook();
		return orderBook;
	}
	
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
			activeBuyOrders = getOrderBook().getBidOrders();
		}
		return activeBuyOrders;
	}

	public List<Order> getActiveSellOrders() throws ApiProviderException {
		if (activeSellOrders == null) {
			activeSellOrders = getOrderBook().getAskOrders();
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

	public List<Order> getUserOrders() throws ApiProviderException {
		if (userOrders == null) {
			userOrders = new ArrayList<Order>();
			userOrders.addAll(getUserActiveOrders());
			userOrders.addAll(getUserCompletedOrders());
			userOrders.addAll(getUserCanceledOrders());
			Collections.sort(userOrders);
		}
		return userOrders;
	}

	public List<Order> getUserActiveOrders() throws ApiProviderException {
		if (userActiveOrders == null)
			userActiveOrders = getApiService().getUserActiveOrders();
		return userActiveOrders;
		
	}
	
	public List<Order> getUserCanceledOrders() throws ApiProviderException {
		if (userCanceledOrders == null)
			userCanceledOrders = getApiService().getUserCompletedOrders();
		return userCanceledOrders;
	}

	public List<Order> getUserCompletedOrders() throws ApiProviderException {
		if (userCompletedOrders == null)
			userCompletedOrders = getApiService().getUserCompletedOrders();
		return userCompletedOrders;
	}

	public List<Order> getUserActiveBuyOrders() throws ApiProviderException {
		if (userActiveBuyOrders == null) {
			userActiveBuyOrders = new ArrayList<Order>();
			for (Order order: getUserActiveOrders())
				if (order.getSide() == RecordSide.BUY)
					userActiveBuyOrders.add(order);
		}
		return userActiveBuyOrders;
	}

	public List<Order> getUserActiveSellOrders() throws ApiProviderException {
		if (userActiveSellOrders == null) {
			userActiveSellOrders = new ArrayList<Order>();
			for (Order order: getUserActiveOrders())
				if (order.getSide() == RecordSide.SELL)
					userActiveSellOrders.add(order);
		}
		return userActiveSellOrders;
	}

	public List<Operation> getUserOperations() throws ApiProviderException {
		if (userOperations == null)
			userOperations = getApiService().getUserOperations();
		return userOperations;
	}

	public Operation getLastUserBuyOrder() throws ApiProviderException {
		if (lastUserBuyOrder == null) {
			lastUserBuyOrder = null;
			for (Operation operation: getUserOperations()) {
				if (lastUserBuyOrder != null)
					break;
				if (operation.getSide() == RecordSide.BUY)
					lastUserBuyOrder = operation;
			}
		}
		return lastUserBuyOrder;
	}
	
	public Operation getLastUserSellOrder() throws ApiProviderException {
		if (lastUserSellOrder == null) {
			lastUserSellOrder = null;
			for (Operation operation: getUserOperations()) {
				if (lastUserSellOrder != null)
					break;
				if (operation.getSide() == RecordSide.SELL)
					lastUserSellOrder = operation;
			}
		}
		return lastUserSellOrder;
	}
		
	public BigDecimal getLastRelevantBuyPrice() throws ApiProviderException {
		if (lastRelevantBuyPrice == null) {
			
			lastRelevantBuyPrice = new BigDecimal(0);
			
			double coinWithOpenOrders = getBalance().getCoinAmount().doubleValue();
			
			List<Operation> groupOfOperations = new ArrayList<Operation>(); 
			double sumOfCoin = 0;
			BigDecimal oldCoinAmount = new BigDecimal(0);
			
			for (Operation operation: getUserOperations()) {
				if (operation.getSide() == RecordSide.BUY) {
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
				System.out.println("    " + operation.toString()); 
			System.out.println("");
			if (groupOfOperations.size() > 0)
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
	
	public void cancelOrder(Order order) throws ApiProviderException {
		getApiService().cancelOrder(order);
	}
	
	public void createBuyOrder(Order order) throws ApiProviderException {
		getApiService().createBuyOrder(order);
	}

	public void createSellOrder(Order order) throws ApiProviderException {
		getApiService().createSellOrder(order);
	}
	
	public void makeBuyOrders() throws ApiProviderException {		
		
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(8);
		
		DecimalFormatSymbols symbols = decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
		System.out.println("");
		System.out.println("Analising buy order");
		
		for (int i = 0; i < getActiveBuyOrders().size(); i++) {
			
			Order order = getActiveBuyOrders().get(i);
			Order nextOrder = getActiveBuyOrders().size() - 1 == i? 
				null: getActiveBuyOrders().get(i + 1);
			
			boolean isAGoodBuyOrder =  
					order.getCurrencyPrice().doubleValue() / 
					getLastRelevantSellPrice().doubleValue() <= 
					1 - userConfiguration.getMinimumBuyRate();
			
			if (isAGoodBuyOrder) {
				
				BigDecimal currencyPrice = new BigDecimal(order.getCurrencyPrice().doubleValue() + userConfiguration.getIncDecPrice());
				Double coinDouble = (getBalance().getCurrencyAmount().doubleValue() - 0.01) / currencyPrice.doubleValue();
				BigDecimal coinAmount = new BigDecimal(coinDouble);
				
				// get the unique buy order or null
				Order myBuyOrder = getUserActiveBuyOrders().size() > 0?
					getUserActiveBuyOrders().get(0): null;
				
				if (myBuyOrder != null) {
					System.out.println(decFmt.format(order.getCurrencyPrice()) + "-" + (decFmt.format(myBuyOrder.getCurrencyPrice())));
					System.out.println(order.getCoinAmount().doubleValue() + " - " + coinAmount.doubleValue());
					System.out.println(order.getCurrencyPrice().doubleValue() - nextOrder.getCurrencyPrice().doubleValue());
				}
				// if my order isn't the best, delete it and create another 
				if (
					myBuyOrder == null || 
					!decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(myBuyOrder.getCurrencyPrice()))
				) {
					if (myBuyOrder != null)
						cancelOrder(myBuyOrder);
					try {
						if (coinAmount.doubleValue() > userConfiguration.getMinimumCoinAmount()) {
							Order newOrder = new Order();
							newOrder.setCoin(userConfiguration.getCoin());
							newOrder.setCurrency(userConfiguration.getCurrency());
							newOrder.setCoinAmount(coinAmount);
							newOrder.setCurrencyPrice(currencyPrice);
							createBuyOrder(newOrder);
							System.out.println(
								"Buy order created: " +
								(i + 1) + "° - " + getCurrency() + " " + 
								decFmt.format(currencyPrice) + " - " + getCoin() + " " + coinAmount
							);
						}
						else
							System.out.println(
								"There are no currency available for " +
								(i + 1) + "° - " + getCurrency() + " " + 
								decFmt.format(currencyPrice) + " - " + getCoin() + " " + coinAmount
							);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				}
				else if (
					decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(myBuyOrder.getCurrencyPrice())) &&
					Math.abs(order.getCoinAmount().doubleValue() - coinAmount.doubleValue()) <= userConfiguration.getMinimumCoinAmount() &&
					order.getCurrencyPrice().doubleValue() - nextOrder.getCurrencyPrice().doubleValue() <= userConfiguration.getIncDecPrice()
				) {
					System.out.println(
						"Maintaining previous order " +
						(i + 1) + "° - " + getCurrency() + " " + 
						decFmt.format(order.getCurrencyPrice()) + " - " + getCoin() + " " + 
						order.getCoinAmount()
					);
					break;
				}
			}
		}
	}
		
	public void makeSellOrders() throws ApiProviderException {	
		
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(8);
		DecimalFormatSymbols symbols=decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
		System.out.println("");
		System.out.println("Analising sell order");
		
		for (int i = 0; i < getActiveSellOrders().size(); i++) {
			
			Order order = getActiveSellOrders().get(i);
			Order nextOrder = getActiveSellOrders().size() - 1 == i? 
				null: getActiveSellOrders().get(i + 1);
			
			boolean isAGoodSellOrder = 
				getLastRelevantBuyPrice() != null && 
				getLastRelevantBuyPrice().doubleValue() > 0 ?
					(order.getCurrencyPrice().doubleValue() / 
					getLastRelevantBuyPrice().doubleValue() >= 
					1 + userConfiguration.getMinimumSellRate()): true;
				
			boolean isToSellSoon = 
				getLastRelevantBuyPrice() != null && 
				getLastRelevantBuyPrice().doubleValue() > 0 ?
					(order.getCurrencyPrice().doubleValue() / 
					getLastRelevantBuyPrice().doubleValue() <= 
					1 + userConfiguration.getSellRateAfterBreakdown()): true;
				
			if (isAGoodSellOrder || isToSellSoon) {
				
				BigDecimal coinAmount = getBalance().getCoinAmount();
				BigDecimal currencyPrice = new BigDecimal(order.getCurrencyPrice().doubleValue() - userConfiguration.getIncDecPrice());
				
				// get the unique buy order or null
				Order mySellOrder = getUserActiveSellOrders().size() > 0?
					getUserActiveSellOrders().get(0): null;
					
				if (mySellOrder != null) {
					System.out.println(decFmt.format(order.getCurrencyPrice()) + "-" + (decFmt.format(mySellOrder.getCurrencyPrice())));
					System.out.println(Math.abs(order.getCoinAmount().doubleValue() - coinAmount.doubleValue()));
					System.out.println(nextOrder.getCurrencyPrice().doubleValue() - order.getCurrencyPrice().doubleValue());
				}
				// if my order isn't the best, delete it and create another 
				if (
					mySellOrder == null || 
					!decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(mySellOrder.getCurrencyPrice()))
				) {
					if (mySellOrder != null)
						cancelOrder(mySellOrder);
					try {
						if (coinAmount.doubleValue() > userConfiguration.getMinimumCoinAmount()) {
							Order newOrder = new Order();
							newOrder.setCoin(userConfiguration.getCoin());
							newOrder.setCurrency(userConfiguration.getCurrency());
							newOrder.setCoinAmount(coinAmount);
							newOrder.setCurrencyPrice(currencyPrice);
							createSellOrder(newOrder);
							System.out.println(
								"Sell order created: " +
								(i + 1) + "° - " + getCurrency() + " " + 
								decFmt.format(currencyPrice) + " - " + getCoin() + " " + coinAmount
							);
						}
						else
							System.out.println(
								"There are no " + getCoin() + " available for " +
								(i + 1) + "° - " + getCurrency() + " " + 
								decFmt.format(currencyPrice) + " - " + getCoin() + " " + coinAmount
							);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				}
				else if (
					decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(mySellOrder.getCurrencyPrice())) &&
					Math.abs(order.getCoinAmount().doubleValue() - coinAmount.doubleValue()) <= userConfiguration.getMinimumCoinAmount() &&
					nextOrder.getCurrencyPrice().doubleValue() - order.getCurrencyPrice().doubleValue() <= userConfiguration.getIncDecPrice()
				) {
					System.out.println(
						"Maintaining previous order " +
						(i + 1) + "° - " + getCurrency() + " " + 
						decFmt.format(order.getCurrencyPrice()) + " - " + getCoin() + " " + 
						order.getCoinAmount()
					);
					break;
				}
			}
		}
	}

}
