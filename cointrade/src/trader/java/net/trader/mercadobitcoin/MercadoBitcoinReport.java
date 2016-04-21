package net.trader.mercadobitcoin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.mercadobitcoin.tradeapi.service.ApiService;
import net.mercadobitcoin.tradeapi.service.TradeApiService;
import net.mercadobitcoin.tradeapi.to.AccountBalance;
import net.mercadobitcoin.tradeapi.to.Operation;
import net.mercadobitcoin.tradeapi.to.MbOrder;
import net.mercadobitcoin.tradeapi.to.MbOrder.CoinPair;
import net.mercadobitcoin.tradeapi.to.MbOrder.OrderStatus;
import net.mercadobitcoin.tradeapi.to.MbOrder.OrderType;
import net.mercadobitcoin.tradeapi.to.OrderFilter;
import net.mercadobitcoin.tradeapi.to.Orderbook;
import net.mercadobitcoin.tradeapi.to.Ticker;
import net.trader.beans.Order;
import net.trader.exception.ApiProviderException;
import net.trader.robot.RobotReport;
import net.trader.robot.UserConfiguration;

public class MercadoBitcoinReport extends RobotReport {

	private static long intervalToReadMyCanceledOrders = 1200;
	private static long totalTimeToReadMyCanceledOrders = 86400;
	private static long lastTimeByReadingMyCanceledOrders = 0;
	private static long totalTimeToReadMyCompletedOrders = 43200;
	private static long numOfConsideredOrdersForLastRelevantSellPrice = 3;
	
	private ApiService apiService;
	private TradeApiService tradeApiService;
	
	private Ticker ticker24h;
	private AccountBalance accountBalance;
	private Orderbook orderbook;
	
	private List<MbOrder> activeOrders;
	private List<MbOrder> activeBuyOrders;
	private List<MbOrder> activeSellOrders;
	private List<Operation> operations;
	
	private MbOrder currentTopBuy;
	private MbOrder currentTopSell;
	
	private List<MbOrder> myOrders;
	private static List<MbOrder> myCanceledOrders;
	private List<MbOrder> myCompletedOrders;
	private List<MbOrder> myActiveOrders;
	private List<MbOrder> myActiveBuyOrders;
	private List<MbOrder> myActiveSellOrders;
	private List<Operation> myOperations;
	
	private Operation lastBuy;
	private Operation lastSell;
	
	private BigDecimal lastRelevantBuyPrice;
	private BigDecimal lastRelevantSellPrice;
	
	public MercadoBitcoinReport(UserConfiguration userConfiguration, String currency, String coin) {
		super(userConfiguration, currency, coin);
	}
	
	public CoinPair getCoinPair() {
		return getCurrency().equals("BRL") && getCoin().equals("BTC")?
			CoinPair.BTC_BRL: null;
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
			ticker24h = getApiService().ticker24h(getCoinPair());
		return ticker24h;
	}
	
	public AccountBalance getAccountBalance() throws ApiProviderException {
		if (accountBalance == null)
			accountBalance = getTradeApiService().getAccountInfo();
		return accountBalance;
	}
	
	public BigDecimal getCurrencyAmount() throws ApiProviderException {
		BigDecimal currencyAmount;
		if (getCurrency().equals("BRL"))
			currencyAmount = getAccountBalance().getFunds().getBrlWithOpenOrders();
		else
			currencyAmount = null;
		return currencyAmount;
	}
	
	public BigDecimal getCoinAmount() throws ApiProviderException {
		BigDecimal coinAmount;
		if (getCoin().equals("BTC"))
			coinAmount = getAccountBalance().getFunds().getBtcWithOpenOrders();
		else if (getCoin().equals("LTC"))
			coinAmount = getAccountBalance().getFunds().getLtcWithOpenOrders();
		else
			coinAmount = null;
		return coinAmount;
	}

	public Orderbook getOrderbook() throws ApiProviderException {
		if (orderbook == null)
			orderbook = getApiService().orderbook(getCoinPair());
		return orderbook;
	}

	public List<MbOrder> getActiveOrders() throws ApiProviderException {
		if (activeOrders == null) {
			activeOrders = new ArrayList<MbOrder>();
			activeOrders.addAll(getActiveBuyOrders());
			activeOrders.addAll(getActiveSellOrders());
			Collections.sort(activeOrders);
		}
		return activeOrders;
	}

	public List<MbOrder> getActiveBuyOrders() throws ApiProviderException {
		if (activeBuyOrders == null) {
			activeBuyOrders = new ArrayList<MbOrder>(Arrays.asList(getOrderbook().getBids()));
		}
		return activeBuyOrders;
	}

	public List<MbOrder> getActiveSellOrders() throws ApiProviderException {
		if (activeSellOrders == null) {
			activeSellOrders = new ArrayList<MbOrder>(Arrays.asList(getOrderbook().getAsks()));
		}
		return activeSellOrders;
	}

	public List<Operation> getOperations() throws ApiProviderException {
		if (operations == null) {
			Operation[] operationArray = getApiService().tradeList(getCoinPair());
			operations = new ArrayList<Operation>(Arrays.asList(operationArray));
			Collections.sort(operations);
		}
		return operations;
	}

	public MbOrder getCurrentTopBuy() throws ApiProviderException {
		if (currentTopBuy == null)
			currentTopBuy = getActiveBuyOrders().get(0);
		return currentTopBuy;
	}

	public MbOrder getCurrentTopSell() throws ApiProviderException {
		if (currentTopSell == null)
			currentTopSell = getActiveSellOrders().get(0);
		return currentTopSell;
	}

	public List<MbOrder> getMyOrders() throws ApiProviderException {
		if (myOrders == null) {
			if (myCanceledOrders == null)
				System.out.println("The first reading can take a lot of seconds. Please wait!");
			myOrders = new ArrayList<MbOrder>();
			myOrders.addAll(getMyActiveOrders());
			myOrders.addAll(getMyCompletedOrders());
			myOrders.addAll(getMyCanceledOrders());
			Collections.sort(myOrders);
		}
		return myOrders;
	}
	
	public List<MbOrder> getMyCanceledOrders() throws ApiProviderException {
		long now = (new Date()).getTime() / 1000;
		
		OrderFilter orderFilter = new OrderFilter(getCoinPair());
		orderFilter.setStatus(OrderStatus.CANCELED);
		
		if (myCanceledOrders == null) {
			myCanceledOrders = new ArrayList<MbOrder>();
			
			for (long time = now; time > now - totalTimeToReadMyCanceledOrders; time -= intervalToReadMyCanceledOrders) {
				orderFilter.setSince(time - intervalToReadMyCanceledOrders);
				orderFilter.setEnd(time - 1);
				List<MbOrder> orders = getTradeApiService().listOrders(orderFilter);
				for (MbOrder order: orders) {
					if (order.getOperations() != null && order.getOperations().size() > 0)
						myCanceledOrders.add(order);
				}
			}
			Collections.sort(myCanceledOrders);
		}
		else {
			orderFilter.setSince(lastTimeByReadingMyCanceledOrders + 1);
			orderFilter.setEnd(now);
			
			List<MbOrder> orders = getTradeApiService().listOrders(orderFilter);
			int i = 0;
			for (MbOrder order: orders) {
				if (order.getOperations() != null && order.getOperations().size() > 0) {
					myCanceledOrders.add(i, order);
					i++;
				}
			}
		}
		lastTimeByReadingMyCanceledOrders = now;
		
		return myCanceledOrders;
	}

	public List<MbOrder> getMyCompletedOrders() throws ApiProviderException {
		if (myCompletedOrders == null) {
			
			// lê uma semana de ordens completas
			long now = (new Date()).getTime() / 1000;			
			OrderFilter orderFilter = new OrderFilter(getCoinPair());
			orderFilter.setStatus(OrderStatus.COMPLETED);
			orderFilter.setSince(now - totalTimeToReadMyCompletedOrders);
			orderFilter.setEnd(now);
			
			myCompletedOrders = getTradeApiService().listOrders(orderFilter);
			Collections.sort(myCompletedOrders);
			
		}
		return myCompletedOrders;
	}

	public List<MbOrder> getMyActiveOrders() throws ApiProviderException {
		if (myActiveOrders == null) {
			OrderFilter orderFilter = new OrderFilter(getCoinPair());			
			orderFilter.setStatus(OrderStatus.ACTIVE);
			myActiveOrders = getTradeApiService().listOrders(orderFilter);
			Collections.sort(myActiveOrders);
		}
		return myActiveOrders;
	}

	public List<MbOrder> getMyActiveBuyOrders() throws ApiProviderException {
		if (myActiveBuyOrders == null) {
			myActiveBuyOrders = new ArrayList<MbOrder>();
			for (MbOrder order: getMyActiveOrders())
				if (order.getType() == OrderType.BUY)
					myActiveBuyOrders.add(order);
		}
		return myActiveBuyOrders;
	}

	public List<MbOrder> getMyActiveSellOrders() throws ApiProviderException {
		if (myActiveSellOrders == null) {
			myActiveSellOrders = new ArrayList<MbOrder>();
			for (MbOrder order: getMyActiveOrders())
				if (order.getType() == OrderType.SELL)
					myActiveSellOrders.add(order);
		}
		return myActiveSellOrders;
	}

	public List<Operation> getMyOperations() throws ApiProviderException {
		if (myOperations == null) {
			myOperations = new ArrayList<Operation>();
			for (MbOrder order: getMyOrders())
				if (order.getOperations() != null)
					for (Operation operation: order.getOperations()) {
						operation.setType(order.getType());
						myOperations.add(operation);
					}
		}
		return myOperations;
	}

	public Operation getLastBuy() throws ApiProviderException {
		if (lastBuy == null) {
			lastBuy = null;
			for (Operation operation: getMyOperations()) {
				if (lastBuy != null)
					break;
				if (operation.getType() == OrderType.BUY)
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
				if (operation.getType() == OrderType.SELL)
					lastSell = operation;
			}
		}
		return lastSell;
	}
	
	public BigDecimal getLastRelevantBuyPrice() throws ApiProviderException {
		if (lastRelevantBuyPrice == null) {
			
			lastRelevantBuyPrice = new BigDecimal(0);
			
			double coinWithOpenOrders = getCoinAmount().doubleValue();
			
			List<Operation> groupOfOperations = new ArrayList<Operation>(); 
			double sumOfCoin = 0;
			
			for (Operation operation: getMyOperations()) {
				if (operation.getType() == OrderType.BUY) {
					if (sumOfCoin + operation.getAmount().doubleValue() <= coinWithOpenOrders) {
						sumOfCoin += operation.getAmount().doubleValue();
						groupOfOperations.add(operation);
					}
					else {
						Operation newOperation = new Operation(operation);
						newOperation.setAmount(new BigDecimal(coinWithOpenOrders - sumOfCoin));
						groupOfOperations.add(newOperation);
						sumOfCoin += coinWithOpenOrders - sumOfCoin;
						break;
					}
				}
			}
			if (sumOfCoin != 0) {
				for (Operation operation: groupOfOperations) {
					lastRelevantBuyPrice = new BigDecimal(
						lastRelevantBuyPrice.doubleValue() +	
						(operation.getAmount().doubleValue() * 
						operation.getPrice().doubleValue() / sumOfCoin)
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
				System.out.print("    " + operation); 
			System.out.println("");
		}
		return lastRelevantBuyPrice;
	}
	
	public BigDecimal getLastRelevantSellPrice() throws ApiProviderException {
		if (lastRelevantSellPrice == null) {
			
			lastRelevantSellPrice = new BigDecimal(0);
			
			double sumOfCoin = 0;
			double sumOfNumerators = 0;
			
			List<MbOrder> groupOfOrders = new ArrayList<MbOrder>();
			
			for (int i = 0; i < numOfConsideredOrdersForLastRelevantSellPrice; i++) {
				MbOrder order = getActiveSellOrders().get(i);				
				sumOfCoin +=  order.getVolume().doubleValue();
				sumOfNumerators += 
					order.getVolume().doubleValue() * order.getPrice().doubleValue();
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
			for (MbOrder order: groupOfOrders)
				System.out.print("    " + order); 
			System.out.println("");
		}
		return lastRelevantSellPrice;
	}
	
	@Override
	public void cancelOrder(Order order) throws ApiProviderException {
		getTradeApiService().cancelOrder((MbOrder) order);
	}
	
	@Override
	public void createBuyOrder(BigDecimal currency, BigDecimal coin) throws ApiProviderException {
		getTradeApiService().createBuyOrder(
			getCoinPair(), coin.toString(), currency.toString()
		);
	}

	@Override
	public void createSellOrder(BigDecimal currency, BigDecimal coin) throws ApiProviderException {
		getTradeApiService().createSellOrder(
			getCoinPair(), coin.toString(), currency.toString()
		);
	}

}
