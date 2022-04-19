package org.nucleodevel.cointrader.robot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.nucleodevel.cointrader.api.ApiService;
import org.nucleodevel.cointrader.api.blinktrade.BlinktradeApiService;
import org.nucleodevel.cointrader.api.mercadobitcoin.MercadoBitcoinApiService;
import org.nucleodevel.cointrader.api.poloniex.PoloniexApiService;
import org.nucleodevel.cointrader.beans.Balance;
import org.nucleodevel.cointrader.beans.CoinCurrencyPair;
import org.nucleodevel.cointrader.beans.Operation;
import org.nucleodevel.cointrader.beans.Order;
import org.nucleodevel.cointrader.beans.OrderBook;
import org.nucleodevel.cointrader.beans.OrderStatus;
import org.nucleodevel.cointrader.beans.OrderType;
import org.nucleodevel.cointrader.beans.Provider;
import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.beans.RecordSideMode;
import org.nucleodevel.cointrader.beans.Ticker;
import org.nucleodevel.cointrader.beans.UserConfiguration;
import org.nucleodevel.cointrader.exception.ApiProviderException;
import org.nucleodevel.cointrader.exception.NotAvailableMoneyException;

public class ProviderReport {

	private static long numOfConsideredOrdersForLastRelevantPriceByOrders = 5;

	private UserConfiguration userConfiguration;

	private Map<String, ApiService> apiServiceMap;
	private Map<String, Ticker> tickerMap;
	private Map<String, Balance> balanceMap;
	private Map<String, BigDecimal> spreadMap;
	private Map<String, OrderBook> orderBookMap;

	private Map<String, List<Order>> activeBuyOrdersMap;
	private Map<String, List<Order>> activeSellOrdersMap;

	private Map<String, List<Order>> userActiveBuyOrdersMap;
	private Map<String, List<Order>> userActiveSellOrdersMap;

	private Map<String, List<Order>> userActiveOrdersMap;

	private Map<String, List<Operation>> userOperationsMap;

	private Map<String, BigDecimal> my24hCoinVolumeMap;

	private static DecimalFormat decFmt;

	public ProviderReport(UserConfiguration userConfiguration) throws ApiProviderException {
		this.userConfiguration = userConfiguration;
		makeDecimalFormat();

		makeApiServiceMap();

		tickerMap = new HashMap<>();
		balanceMap = new HashMap<>();
		spreadMap = new HashMap<>();
		orderBookMap = new HashMap<>();

		activeBuyOrdersMap = new HashMap<>();
		activeSellOrdersMap = new HashMap<>();

		userActiveBuyOrdersMap = new HashMap<>();
		userActiveSellOrdersMap = new HashMap<>();

		userActiveOrdersMap = new HashMap<>();
		userOperationsMap = new HashMap<>();

		my24hCoinVolumeMap = new HashMap<>();
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

	public List<CoinCurrencyPair> getCoinCurrencyPairList() throws ApiProviderException {
		return userConfiguration.getCoinCurrencyPairList();
	}

	public CoinCurrencyPair getCoinCurrencyPair() throws ApiProviderException {
		return userConfiguration.getCoinCurrencyPairList().get(0);
	}

	// ------------ Operations to read data

	private void makeApiServiceMap() throws ApiProviderException {
		apiServiceMap = new HashMap<>();

		Provider provider = userConfiguration.getProvider();
		List<CoinCurrencyPair> coinCurrencyPairList = getCoinCurrencyPairList();

		for (CoinCurrencyPair ccp : coinCurrencyPairList) {
			if (provider == Provider.MERCADO_BITCOIN)
				apiServiceMap.put(ccp.toString(), new MercadoBitcoinApiService(getUserConfiguration(), ccp));
			else if (provider == Provider.BLINKTRADE)
				apiServiceMap.put(ccp.toString(), new BlinktradeApiService(getUserConfiguration(), ccp));
			else if (provider == Provider.POLONIEX)
				apiServiceMap.put(ccp.toString(), new PoloniexApiService(getUserConfiguration(), ccp));
		}
	}

	private ApiService getApiService(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		return apiServiceMap.get(coinCurrencyPair.toString());
	}

	public Ticker getTicker(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!tickerMap.containsKey(coinCurrencyPair.toString())) {
			ApiService apiService = getApiService(coinCurrencyPair);
			tickerMap.put(coinCurrencyPair.toString(), apiService.getTicker());
		}
		return tickerMap.get(coinCurrencyPair.toString());
	}

	public Balance getBalance(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!balanceMap.containsKey(coinCurrencyPair.toString())) {
			ApiService apiService = getApiService(coinCurrencyPair);
			balanceMap.put(coinCurrencyPair.toString(), apiService.getBalance());
		}
		return balanceMap.get(coinCurrencyPair.toString());
	}

	public BigDecimal getSpread(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!spreadMap.containsKey(coinCurrencyPair.toString())) {
			Order currentTopBuyOrder = getActiveOrders(coinCurrencyPair, RecordSide.BUY).get(0);
			Order currentTopSellOrder = getActiveOrders(coinCurrencyPair, RecordSide.SELL).get(0);

			BigDecimal currentTopBuyPrice = currentTopBuyOrder.getCurrencyPrice();
			BigDecimal currentTopSellPrice = currentTopSellOrder.getCurrencyPrice();

			BigDecimal spread = currentTopSellPrice.divide(currentTopBuyPrice, 6, RoundingMode.HALF_EVEN)
					.subtract(new BigDecimal(1.0));
			spreadMap.put(coinCurrencyPair.toString(), spread);
		}
		return spreadMap.get(coinCurrencyPair.toString());
	}

	public OrderBook getOrderBook(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!orderBookMap.containsKey(coinCurrencyPair.toString())) {
			ApiService apiService = getApiService(coinCurrencyPair);
			orderBookMap.put(coinCurrencyPair.toString(), apiService.getOrderBook());
		}
		return orderBookMap.get(coinCurrencyPair.toString());
	}

	public List<Order> getActiveOrders(CoinCurrencyPair coinCurrencyPair, RecordSide side) throws ApiProviderException {
		List<Order> orders = new ArrayList<Order>();
		switch (side) {
		case BUY:
			orders = getActiveBuyOrders(coinCurrencyPair);
			break;
		case SELL:
			orders = getActiveSellOrders(coinCurrencyPair);
			break;
		}
		return orders;
	}

	public List<Order> getActiveBuyOrders(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!activeBuyOrdersMap.containsKey(coinCurrencyPair.toString())) {
			activeBuyOrdersMap.put(coinCurrencyPair.toString(), getOrderBook(coinCurrencyPair).getBidOrders());
		}
		return activeBuyOrdersMap.get(coinCurrencyPair.toString());
	}

	public List<Order> getActiveSellOrders(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!activeSellOrdersMap.containsKey(coinCurrencyPair.toString())) {
			activeSellOrdersMap.put(coinCurrencyPair.toString(), getOrderBook(coinCurrencyPair).getAskOrders());
		}
		return activeSellOrdersMap.get(coinCurrencyPair.toString());
	}

	public Order getCurrentTopOrder(CoinCurrencyPair coinCurrencyPair, RecordSide side) throws ApiProviderException {
		return getActiveOrders(coinCurrencyPair, side).get(0);
	}

	public List<Order> getUserActiveOrders(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!userActiveOrdersMap.containsKey(coinCurrencyPair.toString())) {
			ApiService apiService = getApiService(coinCurrencyPair);
			userActiveOrdersMap.put(coinCurrencyPair.toString(), apiService.getUserActiveOrders());
		}
		return userActiveOrdersMap.get(coinCurrencyPair.toString());
	}

	public List<Order> getUserActiveOrders(CoinCurrencyPair coinCurrencyPair, RecordSide side)
			throws ApiProviderException {
		List<Order> orders = new ArrayList<Order>();
		switch (side) {
		case BUY:
			orders = getUserActiveBuyOrders(coinCurrencyPair);
			break;
		case SELL:
			orders = getUserActiveSellOrders(coinCurrencyPair);
			break;
		}
		return orders;

	}

	private List<Order> getUserActiveBuyOrders(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!userActiveBuyOrdersMap.containsKey(coinCurrencyPair.toString())) {
			List<Order> orderList = new ArrayList<>();

			for (Order order : getUserActiveOrders(coinCurrencyPair))
				if (order.getSide() == RecordSide.BUY)
					orderList.add(order);

			userActiveBuyOrdersMap.put(coinCurrencyPair.toString(), orderList);
		}
		return userActiveBuyOrdersMap.get(coinCurrencyPair.toString());
	}

	private List<Order> getUserActiveSellOrders(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!userActiveSellOrdersMap.containsKey(coinCurrencyPair.toString())) {
			List<Order> orderList = new ArrayList<>();

			for (Order order : getUserActiveOrders(coinCurrencyPair))
				if (order.getSide() == RecordSide.SELL)
					orderList.add(order);

			userActiveSellOrdersMap.put(coinCurrencyPair.toString(), orderList);
		}
		return userActiveSellOrdersMap.get(coinCurrencyPair.toString());
	}

	public List<Operation> getUserOperations(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!userOperationsMap.containsKey(coinCurrencyPair.toString())) {
			ApiService apiService = getApiService(coinCurrencyPair);
			userOperationsMap.put(coinCurrencyPair.toString(), apiService.getUserOperations());
		}
		return userOperationsMap.get(coinCurrencyPair.toString());
	}

	public BigDecimal getMy24hCoinVolume(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!my24hCoinVolumeMap.containsKey(coinCurrencyPair.toString())) {
			Calendar from = Calendar.getInstance();
			Calendar to = Calendar.getInstance();

			from.setTime(new Date());
			from.add(Calendar.HOUR, -24);
			to.setTime(new Date());

			BigDecimal volume = new BigDecimal(0);
			for (Operation operation : getUserOperations(coinCurrencyPair))
				if (operation.getCreationDate().getTimeInMillis() > from.getTimeInMillis())
					volume = volume.add(operation.getCoinAmount());
				else
					break;

			my24hCoinVolumeMap.put(coinCurrencyPair.toString(), volume);
		}
		return my24hCoinVolumeMap.get(coinCurrencyPair.toString());
	}

	public Operation getLastUserOperation(CoinCurrencyPair coinCurrencyPair, RecordSide side)
			throws ApiProviderException {
		Operation lastUserOperation = null;
		for (Operation operation : getUserOperations(coinCurrencyPair)) {
			if (lastUserOperation != null)
				break;
			if (operation.getSide() == side)
				lastUserOperation = operation;
		}
		return lastUserOperation;
	}

	public Operation getLastUserOperation(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (getUserOperations(coinCurrencyPair).size() > 0)
			return getUserOperations(coinCurrencyPair).get(0);
		return null;
	}

	// ------------ Operations to make orders

	public BigDecimal getLastRelevantPriceByOperations(CoinCurrencyPair coinCurrencyPair, RecordSide side,
			Boolean showMessages) throws ApiProviderException {

		BigDecimal lastRelevantPriceByOperations = new BigDecimal(0);
		List<Operation> groupOfOperations = new ArrayList<Operation>();
		BigDecimal oldCoinAmount = new BigDecimal(0);
		Double sumOfMoney = 0.0;

		Double moneyWithOpenOrders = getBalance(coinCurrencyPair).getSideAmount(side.getOther()).doubleValue();

		for (Operation operation : getUserOperations(coinCurrencyPair)) {
			Double otherSideAmount = operation.getSideAmount(side.getOther()).doubleValue();
			if (operation.getSide() == side) {
				if (sumOfMoney + otherSideAmount <= moneyWithOpenOrders) {
					sumOfMoney += otherSideAmount;
					groupOfOperations.add(operation);
				} else {
					oldCoinAmount = operation.getCoinAmount();
					if (side == RecordSide.BUY)
						operation.setCoinAmount(new BigDecimal(moneyWithOpenOrders - sumOfMoney));
					else if (side == RecordSide.SELL)
						operation.setCoinAmount(new BigDecimal(
								(moneyWithOpenOrders - sumOfMoney) / operation.getCurrencyPrice().doubleValue()));
					groupOfOperations.add(operation);
					sumOfMoney += moneyWithOpenOrders - sumOfMoney;
					break;
				}
			}
		}
		if (sumOfMoney != 0) {
			for (Operation operation : groupOfOperations) {
				Double otherSideAmount = operation.getSideAmount(side.getOther()).doubleValue();
				lastRelevantPriceByOperations = new BigDecimal(lastRelevantPriceByOperations.doubleValue()
						+ (otherSideAmount * operation.getCurrencyPrice().doubleValue() / sumOfMoney));
			}
		}
		if (showMessages) {
			System.out.println("  Last relevant " + side + " price: " + decFmt.format(lastRelevantPriceByOperations));
			System.out.println("  Considered operations: ");
			for (Operation operation : groupOfOperations)
				System.out.println("    " + operation.toString());
			System.out.println("");
		}
		if (groupOfOperations.size() > 0)
			groupOfOperations.get(groupOfOperations.size() - 1).setCoinAmount(oldCoinAmount);

		return lastRelevantPriceByOperations;
	}

	public BigDecimal getLastRelevantPriceByOrders(CoinCurrencyPair coinCurrencyPair, RecordSide side,
			Boolean showMessages) throws ApiProviderException {

		BigDecimal lastRelevantPriceByOrders = new BigDecimal(0);

		double sumOfCoin = 0;
		double sumOfNumerators = 0;

		List<Order> groupOfOrders = new ArrayList<Order>();

		for (int i = 0; i < numOfConsideredOrdersForLastRelevantPriceByOrders; i++) {
			Order order = (Order) getActiveOrders(coinCurrencyPair, side).get(i);
			sumOfCoin += order.getCoinAmount().doubleValue();
			sumOfNumerators += order.getCoinAmount().doubleValue() * order.getCurrencyPrice().doubleValue();
			groupOfOrders.add(order);
		}

		if (sumOfCoin != 0) {
			lastRelevantPriceByOrders = new BigDecimal(sumOfNumerators / sumOfCoin);
		}

		if (showMessages) {
			System.out.println(
					"  Last relevant " + side + " price by orders: " + decFmt.format(lastRelevantPriceByOrders));
			System.out.println("  Considered orders: ");
			for (Order order : groupOfOrders)
				System.out.println("    " + order);
			System.out.println("");
		}

		return lastRelevantPriceByOrders;
	}

	public BigDecimal getLastRelevantInactivityTimeByOperations(CoinCurrencyPair coinCurrencyPair, RecordSide side)
			throws ApiProviderException {

		BigDecimal lastRelevantInactivityTimeByOperations = new BigDecimal(0);

		List<Operation> groupOfOperations = new ArrayList<Operation>();
		BigDecimal oldCoinAmount = new BigDecimal(0);
		Double sumOfMoney = 0.0;

		Double moneyWithOpenOrders = getBalance(coinCurrencyPair).getSideAmount(side.getOther()).doubleValue();

		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		if (getApiService(coinCurrencyPair).getTimeZone().getDisplayName()
				.equals(TimeZone.getTimeZone("GMT").getDisplayName())) {
			TimeZone tz1 = getApiService(coinCurrencyPair).getTimeZone();
			TimeZone tz2 = TimeZone.getTimeZone("GMT-03:00");
			int timeDifference = tz1.getRawOffset() - tz2.getRawOffset();
			now.add(Calendar.HOUR, timeDifference / 60 / 60 / 1000);
		}

		Long nowTime = now.getTimeInMillis();

		for (Operation operation : getUserOperations(coinCurrencyPair)) {
			Double otherSideAmount = operation.getSideAmount(side.getOther()).doubleValue();
			if (operation.getSide() == side) {
				if (sumOfMoney + otherSideAmount <= moneyWithOpenOrders) {
					sumOfMoney += otherSideAmount;
					groupOfOperations.add(operation);
				} else {
					oldCoinAmount = operation.getCoinAmount();
					if (side == RecordSide.BUY)
						operation.setCoinAmount(new BigDecimal(moneyWithOpenOrders - sumOfMoney));
					else if (side == RecordSide.SELL)
						operation.setCoinAmount(new BigDecimal(
								(moneyWithOpenOrders - sumOfMoney) / operation.getCurrencyPrice().doubleValue()));
					groupOfOperations.add(operation);
					sumOfMoney += moneyWithOpenOrders - sumOfMoney;
					break;
				}
			}
		}
		if (sumOfMoney != 0) {
			for (Operation operation : groupOfOperations) {
				Double otherSideAmount = operation.getSideAmount(side.getOther()).doubleValue();
				Long operationTime = operation.getCreationDate().getTimeInMillis();
				Long interval = nowTime - operationTime;

				lastRelevantInactivityTimeByOperations = new BigDecimal(
						lastRelevantInactivityTimeByOperations.doubleValue()
								+ (otherSideAmount * interval / sumOfMoney));
			}
		} else
			return null;

		if (groupOfOperations.size() > 0)
			groupOfOperations.get(groupOfOperations.size() - 1).setCoinAmount(oldCoinAmount);

		return lastRelevantInactivityTimeByOperations;
	}

	public void cancelOrder(Order order) throws ApiProviderException {
		CoinCurrencyPair coinCurrencyPair = order.getCoinCurrencyPair();
		getApiService(coinCurrencyPair).cancelOrder(order);
	}

	public void createOrder(Order order, RecordSide side) throws ApiProviderException {
		CoinCurrencyPair coinCurrencyPair = order.getCoinCurrencyPair();

		order.setSide(side);
		order.setStatus(OrderStatus.ACTIVE);
		getApiService(coinCurrencyPair).createOrder(order);
	}

	public void makeOrdersByLastRelevantPrice(RecordSide side, RecordSideMode mode) throws ApiProviderException {
		System.out.println("");
		System.out.println("Analising " + side + " order");
		System.out.println("");

		Boolean hasToWinCurrent = true;
		BigDecimal lastRelevantPrice = null;

		CoinCurrencyPair bestCoinCurrencyPairBySpread = null;
		BigDecimal bestSpread = new BigDecimal(0.0);

		String bestSpreadMessage = "\n  Analising spreads";

		for (CoinCurrencyPair ccp : getCoinCurrencyPairList()) {
			BigDecimal ccpSpread = getSpread(ccp);

			if (ccpSpread.compareTo(bestSpread) > 0) {
				bestCoinCurrencyPairBySpread = ccp;
				bestSpread = ccpSpread;
			}

			bestSpreadMessage += "\n    Spread of " + ccp + ": " + ccpSpread;
		}

		bestSpreadMessage += "\n   The best spread is " + bestCoinCurrencyPairBySpread + ": " + bestSpread + "\n";

		switch (mode) {

		case ORDERS:
			lastRelevantPrice = getLastRelevantPriceByOrders(bestCoinCurrencyPairBySpread, side, true);

			System.out.println("  Price to win: " + decFmt.format(lastRelevantPrice));
			makeOrdersByLastRelevantPrice(bestCoinCurrencyPairBySpread, side, lastRelevantPrice, hasToWinCurrent);
			break;

		case OTHER_ORDERS:

			System.out.println(bestSpreadMessage);

			System.out.println("");
			System.out.println("  ---- " + side + ": " + bestCoinCurrencyPairBySpread);

			lastRelevantPrice = getLastRelevantPriceByOrders(bestCoinCurrencyPairBySpread, side.getOther(), true)
					.multiply(new BigDecimal(userConfiguration.getMinimumRate(side)));

			System.out.println("  Price to win: " + decFmt.format(lastRelevantPrice));

			cancelAllOrdersOfOtherCoinCurrencyPairsButSameSide(bestCoinCurrencyPairBySpread, side);
			makeOrdersByLastRelevantPrice(bestCoinCurrencyPairBySpread, side, lastRelevantPrice, hasToWinCurrent);
			break;

		case OPERATIONS:
			for (CoinCurrencyPair ccp : getCoinCurrencyPairList()) {
				lastRelevantPrice = getLastRelevantPriceByOperations(ccp, side, true);

				System.out.println("  Price to win: " + decFmt.format(lastRelevantPrice));
				makeOrdersByLastRelevantPrice(ccp, side, lastRelevantPrice, hasToWinCurrent);
			}
			break;

		case OTHER_OPERATIONS:

			for (CoinCurrencyPair ccp : getCoinCurrencyPairList()) {

				System.out.println("");
				System.out.println("  ---- " + side + ": " + ccp);

				boolean isLongTimeWithoutOperation = false;

				BigDecimal lastRelevantInactivityTime = getLastRelevantInactivityTimeByOperations(ccp, side.getOther());

				Double maxAcceptedInactivityTime = getMaxAcceptedInactivityTime(ccp, side);
				isLongTimeWithoutOperation = lastRelevantInactivityTime == null || maxAcceptedInactivityTime == null
						? false
						: lastRelevantInactivityTime.longValue() > maxAcceptedInactivityTime;

				if (lastRelevantInactivityTime != null && maxAcceptedInactivityTime != null) {
					System.out.println(
							"  Last 3 hour volume: " + getTicker(ccp).getLast3HourVolume() + " " + ccp.getCoin());
					System.out.println("  Inactivity time: "
							+ decFmt.format(lastRelevantInactivityTime.doubleValue() / (60 * 1000)) + " minutes");
					System.out.println("  Max accepted inactivity time: "
							+ decFmt.format(maxAcceptedInactivityTime / (60 * 1000)) + " minutes");
				}

				Boolean isBreakdown = false;

				BigDecimal lastRelevantPriceByOrders = getLastRelevantPriceByOrders(ccp, side, false);
				BigDecimal lastRelevantPriceByOperations = getLastRelevantPriceByOperations(ccp, side.getOther(),
						false);
				if (userConfiguration.getBreakdownRate(side) != null) {
					BigDecimal breakdownPrice = lastRelevantPriceByOperations
							.multiply(new BigDecimal(userConfiguration.getBreakdownRate(side)));

					int compareBreakdownToOrders = breakdownPrice.compareTo(lastRelevantPriceByOrders);
					isBreakdown = (side == RecordSide.BUY ? compareBreakdownToOrders < 0
							: (side == RecordSide.SELL ? compareBreakdownToOrders > 0 : false));

					System.out.println("  Breakdown if breakdown price " + decFmt.format(breakdownPrice) + " is "
							+ (side == RecordSide.BUY ? "less" : (side == RecordSide.SELL ? "greater" : "")) + " than "
							+ side + " average price " + decFmt.format(lastRelevantPriceByOrders));

					if (isBreakdown)
						System.out.println("  Breakdown was activated");
				}

				if (isLongTimeWithoutOperation || isBreakdown) {
					lastRelevantPrice = getLastRelevantPriceByOrders(ccp, side, true);
					hasToWinCurrent = false;
				} else
					lastRelevantPrice = getLastRelevantPriceByOperations(ccp, side.getOther(), true)
							.multiply(new BigDecimal(userConfiguration.getMinimumRate(side)));

				System.out.println("  Price to win: " + decFmt.format(lastRelevantPrice));
				makeOrdersByLastRelevantPrice(ccp, side, lastRelevantPrice, hasToWinCurrent);
			}
			break;
		default:
			for (CoinCurrencyPair ccp : getCoinCurrencyPairList()) {
				Order myOrder = getUserActiveOrders(ccp, side).size() > 0 ? getUserActiveOrders(ccp, side).get(0)
						: null;
				if (myOrder != null)
					cancelOrder(myOrder);
				System.out.println("\n  Don't make buy order but cancel any!");
			}
			break;
		}
	}

	private void makeOrdersByLastRelevantPrice(CoinCurrencyPair coinCurrencyPair, RecordSide side,
			BigDecimal lastRelevantPrice, Boolean hasToWinCurrent) throws ApiProviderException {

		try {
			Order newOrder = hasToWinCurrent ? winTheCurrentOrder(coinCurrencyPair, side, 0, lastRelevantPrice)
					: winThePreviousOrder(coinCurrencyPair, side, 0, lastRelevantPrice);

			List<Order> userActiveOrders = getUserActiveOrders(coinCurrencyPair, side);
			Order myOrder = userActiveOrders.size() > 0 ? userActiveOrders.get(0) : null;

			if (newOrder == myOrder || (newOrder == null && myOrder != null))
				System.out.println("  Maintaining previous - " + myOrder);
			else {
				if (myOrder != null) {
					cancelOrder(myOrder);
					System.out.println("  " + side + " cancelled: " + " - " + myOrder);
				}
				createOrder(newOrder, side);
				System.out.println("  " + side + " created: " + " - " + newOrder);
			}
		} catch (NotAvailableMoneyException e) {
			System.out.println("  There are no money available for " + side);
		}
	}

	private void cancelAllOrdersOfOtherCoinCurrencyPairsButSameSide(CoinCurrencyPair coinCurrencyPair, RecordSide side)
			throws ApiProviderException {

		for (CoinCurrencyPair ccp : getCoinCurrencyPairList()) {

			// avoid coinCurrencyPair passed by parameter
			if (!ccp.toString().equals(coinCurrencyPair.toString())) {
				List<Order> userActiveOrders = getUserActiveOrders(ccp, side);

				Order myOrder = userActiveOrders.size() > 0 ? userActiveOrders.get(0) : null;
				if (myOrder != null) {
					System.out.println(
							"  " + side + " cancelled by order of " + coinCurrencyPair + " : " + " - " + myOrder);
					cancelOrder(myOrder);
				}
			}

		}
	}

	private Order winTheCurrentOrder(CoinCurrencyPair coinCurrencyPair, RecordSide side, Integer orderIndex,
			BigDecimal lastRelevantPrice) throws ApiProviderException, NotAvailableMoneyException {

		List<Order> activeOrders = getActiveOrders(coinCurrencyPair, side);
		if (orderIndex >= activeOrders.size() - 1) {
			Order myOrder = getUserActiveOrders(coinCurrencyPair, side).size() > 0
					? getUserActiveOrders(coinCurrencyPair, side).get(0)
					: null;
			if (myOrder != null)
				cancelOrder(myOrder);
			BigDecimal coinAmount = getBalance(coinCurrencyPair).getEstimatedCoinAmount(side, lastRelevantPrice);

			if (coinAmount.doubleValue() < userConfiguration.getMinimumCoinAmount()) {
				throw new NotAvailableMoneyException();
			}

			Order newOrder = new Order(coinCurrencyPair.getCoin(), coinCurrencyPair.getCurrency(), side, coinAmount,
					lastRelevantPrice);
			newOrder.setType(OrderType.LIMITED);
			return newOrder;
		}

		Order order = activeOrders.get(orderIndex);

		Double left = lastRelevantPrice == null ? null : order.getCurrencyPrice().doubleValue();
		Double right = lastRelevantPrice == null ? null : lastRelevantPrice.doubleValue();

		boolean isAGoodOrder = lastRelevantPrice == null || lastRelevantPrice.doubleValue() <= 0;
		if (!isAGoodOrder && left != null)
			isAGoodOrder = side == RecordSide.BUY ? left <= right : left > right;

		if (isAGoodOrder) {
			Order newOrder = tryToWinAnOrder(coinCurrencyPair, side, orderIndex);
			if (newOrder != null)
				return newOrder;
		}

		return winTheCurrentOrder(coinCurrencyPair, side, orderIndex + 1, lastRelevantPrice);
	}

	private Order winThePreviousOrder(CoinCurrencyPair coinCurrencyPair, RecordSide side, Integer orderIndex,
			BigDecimal lastRelevantPrice) throws ApiProviderException, NotAvailableMoneyException {

		List<Order> activeOrders = getActiveOrders(coinCurrencyPair, side);
		if (orderIndex >= activeOrders.size() - 1) {
			Order myOrder = getUserActiveOrders(coinCurrencyPair, side).size() > 0
					? getUserActiveOrders(coinCurrencyPair, side).get(0)
					: null;
			if (myOrder != null)
				cancelOrder(myOrder);
			BigDecimal coinAmount = getBalance(coinCurrencyPair).getEstimatedCoinAmount(side, lastRelevantPrice);

			if (coinAmount.doubleValue() < userConfiguration.getMinimumCoinAmount()) {
				throw new NotAvailableMoneyException();
			}

			Order newOrder = new Order(coinCurrencyPair.getCoin(), coinCurrencyPair.getCurrency(), side, coinAmount,
					lastRelevantPrice);
			newOrder.setType(OrderType.LIMITED);
			return newOrder;
		}

		Order order = activeOrders.get(orderIndex);

		Double left = lastRelevantPrice == null ? null : order.getCurrencyPrice().doubleValue();
		Double right = lastRelevantPrice == null ? null : lastRelevantPrice.doubleValue();

		boolean isAGoodOrder = lastRelevantPrice == null || lastRelevantPrice.doubleValue() <= 0;
		if (!isAGoodOrder && left != null)
			isAGoodOrder = side == RecordSide.BUY ? left <= right : left > right;

		if (isAGoodOrder) {
			Order newOrder = tryToWinAnOrder(coinCurrencyPair, side, orderIndex > 0 ? orderIndex - 1 : 0);
			if (newOrder != null)
				return newOrder;
		}

		return winThePreviousOrder(coinCurrencyPair, side, orderIndex + 1, lastRelevantPrice);
	}

	private Order tryToWinAnOrder(CoinCurrencyPair coinCurrencyPair, RecordSide side, Integer orderIndex)
			throws NotAvailableMoneyException, ApiProviderException {

		List<Order> activeOrders = getActiveOrders(coinCurrencyPair, side);
		List<Order> userActiveOrders = getUserActiveOrders(coinCurrencyPair, side);

		Order order = activeOrders.get(orderIndex);
		Order nextOrder = activeOrders.size() - 1 == orderIndex ? null : activeOrders.get(orderIndex + 1);
		Order bestOtherSideOrder = getCurrentTopOrder(coinCurrencyPair, side.getOther());

		BigDecimal currencyPrice = new BigDecimal(
				order.getCurrencyPrice().doubleValue() + userConfiguration.getIncDecPrice(side));
		BigDecimal coinAmount = getBalance(coinCurrencyPair).getEstimatedCoinAmount(side, currencyPrice);

		if (coinAmount.doubleValue() < userConfiguration.getMinimumCoinAmount()) {
			throw new NotAvailableMoneyException();
		}

		// get the unique order or null
		Order myOrder = userActiveOrders.size() > 0 ? userActiveOrders.get(0) : null;

		// if my order isn't the best, delete it and create another
		if (myOrder == null
				|| !decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(myOrder.getCurrencyPrice()))) {
			Boolean isNearTheBestOtherSideOrder = Math.abs(
					order.getCurrencyPrice().add(new BigDecimal(userConfiguration.getIncDecPrice(side))).doubleValue()
							- bestOtherSideOrder.getCurrencyPrice().doubleValue()) <= userConfiguration
									.getIncDecPrice(side);
			if (isNearTheBestOtherSideOrder)
				currencyPrice = new BigDecimal(order.getCurrencyPrice().doubleValue());
			Order newOrder = new Order(coinCurrencyPair.getCoin(), coinCurrencyPair.getCurrency(), side, coinAmount,
					currencyPrice);
			newOrder.setType(OrderType.LIMITED);
			newOrder.setPosition(orderIndex);
			return newOrder;
		} else if ((decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(myOrder.getCurrencyPrice()))
				&& Math.abs(order.getCoinAmount().doubleValue() - coinAmount.doubleValue()) <= userConfiguration
						.getMinimumCoinAmount()
				&& Math.abs(order.getCurrencyPrice().doubleValue()
						- nextOrder.getCurrencyPrice().doubleValue()) <= userConfiguration.getIncDecPrice(side))) {
			myOrder.setPosition(orderIndex);
			return myOrder;
		}
		return null;
	}

	private Double getMaxAcceptedInactivityTime(CoinCurrencyPair coinCurrencyPair, RecordSide side)
			throws ApiProviderException {
		return getTicker(coinCurrencyPair) == null || userConfiguration.getMaxInterval(side) == null ? null
				: userConfiguration.getMaxInterval(side)
						/ (getTicker(coinCurrencyPair).getLast3HourVolume().doubleValue());
	}

}