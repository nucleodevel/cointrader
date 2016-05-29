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
import net.trader.exception.NotAvailableMoneyException;

public class ProviderReport {
	
	private static long numOfConsideredOrdersForLastRelevantPriceByOrders = 5;
	
	private UserConfiguration userConfiguration;

	private ApiService apiService;
	private Ticker ticker;
	private Balance balance;
	private OrderBook orderBook;
	
	private List<Order> activeBuyOrders;
	private List<Order> activeSellOrders;

	private List<Order> userActiveBuyOrders;
	private List<Order> userActiveSellOrders;
	
	private List<Order> activeOrders;
	
	private List<Order> userActiveOrders;
	
	private List<Operation> userOperations;
	
	private BigDecimal my24hCoinVolume;
	
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
	
	public void readApiAtFirst() throws ApiProviderException {
		getTicker();
		getBalance();
		getOrderBook();
		getUserActiveOrders();
		getUserOperations();
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

	private List<Order> getActiveBuyOrders() throws ApiProviderException {
		if (activeBuyOrders == null) {
			activeBuyOrders = getOrderBook().getBidOrders();
		}
		return activeBuyOrders;
	}

	private List<Order> getActiveSellOrders() throws ApiProviderException {
		if (activeSellOrders == null) {
			activeSellOrders = getOrderBook().getAskOrders();
		}
		return activeSellOrders;
	}
	
	public Order getCurrentTopOrder(RecordSide side) throws ApiProviderException {
		return getActiveOrders(side).get(0);
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

	private List<Order> getUserActiveBuyOrders() throws ApiProviderException {
		if (userActiveBuyOrders == null) {
			userActiveBuyOrders = new ArrayList<Order>();
			for (Order order: getUserActiveOrders())
				if (order.getSide() == RecordSide.BUY)
					userActiveBuyOrders.add(order);
		}
		return userActiveBuyOrders;
	}

	private List<Order> getUserActiveSellOrders() throws ApiProviderException {
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
	
	public BigDecimal getMy24hCoinVolume() throws ApiProviderException {
		if (my24hCoinVolume == null) {
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
			
			my24hCoinVolume = volume;
		}
		return my24hCoinVolume;
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
	
	public BigDecimal getLastRelevantPriceByOperations(RecordSide side) throws ApiProviderException {
		BigDecimal lastRelevantPriceByOperations = new BigDecimal(0);
		List<Operation> groupOfOperations = new ArrayList<Operation>(); 
		BigDecimal oldCoinAmount = new BigDecimal(0);
		Double sumOfMoney = 0.0;
		
		Double moneyWithOpenOrders = getBalance().getSideAmount(side.getOther()).doubleValue();
		
		for (Operation operation: getUserOperations()) {
			Double otherSideAmount = operation.getSideAmount(side.getOther()).doubleValue();
			if (operation.getSide() == side) {
				if (sumOfMoney + otherSideAmount <= moneyWithOpenOrders) {
					sumOfMoney += otherSideAmount;
					groupOfOperations.add(operation);
				}
				else {
					oldCoinAmount = operation.getCoinAmount();
					if (side == RecordSide.BUY)
						operation.setCoinAmount(new BigDecimal(moneyWithOpenOrders - sumOfMoney));
					else if (side == RecordSide.SELL)
						operation.setCoinAmount(new BigDecimal(
							(moneyWithOpenOrders - sumOfMoney) / 
							operation.getCurrencyPrice().doubleValue()
						));
					groupOfOperations.add(operation);
					sumOfMoney += moneyWithOpenOrders - sumOfMoney;
					break;
				}
			}
		}
		if (sumOfMoney != 0) {
			for (Operation operation: groupOfOperations) {
				Double otherSideAmount = operation.getSideAmount(side.getOther()).doubleValue();
				lastRelevantPriceByOperations = new BigDecimal(
					lastRelevantPriceByOperations.doubleValue() +	
					(otherSideAmount * operation.getCurrencyPrice().doubleValue() / sumOfMoney)
				); 
			}
		}
		
		System.out.println("Last relevant " + side + " price: " + lastRelevantPriceByOperations);
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
		
		System.out.println(
			"Last relevant " + side + " price by orders: " + decFmt.format(lastRelevantPriceByOrders)
		);
		System.out.println("  Considered orders: ");
		for (Order order: groupOfOrders)
			System.out.println("    " + order); 
		System.out.println("");
		
		return lastRelevantPriceByOrders;
	}
	
	public BigDecimal getLastRelevantInactivityTimeByOperations(RecordSide side) throws ApiProviderException {
		BigDecimal lastRelevantInactivityTimeByOperations = new BigDecimal(0);
		
		List<Operation> groupOfOperations = new ArrayList<Operation>(); 
		BigDecimal oldCoinAmount = new BigDecimal(0);
		Double sumOfMoney = 0.0;
		
		Double moneyWithOpenOrders = getBalance().getSideAmount(side.getOther()).doubleValue();
		
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		Long nowTime = now.getTimeInMillis();
		
		for (Operation operation: getUserOperations()) {
			Double otherSideAmount = operation.getSideAmount(side.getOther()).doubleValue();
			if (operation.getSide() == side) {
				if (sumOfMoney + otherSideAmount <= moneyWithOpenOrders) {
					sumOfMoney += otherSideAmount;
					groupOfOperations.add(operation);
				}
				else {
					oldCoinAmount = operation.getCoinAmount();
					if (side == RecordSide.BUY)
						operation.setCoinAmount(new BigDecimal(moneyWithOpenOrders - sumOfMoney));
					else if (side == RecordSide.SELL)
						operation.setCoinAmount(new BigDecimal(
							(moneyWithOpenOrders - sumOfMoney) / 
							operation.getCurrencyPrice().doubleValue()
						));
					groupOfOperations.add(operation);
					sumOfMoney += moneyWithOpenOrders - sumOfMoney;
					break;
				}
			}
		}
		if (sumOfMoney != 0) {
			for (Operation operation: groupOfOperations) {
				Double otherSideAmount = operation.getSideAmount(side.getOther()).doubleValue();
				Long operationTime = operation.getCreationDate().getTimeInMillis();
				Long interval = nowTime - operationTime;
				
				lastRelevantInactivityTimeByOperations = new BigDecimal(
					lastRelevantInactivityTimeByOperations.doubleValue() +	
					(otherSideAmount * interval / sumOfMoney)
				);
			}
		}
		else
			return null;
		
		if (groupOfOperations.size() > 0)
			groupOfOperations.get(groupOfOperations.size() - 1).setCoinAmount(oldCoinAmount);
		
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
	
	public void makeOrdersByLastRelevantPriceByOrders(RecordSide side) throws ApiProviderException {
		System.out.println("");
		System.out.println("Analising " + side + " order");
		System.out.println("");
		
		BigDecimal lastRelevantPrice = getLastRelevantPriceByOrders(side.getOther());
		BigDecimal lastRelevantInactivityTime =
			getLastRelevantInactivityTimeByOperations(side.getOther());
		makeOrdersByLastRelevantPrice(side, lastRelevantPrice, lastRelevantInactivityTime);
	}
	
	public void makeOrdersByLastRelevantPriceByOperations(RecordSide side) throws ApiProviderException {
		System.out.println("");
		System.out.println("Analising " + side + " order");
		System.out.println("");
		
		BigDecimal lastRelevantPrice = getLastRelevantPriceByOperations(side.getOther());
		BigDecimal lastRelevantInactivityTime =
			getLastRelevantInactivityTimeByOperations(side.getOther());
		makeOrdersByLastRelevantPrice(side, lastRelevantPrice, lastRelevantInactivityTime);
	}
	
	private void makeOrdersByLastRelevantPrice(
		RecordSide side, BigDecimal lastRelevantPrice, BigDecimal lastRelevantInactivityTime
	) throws ApiProviderException {
		Double maxAcceptedInactivityTime = getMaxAcceptedInactivityTime(side);
		
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
		
		try {
			Order newOrder = isLongTimeWithoutOperation?
				winTheFirstOrder(side): winTheCurrentOrder(side, 0, lastRelevantPrice, true);
			List<Order> userActiveOrders = getUserActiveOrders(side);
			Order myOrder = userActiveOrders.size() > 0? userActiveOrders.get(0): null;
			if (newOrder != null && newOrder == myOrder)
				System.out.println(
					"Maintaining previous order " + myOrder
				);
		} catch (NotAvailableMoneyException e) {
			System.out.println(
				"There are no money available for " + side
			);
		}	
	}
	
	private Order winTheCurrentOrder(
		RecordSide side, Integer orderIndex, BigDecimal lastRelevantPrice, Boolean keepSearching
	) throws ApiProviderException, NotAvailableMoneyException {
		List<Order> activeOrders = getActiveOrders(side);
		List<Order> userActiveOrders = getUserActiveOrders(side);
		
		Order order = activeOrders.get(orderIndex);
		Order nextOrder = activeOrders.size() - 1 == orderIndex? 
			null: activeOrders.get(orderIndex + 1);
		Order bestOtherSideOrder = getCurrentTopOrder(side.getOther());
		
		BigDecimal currencyPrice = new BigDecimal(
			order.getCurrencyPrice().doubleValue() + userConfiguration.getIncDecPrice(side)
		);
		BigDecimal coinAmount = getBalance().getEstimatedCoinAmount(side, currencyPrice);
		
		if (coinAmount.doubleValue() < userConfiguration.getMinimumCoinAmount()) {
			throw new NotAvailableMoneyException();
		}
		
		double left = order.getCurrencyPrice().doubleValue() / lastRelevantPrice.doubleValue();
		double right = userConfiguration.getMinimumRate(side);
		
		boolean isAGoodOrder = lastRelevantPrice == null || lastRelevantPrice.doubleValue() <= 0;
		if (!isAGoodOrder)
			isAGoodOrder = side == RecordSide.BUY? left <= right: left > right;
			
		if (isAGoodOrder) {
			
			// get the unique order or null
			Order myOrder = userActiveOrders.size() > 0? userActiveOrders.get(0): null;
			
			// if my order isn't the best, delete it and create another 
			if (
				myOrder == null || 
				!decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(myOrder.getCurrencyPrice()))
			) {
				if (myOrder != null)
					cancelOrder(myOrder);
				if (coinAmount.doubleValue() >= userConfiguration.getMinimumCoinAmount()) {
					if (
						bestOtherSideOrder.getCurrencyPrice() == 
						order.getCurrencyPrice().add(new BigDecimal(userConfiguration.getIncDecPrice(side)))
					)
						currencyPrice = new BigDecimal(order.getCurrencyPrice().doubleValue());
					Order newOrder = new Order(
						userConfiguration.getCoin(), userConfiguration.getCurrency(),
						side, coinAmount, currencyPrice
					);
					newOrder.setType(OrderType.LIMITED);
					createOrder(newOrder, side);
					System.out.println(
						side + " order created: " +  (orderIndex + 1) + "° - " + newOrder
					);
					return newOrder;
				}
			}
			else if (
				!keepSearching ||
				(decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(myOrder.getCurrencyPrice())) &&
				Math.abs(order.getCoinAmount().doubleValue() - coinAmount.doubleValue()) <= userConfiguration.getMinimumCoinAmount() &&
				Math.abs(order.getCurrencyPrice().doubleValue() - nextOrder.getCurrencyPrice().doubleValue()) <= 
					userConfiguration.getIncDecPrice(side))
			) {
				return myOrder;
			}
		}
		
		return winTheCurrentOrder(side, orderIndex + 1, lastRelevantPrice, true);
	}
	
	private Order winTheFirstOrder(RecordSide side) throws ApiProviderException, NotAvailableMoneyException {
		return winTheCurrentOrder(side, 0, null, false);
	}
	
	private Double getMaxAcceptedInactivityTime(RecordSide side) throws ApiProviderException {
		return getTicker() == null || userConfiguration.getMaxInterval(side) == null?
			null: 
			userConfiguration.getMaxInterval(side) / (getTicker().getLast3HourVolume().doubleValue());
	}

}