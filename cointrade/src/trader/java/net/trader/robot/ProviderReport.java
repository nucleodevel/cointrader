package net.trader.robot;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.blinktrade.api.BlinktradeApiService;
import net.mercadobitcoin.api.MercadoBitcoinApiService;
import net.trader.api.ApiService;
import net.trader.beans.Balance;
import net.trader.beans.Coin;
import net.trader.beans.Currency;
import net.trader.beans.Operation;
import net.trader.beans.Order;
import net.trader.beans.OrderBook;
import net.trader.beans.OrderStatus;
import net.trader.beans.OrderType;
import net.trader.beans.Provider;
import net.trader.beans.RecordSide;
import net.trader.beans.Ticker;
import net.trader.beans.UserConfiguration;
import net.trader.exception.ApiProviderException;

public class ProviderReport {
	
	private static long numOfConsideredOrdersForLastRelevantPriceByOrders = 5;
	
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
	
	private Operation lastUserBuyOperation;
	private Operation lastUserSellOperation;
	
	private List<Order> userActiveOrders;
	
	private List<Operation> userOperations;
	
	private BigDecimal my24hVolume;
	
	private static DecimalFormat decFmt;
	
	public ProviderReport(UserConfiguration userConfiguration) {
		this.userConfiguration = userConfiguration;
		makeDecimalFormat();
	}
	
	private static void makeDecimalFormat() {
		if (decFmt == null) {
			decFmt = new DecimalFormat();
			decFmt.setMaximumFractionDigits(8);
			
			DecimalFormatSymbols symbols = decFmt.getDecimalFormatSymbols();
			symbols.setDecimalSeparator('.');
			symbols.setGroupingSeparator(',');
			decFmt.setDecimalFormatSymbols(symbols);
		}
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
			if (userConfiguration.getProvider() == Provider.MERCADO_BITCOIN)
				apiService = new MercadoBitcoinApiService(getUserConfiguration());
			else if (userConfiguration.getProvider() == Provider.BLINKTRADE)
				apiService = new BlinktradeApiService(getUserConfiguration());
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
	
	public List<Operation> getOperationList(Calendar from, Calendar to) throws ApiProviderException {
		return getApiService().getOperationList(from, to);
	}
	
	public List<Order> getActiveOrders(RecordSide side) throws ApiProviderException {
		List<Order> orders = new ArrayList<Order>();
		switch (side) {
			case BUY:
				orders = getActiveBuyOrders();
			break;
			case SELL:
				orders = getActiveSellOrders();
			break;
		}
		return orders;
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
	
	public Order getCurrentTopOrder(RecordSide side) throws ApiProviderException {
		if (currentTopBuy == null)
			currentTopBuy = getActiveOrders(side).get(0);
		return currentTopBuy;
	}

	public Order getCurrentTopBuy() throws ApiProviderException {
		if (currentTopBuy == null)
			currentTopBuy = getCurrentTopOrder(RecordSide.BUY);
		return currentTopBuy;
	}

	public Order getCurrentTopSell() throws ApiProviderException {
		if (currentTopSell == null)
			currentTopSell = getCurrentTopOrder(RecordSide.SELL);
		return currentTopSell;
	}

	public List<Order> getUserActiveOrders() throws ApiProviderException {
		if (userActiveOrders == null)
			userActiveOrders = getApiService().getUserActiveOrders();
		return userActiveOrders;
		
	}

	public List<Order> getUserActiveOrders(RecordSide side) throws ApiProviderException {
		List<Order> orders = new ArrayList<Order>();
		switch (side) {
			case BUY:
				orders = getUserActiveBuyOrders();
			break;
			case SELL:
				orders = getUserActiveSellOrders();
			break;
		}
		return orders;
		
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
	
	public BigDecimal getMy24hVolume() throws ApiProviderException {
		if (my24hVolume == null) {
			Calendar from = Calendar.getInstance();
			Calendar to = Calendar.getInstance();

			from.setTime(new Date());
			from.add(Calendar.HOUR, -24);
			to.setTime(new Date());
			
			BigDecimal volume = new BigDecimal(0);
			for (Operation operation: getUserOperations())
				if (operation.getCreationDate().getTimeInMillis() > from.getTimeInMillis())
					volume = volume.add(operation.getCoinAmount());
				else
					break;
			
			my24hVolume = volume;
		}
		return my24hVolume;
	}

	public Operation getLastUserOperation(RecordSide side) throws ApiProviderException {
		Operation lastUserOperation = null;
		for (Operation operation: getUserOperations()) {
			if (lastUserOperation != null)
				break;
			if (operation.getSide() == side)
				lastUserOperation = operation;
		}
		return lastUserOperation;
	}

	public Operation getLastUserOperation() throws ApiProviderException {
		if (getUserOperations().size() > 0)
			return getUserOperations().get(0);
		return null;
	}

	public Operation getLastUserBuyOperation() throws ApiProviderException {
		if (lastUserBuyOperation == null)
			lastUserBuyOperation = getLastUserOperation(RecordSide.BUY);
		return lastUserBuyOperation;
	}
	
	public Operation getLastUserSellOperation() throws ApiProviderException {
		if (lastUserSellOperation == null)
			lastUserSellOperation = getLastUserOperation(RecordSide.SELL);
		return lastUserSellOperation;
	}
	
	public BigDecimal getLastRelevantPriceByOperations(RecordSide side) throws ApiProviderException {
		BigDecimal lastRelevantPriceByOperations = new BigDecimal(0);
		List<Operation> groupOfOperations = new ArrayList<Operation>(); 
		BigDecimal oldCoinAmount = new BigDecimal(0);
		
		if (side == RecordSide.BUY) {
			
			double coinWithOpenOrders = getBalance().getCoinAmount().doubleValue();
			
			double sumOfCoin = 0;
			
			for (Operation operation: getUserOperations()) {
				if (operation.getSide() == side) {
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
					lastRelevantPriceByOperations = new BigDecimal(
						lastRelevantPriceByOperations.doubleValue() +	
						(operation.getCoinAmount().doubleValue() * 
						operation.getCurrencyPrice().doubleValue() / sumOfCoin)
					); 
				}
			}
			
		} 
		else if (side == RecordSide.SELL) {
			
			double currencyWithOpenOrders = getBalance().getCurrencyAmount().doubleValue();
			
			double sumOfCurrency = 0;
			
			for (Operation operation: getUserOperations()) {
				BigDecimal currencyAmount = operation.getCurrencyAmount();
				if (operation.getSide() == side) {
					if (sumOfCurrency + currencyAmount.doubleValue() <= currencyWithOpenOrders) {
						sumOfCurrency += currencyAmount.doubleValue();
						groupOfOperations.add(operation);
					}
					else {
						oldCoinAmount = operation.getCoinAmount();
						operation.setCoinAmount(new BigDecimal(
							(currencyWithOpenOrders - sumOfCurrency) / 
							operation.getCurrencyPrice().doubleValue()
						));
						groupOfOperations.add(operation);
						sumOfCurrency += currencyWithOpenOrders - sumOfCurrency;
						break;
					}
				}
			}
			if (sumOfCurrency != 0) {
				for (Operation operation: groupOfOperations) {
					lastRelevantPriceByOperations = new BigDecimal(
						lastRelevantPriceByOperations.doubleValue() +	
						(operation.getCurrencyAmount().doubleValue() * 
						operation.getCurrencyPrice().doubleValue() / sumOfCurrency)
					); 
				}
			}
			
		}
		
		System.out.println("Last relevant price: " + lastRelevantPriceByOperations);
		System.out.println("  Considered operations: ");
		for (Operation operation: groupOfOperations)
			System.out.println("    " + operation.toString()); 
		System.out.println("");
		if (groupOfOperations.size() > 0)
			groupOfOperations.get(groupOfOperations.size() - 1).setCoinAmount(oldCoinAmount);
		
		
		return lastRelevantPriceByOperations;
	}
	
	public BigDecimal getLastRelevantPriceByOrders(RecordSide side) throws ApiProviderException {
		BigDecimal lastRelevantPriceByOrders = new BigDecimal(0);
		
		double sumOfCoin = 0;
		double sumOfNumerators = 0;
		
		List<Order> groupOfOrders = new ArrayList<Order>();
		
		for (int i = 0; i < numOfConsideredOrdersForLastRelevantPriceByOrders; i++) {
			Order order = (Order) getActiveOrders(side).get(i);				
			sumOfCoin +=  order.getCoinAmount().doubleValue();
			sumOfNumerators += 
				order.getCoinAmount().doubleValue() * order.getCurrencyPrice().doubleValue();
			groupOfOrders.add(order);
		}
		
		if (sumOfCoin != 0) {
			lastRelevantPriceByOrders = new BigDecimal(sumOfNumerators / sumOfCoin);
		}
		
		System.out.println("Last relevant price by orders: " + decFmt.format(lastRelevantPriceByOrders));
		System.out.println("  Considered orders: ");
		for (Order order: groupOfOrders)
			System.out.println("    " + order); 
		System.out.println("");
		
		return lastRelevantPriceByOrders;
	}
	
	public BigDecimal getLastRelevantInactivityTimeByOperations(RecordSide side) throws ApiProviderException {
		BigDecimal lastRelevantInactivityTimeByOperations = new BigDecimal(0);
		
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		Long nowTime = now.getTimeInMillis();
		
		if (side == RecordSide.BUY) {
			double coinWithOpenOrders = getBalance().getCoinAmount().doubleValue();
			
			List<Operation> groupOfOperations = new ArrayList<Operation>(); 
			double sumOfCoin = 0;
			BigDecimal oldCoinAmount = new BigDecimal(0);
			
			for (Operation operation: getUserOperations()) {
				if (operation.getSide() == side) {
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
					Long operationTime = operation.getCreationDate().getTimeInMillis();
					long interval = nowTime - operationTime;
					lastRelevantInactivityTimeByOperations = new BigDecimal(
						lastRelevantInactivityTimeByOperations.doubleValue() +	
						(operation.getCoinAmount().doubleValue() * interval / sumOfCoin)
					); 
				}
			}
			else
				return null;
			if (groupOfOperations.size() > 0)
				groupOfOperations.get(groupOfOperations.size() - 1).setCoinAmount(oldCoinAmount);
			
		} 
		else if (side == RecordSide.SELL) {
			
			double currencyWithOpenOrders = getBalance().getCurrencyAmount().doubleValue();
			
			List<Operation> groupOfOperations = new ArrayList<Operation>(); 
			double sumOfCurrency = 0;
			BigDecimal oldCoinAmount = new BigDecimal(0);
			
			for (Operation operation: getUserOperations()) {
				BigDecimal currencyAmount = operation.getCurrencyAmount();
				if (operation.getSide() == side) {
					if (sumOfCurrency + currencyAmount.doubleValue() <= currencyWithOpenOrders) {
						sumOfCurrency += currencyAmount.doubleValue();
						groupOfOperations.add(operation);
					}
					else {
						oldCoinAmount = operation.getCoinAmount();
						operation.setCoinAmount(new BigDecimal(
							(currencyWithOpenOrders - sumOfCurrency) / 
							operation.getCurrencyPrice().doubleValue()
						));
						groupOfOperations.add(operation);
						sumOfCurrency += currencyWithOpenOrders - sumOfCurrency;
						break;
					}
				}
			}
			if (sumOfCurrency != 0) {
				for (Operation operation: groupOfOperations) {
					Long operationTime = operation.getCreationDate().getTimeInMillis();
					long interval = nowTime - operationTime;
					lastRelevantInactivityTimeByOperations = new BigDecimal(
						lastRelevantInactivityTimeByOperations.doubleValue() +	
						(operation.getCurrencyAmount().doubleValue() * interval / sumOfCurrency)
					); 
				}
			}
			else
				return null;
			if (groupOfOperations.size() > 0)
				groupOfOperations.get(groupOfOperations.size() - 1).setCoinAmount(oldCoinAmount);
			
		}
		
		
		return lastRelevantInactivityTimeByOperations;
	}
	
	public BigInteger getLastUserOperationInterval() throws ApiProviderException {
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		Long nowTime = now.getTimeInMillis();
		Operation lastOperation = getLastUserOperation();
		if (lastOperation == null)
			return null;
		Long lastOperationTime = lastOperation.getCreationDate().getTimeInMillis();
		return BigInteger.valueOf(nowTime - lastOperationTime);
	}
	
	public void cancelOrder(Order order) throws ApiProviderException {
		getApiService().cancelOrder(order);
	}
	
	public void createOrder(Order order, RecordSide side) throws ApiProviderException {
		order.setSide(side);
		order.setStatus(OrderStatus.ACTIVE);
		getApiService().createOrder(order);
	}
	
	public void createBuyOrder(Order order) throws ApiProviderException {
		getApiService().createBuyOrder(order);
	}

	public void createSellOrder(Order order) throws ApiProviderException {
		getApiService().createSellOrder(order);
	}
	
	public void makeOrdersByLastRelevantPriceByOrders(RecordSide side) throws ApiProviderException {
		System.out.println("");
		System.out.println("Analising " + side + " order");
		System.out.println("");
		
		BigDecimal lastRelevantPrice = getLastRelevantPriceByOrders(side.getOther());
		makeOrdersByLastRelevantPrice(side, lastRelevantPrice, null);
	}
	
	public void makeOrdersByLastRelevantPriceByOperations(RecordSide side) throws ApiProviderException {
		System.out.println("");
		System.out.println("Analising " + side + " order");
		System.out.println("");
		
		BigDecimal lastRelevantPrice = getLastRelevantPriceByOperations(side.getOther());
		if (lastRelevantPrice == null)
			lastRelevantPrice = getLastRelevantPriceByOrders(side.getOther());
		BigDecimal lastRelevantInactivityTime =
			getLastRelevantInactivityTimeByOperations(side.getOther());
		makeOrdersByLastRelevantPrice(side, lastRelevantPrice, lastRelevantInactivityTime);
	}
	
	private void makeOrdersByLastRelevantPrice(RecordSide side, BigDecimal lastRelevantPrice, BigDecimal lastRelevantInactivityTime) throws ApiProviderException {
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(8);
		
		DecimalFormatSymbols symbols = decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
		List<Order> activeOrders = getActiveOrders(side);
		List<Order> userActiveOrders = getUserActiveOrders(side);
		
		Double maxAcceptedInactivityTime = 
			getTicker() == null || userConfiguration.getMaxInterval(side) == null?
				null: 
				userConfiguration.getMaxInterval(side) / (getTicker().getLast3HourVolume().doubleValue());
		
		boolean isLongTimeWithoutOperation = 
			lastRelevantInactivityTime == null || maxAcceptedInactivityTime == null?
				false:
				lastRelevantInactivityTime.longValue() > maxAcceptedInactivityTime;
		
		if (lastRelevantInactivityTime != null && maxAcceptedInactivityTime != null) {
			System.out.println("  Last 3 hour volume: " + getTicker().getLast3HourVolume() + " " + getCoin());
			System.out.println(
				"  Inactivity time: " 
				+ decFmt.format(lastRelevantInactivityTime.doubleValue() / (60 * 1000))
				+ " minutes" 
			);
			System.out.println(
				"  Max accepted inactivity time: " 
				+ decFmt.format(maxAcceptedInactivityTime / (60 * 1000))
				+ " minutes" 
			);
		}
		
		for (int i = 0; i < activeOrders.size(); i++) {
			
			Order order = activeOrders.get(i);
			Order nextOrder = activeOrders.size() - 1 == i? null: activeOrders.get(i + 1);
			
			double left = 
				order.getCurrencyPrice().doubleValue() / lastRelevantPrice.doubleValue();
			double right = 1 + userConfiguration.getMinimumRate(side);
			
			boolean isAGoodOrder = lastRelevantPrice == null || lastRelevantPrice.doubleValue() <= 0;
			if (!isAGoodOrder)
				isAGoodOrder = side == RecordSide.BUY? left <= right: left > right;
			
			if (isAGoodOrder || isLongTimeWithoutOperation) {
				
				Order bestOtherSideOrder = getCurrentTopOrder(side.getOther());
				BigDecimal currencyPrice = null;
				
				if (
					bestOtherSideOrder.getCurrencyPrice() == 
					order.getCurrencyPrice().add(new BigDecimal(userConfiguration.getIncDecPrice(side)))
				)
					currencyPrice = new BigDecimal(order.getCurrencyPrice().doubleValue());
				else
					currencyPrice = new BigDecimal(
						order.getCurrencyPrice().doubleValue() + userConfiguration.getIncDecPrice(side)
					);
					
				BigDecimal coinAmount = new BigDecimal(0);
				
				coinAmount = getBalance().getEstimatedCoinAmount(side, currencyPrice);
				
				// get the unique order or null
				Order myOrder = userActiveOrders.size() > 0? userActiveOrders.get(0): null;
				
				// if my order isn't the best, delete it and create another 
				if (
					myOrder == null || 
					!decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(myOrder.getCurrencyPrice()))
				) {
					if (myOrder != null)
						cancelOrder(myOrder);
					try {
						if (coinAmount.doubleValue() > userConfiguration.getMinimumCoinAmount()) {
							Order newOrder = new Order(
								userConfiguration.getCoin(), userConfiguration.getCurrency(),
								side, coinAmount, currencyPrice
							);
							newOrder.setType(OrderType.LIMITED);
							createOrder(newOrder, side);
							System.out.println(
								side + " order created: " +  (i + 1) + "° - " + newOrder
							);
						}
						else
							System.out.println(
								"There are no currency available for " + (i + 1) + "° - " 
								+ getCurrency() + decFmt.format(currencyPrice)
							);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				}
				else if (
					decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(myOrder.getCurrencyPrice())) &&
					Math.abs(order.getCoinAmount().doubleValue() - coinAmount.doubleValue()) <= userConfiguration.getMinimumCoinAmount() &&
					Math.abs(order.getCurrencyPrice().doubleValue() - nextOrder.getCurrencyPrice().doubleValue()) <= 
						Math.abs(userConfiguration.getIncDecPrice())
				) {
					System.out.println(
						"Maintaining previous order " + (i + 1) + "° - " + myOrder
					);
					break;
				}
			}
		}
	}

}