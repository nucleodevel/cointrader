package org.nucleodevel.cointrader.robot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

import org.nucleodevel.cointrader.api.AbstractApiService;
import org.nucleodevel.cointrader.beans.Balance;
import org.nucleodevel.cointrader.beans.CoinCurrencyPair;
import org.nucleodevel.cointrader.beans.Operation;
import org.nucleodevel.cointrader.beans.Order;
import org.nucleodevel.cointrader.beans.OrderBook;
import org.nucleodevel.cointrader.beans.OrderStatus;
import org.nucleodevel.cointrader.beans.OrderType;
import org.nucleodevel.cointrader.beans.Provider;
import org.nucleodevel.cointrader.beans.Record;
import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.beans.RecordSideMode;
import org.nucleodevel.cointrader.beans.Ticker;
import org.nucleodevel.cointrader.beans.UserConfiguration;
import org.nucleodevel.cointrader.beans.UserSideConfiguration;
import org.nucleodevel.cointrader.exception.ApiProviderException;
import org.nucleodevel.cointrader.exception.NotAvailableMoneyException;
import org.nucleodevel.cointrader.recordsidemode.implementer.AbstractRecordSideModeImplementer;

public class ProviderReport {

	private static long NUM_OF_CONSIDERED_RECORDS_FOR_LAST_RELEVANT_PRICE_BY_RECORDS_AND_THEIR_POSITIONS = 5;

	private UserConfiguration userConfiguration;

	private Map<String, AbstractApiService> apiServiceMap;
	private Map<String, Ticker> tickerMap;
	private Map<String, Balance> balanceMap;
	private Map<String, BigDecimal> spreadMap;
	private Map<String, OrderBook> orderBookMap;

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

		try {
			Class<? extends AbstractApiService> apiServiceClass = provider.getImplementer();
			Constructor<? extends AbstractApiService> apiServiceConstructor = apiServiceClass
					.getDeclaredConstructor(UserConfiguration.class, CoinCurrencyPair.class);

			for (CoinCurrencyPair ccp : coinCurrencyPairList) {
				AbstractApiService apiService = apiServiceConstructor.newInstance(getUserConfiguration(), ccp);
				apiServiceMap.put(ccp.toString(), apiService);
			}
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

	}

	private AbstractApiService getApiService(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		return apiServiceMap.get(coinCurrencyPair.toString());
	}

	public Ticker getTicker(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!tickerMap.containsKey(coinCurrencyPair.toString())) {
			AbstractApiService apiService = getApiService(coinCurrencyPair);
			tickerMap.put(coinCurrencyPair.toString(), apiService.getTicker());
		}
		return tickerMap.get(coinCurrencyPair.toString());
	}

	public Balance getBalance(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!balanceMap.containsKey(coinCurrencyPair.toString())) {
			AbstractApiService apiService = getApiService(coinCurrencyPair);
			balanceMap.put(coinCurrencyPair.toString(), apiService.getBalance());
		}
		return balanceMap.get(coinCurrencyPair.toString());
	}

	public BigDecimal getSpread(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!spreadMap.containsKey(coinCurrencyPair.toString())) {
			Order currentTopBuyOrder = getOrderBookBySide(coinCurrencyPair, RecordSide.BUY).get(0);
			Order currentTopSellOrder = getOrderBookBySide(coinCurrencyPair, RecordSide.SELL).get(0);

			BigDecimal currentTopBuyPrice = currentTopBuyOrder.getCurrencyPrice();
			BigDecimal currentTopSellPrice = currentTopSellOrder.getCurrencyPrice();

			BigDecimal spread = currentTopSellPrice.divide(currentTopBuyPrice, 8, RoundingMode.HALF_EVEN)
					.subtract(new BigDecimal(1.0));
			spreadMap.put(coinCurrencyPair.toString(), spread);
		}
		return spreadMap.get(coinCurrencyPair.toString());
	}

	public OrderBook getOrderBook(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!orderBookMap.containsKey(coinCurrencyPair.toString())) {
			AbstractApiService apiService = getApiService(coinCurrencyPair);
			orderBookMap.put(coinCurrencyPair.toString(), apiService.getOrderBook());
		}
		return orderBookMap.get(coinCurrencyPair.toString());
	}

	public List<Order> getOrderBookBySide(CoinCurrencyPair coinCurrencyPair, RecordSide side)
			throws ApiProviderException {
		return getOrderBook(coinCurrencyPair).getOrdersBySide(side);
	}

	public Order getCurrentTopOrder(CoinCurrencyPair coinCurrencyPair, RecordSide side) throws ApiProviderException {
		return getOrderBookBySide(coinCurrencyPair, side).get(0);
	}

	public List<Order> getUserActiveOrders(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!userActiveOrdersMap.containsKey(coinCurrencyPair.toString())) {
			AbstractApiService apiService = getApiService(coinCurrencyPair);
			userActiveOrdersMap.put(coinCurrencyPair.toString(), apiService.getUserActiveOrders());
		}
		return userActiveOrdersMap.get(coinCurrencyPair.toString());
	}

	public List<Order> getUserActiveOrders(CoinCurrencyPair coinCurrencyPair, RecordSide side)
			throws ApiProviderException {

		List<Order> orderList = new ArrayList<>();
		for (Order order : getUserActiveOrders(coinCurrencyPair))
			if (order.getSide() == side)
				orderList.add(order);

		return orderList;
	}

	public List<Operation> getUserOperations(CoinCurrencyPair coinCurrencyPair) throws ApiProviderException {
		if (!userOperationsMap.containsKey(coinCurrencyPair.toString())) {
			AbstractApiService apiService = getApiService(coinCurrencyPair);
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

	public BigDecimal getLastRelevantPriceByRecordsAndTheirPositions(CoinCurrencyPair coinCurrencyPair, RecordSide side,
			List<Record> recordList, Boolean showMessages) throws ApiProviderException {

		BigDecimal lastRelevantPriceByRecords = new BigDecimal(0);

		BigDecimal sumOfCoin = BigDecimal.valueOf(0);
		BigDecimal sumOfNumerators = BigDecimal.valueOf(0);

		List<Record> selectedList = new ArrayList<>();

		for (int i = 0; i < NUM_OF_CONSIDERED_RECORDS_FOR_LAST_RELEVANT_PRICE_BY_RECORDS_AND_THEIR_POSITIONS; i++) {
			Record record = recordList.get(i);
			sumOfCoin = sumOfCoin.add(record.getCoinAmount());
			sumOfNumerators = sumOfNumerators.add(record.getCoinAmount().multiply(record.getCurrencyPrice()));
			selectedList.add(record);
		}

		if (sumOfCoin.compareTo(BigDecimal.ZERO) != 0) {
			lastRelevantPriceByRecords = sumOfNumerators.divide(sumOfCoin, 8, RoundingMode.HALF_EVEN);
		}

		if (showMessages) {
			System.out.println(
					"  Last relevant " + side + " price by records: " + decFmt.format(lastRelevantPriceByRecords));
			System.out.println("  Considered records: ");
			for (Record record : selectedList)
				System.out.println("    " + record);
			System.out.println("");
		}

		return lastRelevantPriceByRecords;
	}

	public BigDecimal getLastRelevantPriceByRecordsAndTheirAmounts(CoinCurrencyPair coinCurrencyPair, RecordSide side,
			List<Record> recordList, Boolean showMessages) throws ApiProviderException {

		BigDecimal lastRelevantPriceByRecords = new BigDecimal(0);
		BigDecimal oldCoinAmount = new BigDecimal(0);
		BigDecimal sumOfAmount = BigDecimal.valueOf(0);

		BigDecimal otherSideAmountWithOpenOrders = getBalance(coinCurrencyPair).getSideAmount(side.getOther());

		List<Record> selectedList = new ArrayList<>();
		for (Record record : recordList) {
			BigDecimal otherSideAmount = record.getSideAmount(side.getOther());
			if (record.getSide() == side) {
				if (sumOfAmount.add(otherSideAmount).compareTo(otherSideAmountWithOpenOrders) <= 0) {
					sumOfAmount = sumOfAmount.add(otherSideAmount);
					selectedList.add(record);
				} else {
					oldCoinAmount = record.getCoinAmount();
					BigDecimal otherSideRestOfAmount = otherSideAmountWithOpenOrders.subtract(sumOfAmount);
					BigDecimal recordPrice = record.getCurrencyPrice();
					BigDecimal otherSideEstimatedCoinAmount = side.getOther()
							.getEstimatedCoinAmountByAmountAndPrice(otherSideRestOfAmount, recordPrice);

					record.setCoinAmount(otherSideEstimatedCoinAmount);
					selectedList.add(record);
					sumOfAmount = sumOfAmount.add(otherSideAmountWithOpenOrders).subtract(sumOfAmount);
					break;
				}
			}
		}
		if (sumOfAmount.compareTo(BigDecimal.ZERO) != 0) {
			for (Record record : selectedList) {
				BigDecimal otherSideAmount = record.getSideAmount(side.getOther());
				lastRelevantPriceByRecords = lastRelevantPriceByRecords.add(otherSideAmount
						.multiply(record.getCurrencyPrice()).divide(sumOfAmount, 8, RoundingMode.HALF_EVEN));
			}
		}
		if (showMessages) {
			System.out.println("  Last relevant " + side + " price: " + decFmt.format(lastRelevantPriceByRecords));
			System.out.println("  Considered records: ");
			for (Record record : selectedList)
				System.out.println("    " + record.toString());
			System.out.println("");
		}
		if (selectedList.size() > 0)
			selectedList.get(selectedList.size() - 1).setCoinAmount(oldCoinAmount);

		return lastRelevantPriceByRecords;
	}

	public BigDecimal getLastRelevantPriceByOperationsAndTheirAmounts(CoinCurrencyPair coinCurrencyPair,
			RecordSide side, Boolean showMessages) throws ApiProviderException {

		List<Record> selectedList = new ArrayList<>(getUserOperations(coinCurrencyPair));
		return getLastRelevantPriceByRecordsAndTheirAmounts(coinCurrencyPair, side, selectedList, showMessages);
	}

	public BigDecimal getLastRelevantPriceByOperationsAndTheirPositions(CoinCurrencyPair coinCurrencyPair,
			RecordSide side, Boolean showMessages) throws ApiProviderException {

		List<Record> selectedList = new ArrayList<>(getUserOperations(coinCurrencyPair));
		return getLastRelevantPriceByRecordsAndTheirPositions(coinCurrencyPair, side, selectedList, showMessages);
	}

	public BigDecimal getLastRelevantPriceByOrdersAndTheirAmounts(CoinCurrencyPair coinCurrencyPair, RecordSide side,
			Boolean showMessages) throws ApiProviderException {

		List<Record> selectedList = new ArrayList<>(getOrderBookBySide(coinCurrencyPair, side));
		return getLastRelevantPriceByRecordsAndTheirAmounts(coinCurrencyPair, side, selectedList, showMessages);
	}

	public BigDecimal getLastRelevantPriceByOrdersAndTheirPositions(CoinCurrencyPair coinCurrencyPair, RecordSide side,
			Boolean showMessages) throws ApiProviderException {

		List<Record> recordList = new ArrayList<>(getOrderBookBySide(coinCurrencyPair, side));
		return getLastRelevantPriceByRecordsAndTheirPositions(coinCurrencyPair, side, recordList, showMessages);
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

	public void makeOrdersByLastRelevantPrice(UserSideConfiguration sideConfiguration) throws ApiProviderException {

		RecordSide side = sideConfiguration.getSide();
		RecordSideMode mode = sideConfiguration.getMode();

		System.out.println("");
		System.out.println("Analising " + side + " order");
		System.out.println("");

		Boolean hasToWinCurrent = true;

		Class<? extends AbstractRecordSideModeImplementer> recordSideModeImplementerClass = mode.getImplementer();
		Constructor<? extends AbstractRecordSideModeImplementer> recordSideModeImplementerConstructor;
		try {
			recordSideModeImplementerConstructor = recordSideModeImplementerClass
					.getDeclaredConstructor(ProviderReport.class);
			AbstractRecordSideModeImplementer recordSideModeImplementer = recordSideModeImplementerConstructor
					.newInstance(this);
			recordSideModeImplementer.makeOrdersByLastRelevantPrice(this, side, hasToWinCurrent);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

	}

	public void makeOrdersByLastRelevantPrice(CoinCurrencyPair coinCurrencyPair, RecordSide side,
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

	public void cancelAllOrdersOfOtherCoinCurrencyPairsButSameSide(CoinCurrencyPair coinCurrencyPair, RecordSide side)
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

		List<Order> activeOrders = getOrderBookBySide(coinCurrencyPair, side);
		if (orderIndex >= activeOrders.size() - 1) {
			Order myOrder = getUserActiveOrders(coinCurrencyPair, side).size() > 0
					? getUserActiveOrders(coinCurrencyPair, side).get(0)
					: null;
			if (myOrder != null)
				cancelOrder(myOrder);
			BigDecimal coinAmount = getBalance(coinCurrencyPair).getEstimatedCoinAmount(side, lastRelevantPrice);

			if (coinAmount.compareTo(userConfiguration.getMinimumCoinAmount()) < 0) {
				throw new NotAvailableMoneyException();
			}

			Order newOrder = new Order(coinCurrencyPair.getCoin(), coinCurrencyPair.getCurrency(), side, coinAmount,
					lastRelevantPrice);
			newOrder.setType(OrderType.LIMITED);
			return newOrder;
		}

		Order order = activeOrders.get(orderIndex);

		BigDecimal orderCurrencyPriceToCompare = lastRelevantPrice == null ? null : order.getCurrencyPrice();

		boolean isAGoodOrder = lastRelevantPrice == null || lastRelevantPrice.compareTo(BigDecimal.ZERO) <= 0;
		if (!isAGoodOrder && orderCurrencyPriceToCompare != null)
			isAGoodOrder = side.isAGoodRecordByRecordCurrencyPriceAndLastRelevantPrice(orderCurrencyPriceToCompare,
					lastRelevantPrice);

		if (isAGoodOrder) {
			Order newOrder = tryToWinAnOrder(coinCurrencyPair, side, orderIndex);
			if (newOrder != null)
				return newOrder;
		}

		return winTheCurrentOrder(coinCurrencyPair, side, orderIndex + 1, lastRelevantPrice);
	}

	private Order winThePreviousOrder(CoinCurrencyPair coinCurrencyPair, RecordSide side, Integer orderIndex,
			BigDecimal lastRelevantPrice) throws ApiProviderException, NotAvailableMoneyException {

		List<Order> activeOrders = getOrderBookBySide(coinCurrencyPair, side);
		if (orderIndex >= activeOrders.size() - 1) {
			Order myOrder = getUserActiveOrders(coinCurrencyPair, side).size() > 0
					? getUserActiveOrders(coinCurrencyPair, side).get(0)
					: null;
			if (myOrder != null)
				cancelOrder(myOrder);
			BigDecimal coinAmount = getBalance(coinCurrencyPair).getEstimatedCoinAmount(side, lastRelevantPrice);

			if (coinAmount.compareTo(userConfiguration.getMinimumCoinAmount()) < 0) {
				throw new NotAvailableMoneyException();
			}

			Order newOrder = new Order(coinCurrencyPair.getCoin(), coinCurrencyPair.getCurrency(), side, coinAmount,
					lastRelevantPrice);
			newOrder.setType(OrderType.LIMITED);
			return newOrder;
		}

		Order order = activeOrders.get(orderIndex);

		BigDecimal orderPriceToCompare = lastRelevantPrice == null ? null : order.getCurrencyPrice();

		boolean isAGoodOrder = lastRelevantPrice == null || lastRelevantPrice.compareTo(BigDecimal.ZERO) <= 0;
		if (!isAGoodOrder && orderPriceToCompare != null)
			isAGoodOrder = side.isAGoodRecordByRecordCurrencyPriceAndLastRelevantPrice(orderPriceToCompare,
					lastRelevantPrice);

		if (isAGoodOrder) {
			Order newOrder = tryToWinAnOrder(coinCurrencyPair, side, orderIndex > 0 ? orderIndex - 1 : 0);
			if (newOrder != null)
				return newOrder;
		}

		return winThePreviousOrder(coinCurrencyPair, side, orderIndex + 1, lastRelevantPrice);
	}

	private Order tryToWinAnOrder(CoinCurrencyPair coinCurrencyPair, RecordSide side, Integer orderIndex)
			throws NotAvailableMoneyException, ApiProviderException {

		List<Order> activeOrders = getOrderBookBySide(coinCurrencyPair, side);
		List<Order> userActiveOrders = getUserActiveOrders(coinCurrencyPair, side);

		Order order = activeOrders.get(orderIndex);
		Order nextOrder = activeOrders.size() - 1 == orderIndex ? null : activeOrders.get(orderIndex + 1);
		Order bestOtherSideOrder = getCurrentTopOrder(coinCurrencyPair, side.getOther());

		BigDecimal minimumCoinAmount = userConfiguration.getMinimumCoinAmount();
		BigDecimal effectiveIncDecPrice = userConfiguration.getEffectiveIncDecPrice(side);

		BigDecimal currencyPricePlusIncDec = order.getCurrencyPrice().add(effectiveIncDecPrice);
		BigDecimal balanceCoinAmount = getBalance(coinCurrencyPair).getEstimatedCoinAmount(side,
				currencyPricePlusIncDec);

		if (balanceCoinAmount.compareTo(minimumCoinAmount) < 0) {
			throw new NotAvailableMoneyException();
		}

		// get the unique order or null
		Order myOrder = userActiveOrders.size() > 0 ? userActiveOrders.get(0) : null;
		boolean isOrderCurrencyPriceEqualsToMyOrderCurrencyPrice = myOrder != null
				&& decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(myOrder.getCurrencyPrice()));

		// if my order isn't the best, delete it and create another
		if (myOrder == null || !isOrderCurrencyPriceEqualsToMyOrderCurrencyPrice) {

			BigDecimal absDiffIncDecOrderAndBestOtherSideOrder = order.getCurrencyPrice().add(effectiveIncDecPrice)
					.subtract(bestOtherSideOrder.getCurrencyPrice()).abs();

			Boolean isNearTheBestOtherSideOrder = absDiffIncDecOrderAndBestOtherSideOrder
					.compareTo(effectiveIncDecPrice) <= 0;

			if (isNearTheBestOtherSideOrder)
				currencyPricePlusIncDec = order.getCurrencyPrice();

			Order newOrder = new Order(coinCurrencyPair.getCoin(), coinCurrencyPair.getCurrency(), side,
					balanceCoinAmount, currencyPricePlusIncDec);
			newOrder.setType(OrderType.LIMITED);
			newOrder.setPosition(orderIndex);
			return newOrder;

		} else {

			BigDecimal absDiffOrderCoinAmountAndBalanceCoinAmount = order.getCoinAmount().subtract(balanceCoinAmount)
					.abs();
			BigDecimal absDiffOrderCurrencyPriceAndNextOrderCurrencyPrice = order.getCurrencyPrice()
					.subtract(nextOrder.getCurrencyPrice()).abs();

			if (isOrderCurrencyPriceEqualsToMyOrderCurrencyPrice
					&& absDiffOrderCoinAmountAndBalanceCoinAmount.compareTo(minimumCoinAmount) <= 0
					&& absDiffOrderCurrencyPriceAndNextOrderCurrencyPrice.compareTo(effectiveIncDecPrice) <= 0) {

				myOrder.setPosition(orderIndex);
				return myOrder;

			}
		}
		return null;
	}

}