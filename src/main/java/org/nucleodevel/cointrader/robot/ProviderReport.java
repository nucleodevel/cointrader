package org.nucleodevel.cointrader.robot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.nucleodevel.cointrader.beans.Provider;
import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.beans.RecordSideMode;
import org.nucleodevel.cointrader.beans.Ticker;
import org.nucleodevel.cointrader.beans.UserConfiguration;
import org.nucleodevel.cointrader.beans.UserSideConfiguration;
import org.nucleodevel.cointrader.exception.ApiProviderException;
import org.nucleodevel.cointrader.recordsidemode.AbstractRecordSideModeImplementer;

public class ProviderReport {

	private UserConfiguration userConfiguration;

	private Map<String, AbstractApiService> apiServiceMap;
	private Map<String, Ticker> tickerMap;
	private Map<String, Balance> balanceMap;
	private Map<String, BigDecimal> spreadMap;
	private Map<String, OrderBook> orderBookMap;

	private Map<String, List<Order>> userActiveOrdersMap;
	private Map<String, List<Operation>> userOperationsMap;

	private Map<String, BigDecimal> my24hCoinVolumeMap;

	public ProviderReport(UserConfiguration userConfiguration) throws ApiProviderException {
		this.userConfiguration = userConfiguration;

		makeApiServiceMap();

		tickerMap = new HashMap<>();
		balanceMap = new HashMap<>();
		spreadMap = new HashMap<>();
		orderBookMap = new HashMap<>();

		userActiveOrdersMap = new HashMap<>();
		userOperationsMap = new HashMap<>();

		my24hCoinVolumeMap = new HashMap<>();
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

	// ------------ Operations to make orders

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

	public void tryToMakeOrders(UserSideConfiguration sideConfiguration) throws ApiProviderException {

		RecordSide side = sideConfiguration.getSide();
		RecordSideMode mode = sideConfiguration.getMode();

		System.out.println("");
		System.out.println("Analising " + side + " order");
		System.out.println("");

		Class<? extends AbstractRecordSideModeImplementer> recordSideModeImplementerClass = mode.getImplementer();
		Constructor<? extends AbstractRecordSideModeImplementer> recordSideModeImplementerConstructor;
		try {
			recordSideModeImplementerConstructor = recordSideModeImplementerClass
					.getDeclaredConstructor(ProviderReport.class, RecordSide.class);
			AbstractRecordSideModeImplementer recordSideModeImplementer = recordSideModeImplementerConstructor
					.newInstance(this, side);
			recordSideModeImplementer.tryToMakeOrders();
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

	}

}