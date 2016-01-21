package net.trader.mercadobitcoin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.mercadobitcoin.common.exception.MercadoBitcoinException;
import net.mercadobitcoin.tradeapi.service.ApiService;
import net.mercadobitcoin.tradeapi.service.TradeApiService;
import net.mercadobitcoin.tradeapi.to.AccountBalance;
import net.mercadobitcoin.tradeapi.to.Operation;
import net.mercadobitcoin.tradeapi.to.Order;
import net.mercadobitcoin.tradeapi.to.Order.CoinPair;
import net.mercadobitcoin.tradeapi.to.Order.OrderStatus;
import net.mercadobitcoin.tradeapi.to.Order.OrderType;
import net.mercadobitcoin.tradeapi.to.OrderFilter;
import net.mercadobitcoin.tradeapi.to.Orderbook;
import net.mercadobitcoin.tradeapi.to.Ticker;
import net.trader.exception.NetworkErrorException;

public class MercadoBitcoinReport {
	
	private static long intervalToReadMyCanceledOrders = 900;
	private static long totalTimeToReadMyCanceledOrders = 86400;
	private static long lastTimeByReadingMyCanceledOrders = 0;
	private static long totalTimeToReadMyCompletedOrders = 86400;

	private CoinPair coinPair;
	
	private MercadoBitcoinUserInformation userInformation;
	
	private ApiService apiService;
	private TradeApiService tradeApiService;
	
	private Ticker ticker24h;
	private AccountBalance accountBalance;
	private Orderbook orderbook;
	
	private List<Order> activeOrders;
	private List<Order> activeBuyOrders;
	private List<Order> activeSellOrders;
	private List<Operation> operations;
	
	private Order currentTopBuy;
	private Order currentTopSell;
	
	private List<Order> myOrders;
	private static List<Order> myCanceledOrders;
	private List<Order> myCompletedOrders;
	private List<Order> myActiveOrders;
	private List<Order> myActiveBuyOrders;
	private List<Order> myActiveSellOrders;
	private List<Operation> myOperations;
	
	private Operation lastBuy;
	private Operation lastSell;
	
	private BigDecimal lastRelevantBuyPrice;
	
	public MercadoBitcoinReport(CoinPair coinPair) {		
		this.coinPair = coinPair;
	}

	public CoinPair getCoinPair() {
		return coinPair;
	}
	
	public MercadoBitcoinUserInformation getUserInformation() {
		if (userInformation == null)
			userInformation = new MercadoBitcoinUserInformation();
		return userInformation;
	}

	public ApiService getApiService() throws MercadoBitcoinException {
		if (apiService == null)
			apiService = new ApiService();
		return apiService;
	}
	
	public TradeApiService getTradeApiService() throws MercadoBitcoinException {
		if (tradeApiService == null)
			tradeApiService = new TradeApiService(
				getUserInformation().getMyTapiCode(), getUserInformation().getMyTapiKey()
			);
		return tradeApiService;
	}

	public Ticker getTicker24h() throws MercadoBitcoinException {
		if (ticker24h == null)
			ticker24h = getApiService().ticker24h(coinPair);
		return ticker24h;
	}
	
	public AccountBalance getAccountBalance() throws MercadoBitcoinException, NetworkErrorException {
		if (accountBalance == null)
			accountBalance = getTradeApiService().getAccountInfo();
		return accountBalance;
	}

	public Orderbook getOrderbook() throws MercadoBitcoinException {
		if (orderbook == null)
			orderbook = getApiService().orderbook(getCoinPair());
		return orderbook;
	}

	public List<Order> getActiveOrders() throws MercadoBitcoinException {
		if (activeOrders == null) {
			activeOrders = new ArrayList<Order>();
			activeOrders.addAll(getActiveBuyOrders());
			activeOrders.addAll(getActiveSellOrders());
			Collections.sort(activeOrders);
		}
		return activeOrders;
	}

	public List<Order> getActiveBuyOrders() throws MercadoBitcoinException {
		if (activeBuyOrders == null) {
			activeBuyOrders = new ArrayList<Order>(Arrays.asList(getOrderbook().getBids()));
		}
		return activeBuyOrders;
	}

	public List<Order> getActiveSellOrders() throws MercadoBitcoinException {
		if (activeSellOrders == null) {
			activeSellOrders = new ArrayList<Order>(Arrays.asList(getOrderbook().getAsks()));
		}
		return activeSellOrders;
	}

	public List<Operation> getOperations() throws MercadoBitcoinException {
		if (operations == null) {
			Operation[] operationArray = getApiService().tradeList(coinPair);
			operations = new ArrayList<Operation>(Arrays.asList(operationArray));
			Collections.sort(operations);
		}
		return operations;
	}

	public Order getCurrentTopBuy() throws MercadoBitcoinException {
		if (currentTopBuy == null)
			currentTopBuy = getActiveBuyOrders().get(0);
		return currentTopBuy;
	}

	public Order getCurrentTopSell() throws MercadoBitcoinException {
		if (currentTopSell == null)
			currentTopSell = getActiveSellOrders().get(0);
		return currentTopSell;
	}

	public List<Order> getMyOrders() throws MercadoBitcoinException, NetworkErrorException {
		if (myOrders == null) {
			if (myCanceledOrders == null)
				System.out.println("The first reading can take a lot of seconds. Please wait!");
			myOrders = new ArrayList<Order>();
			myOrders.addAll(getMyActiveOrders());
			myOrders.addAll(getMyCompletedOrders());
			myOrders.addAll(getMyCanceledOrders());
			Collections.sort(myOrders);
		}
		return myOrders;
	}
	
	public List<Order> getMyCanceledOrders() throws MercadoBitcoinException, NetworkErrorException {
		long now = (new Date()).getTime() / 1000;
		
		OrderFilter orderFilter = new OrderFilter(coinPair);
		orderFilter.setStatus(OrderStatus.CANCELED);
		
		if (myCanceledOrders == null) {
			myCanceledOrders = new ArrayList<Order>();
			
			for (long time = now; time > now - totalTimeToReadMyCanceledOrders; time -= intervalToReadMyCanceledOrders) {
				orderFilter.setSince(time - intervalToReadMyCanceledOrders - 1);
				orderFilter.setEnd(now);
				List<Order> orders = getTradeApiService().listOrders(orderFilter);
				for (Order order: orders) {
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
			for (Order order: orders) {
				if (order.getOperations() != null && order.getOperations().size() > 0) {
					myCanceledOrders.add(i, order);
					i++;
				}
			}
		}
		lastTimeByReadingMyCanceledOrders = now;
		
		return myCanceledOrders;
	}

	public List<Order> getMyCompletedOrders() throws MercadoBitcoinException, NetworkErrorException {
		if (myCompletedOrders == null) {
			
			// lê uma semana de ordens completas
			long now = (new Date()).getTime() / 1000;			
			OrderFilter orderFilter = new OrderFilter(coinPair);
			orderFilter.setStatus(OrderStatus.COMPLETED);
			orderFilter.setSince(now - totalTimeToReadMyCompletedOrders);
			orderFilter.setEnd(now);
			
			myCompletedOrders = getTradeApiService().listOrders(orderFilter);
			Collections.sort(myCompletedOrders);
			
		}
		return myCompletedOrders;
	}

	public List<Order> getMyActiveOrders() throws MercadoBitcoinException, NetworkErrorException {
		if (myActiveOrders == null) {
			OrderFilter orderFilter = new OrderFilter(coinPair);			
			orderFilter.setStatus(OrderStatus.ACTIVE);
			myActiveOrders = getTradeApiService().listOrders(orderFilter);
			Collections.sort(myActiveOrders);
		}
		return myActiveOrders;
	}

	public List<Order> getMyActiveBuyOrders() throws MercadoBitcoinException, NetworkErrorException {
		if (myActiveBuyOrders == null) {
			myActiveBuyOrders = new ArrayList<Order>();
			for (Order order: getMyActiveOrders())
				if (order.getType() == OrderType.BUY)
					myActiveBuyOrders.add(order);
		}
		return myActiveBuyOrders;
	}

	public List<Order> getMyActiveSellOrders() throws MercadoBitcoinException, NetworkErrorException {
		if (myActiveSellOrders == null) {
			myActiveSellOrders = new ArrayList<Order>();
			for (Order order: getMyActiveOrders())
				if (order.getType() == OrderType.SELL)
					myActiveSellOrders.add(order);
		}
		return myActiveSellOrders;
	}

	public List<Operation> getMyOperations() throws MercadoBitcoinException, NetworkErrorException {
		if (myOperations == null) {
			myOperations = new ArrayList<Operation>();
			for (Order order: getMyOrders())
				if (order.getOperations() != null)
					for (Operation operation: order.getOperations()) {
						operation.setType(order.getType());
						myOperations.add(operation);
					}
		}
		return myOperations;
	}

	public Operation getLastBuy() throws MercadoBitcoinException, NetworkErrorException {
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
	
	public Operation getLastSell() throws MercadoBitcoinException, NetworkErrorException {
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
	
	public BigDecimal getLastRelevantBuyPrice() throws MercadoBitcoinException, NetworkErrorException {
		if (lastRelevantBuyPrice == null) {
			
			lastRelevantBuyPrice = new BigDecimal(0);
			
			double btcWithOpenOrders = 
				getAccountBalance().getFunds().getBtcWithOpenOrders().doubleValue();
			
			List<Operation> groupOfOperations = new ArrayList<Operation>(); 
			double sumOfBtc = 0;
			
			for (Operation operation: getMyOperations()) {
				if (operation.getType() == OrderType.BUY) {
					if (sumOfBtc + operation.getAmount().doubleValue() <= btcWithOpenOrders) {
						sumOfBtc += operation.getAmount().doubleValue();
						groupOfOperations.add(operation);
					}
					else {
						Operation newOperation = new Operation(operation);
						newOperation.setAmount(new BigDecimal(btcWithOpenOrders - sumOfBtc));
						groupOfOperations.add(newOperation);
						sumOfBtc += btcWithOpenOrders - sumOfBtc;
						break;
					}
				}
			}
			if (sumOfBtc != 0) {
				for (Operation operation: groupOfOperations) {
					System.out.println(sumOfBtc);
					lastRelevantBuyPrice = new BigDecimal(
						lastRelevantBuyPrice.doubleValue() +	
						(operation.getAmount().doubleValue() * 
						operation.getPrice().doubleValue() / sumOfBtc)
					); 
				}
			}
			System.out.println("Calculating last relevant buy price: ");
			System.out.println("  BTC with open orders: " + btcWithOpenOrders);
			System.out.println("  Considered BTC sum: " + sumOfBtc);
			System.out.println("  Considered buy orders: " + groupOfOperations.size());
			System.out.println("  Last relevant buy price: " + lastRelevantBuyPrice);
			System.out.println("  Considered operations: ");
			for (Operation operation: groupOfOperations)
				System.out.println("    " + operation); 
		}
		return lastRelevantBuyPrice;
	}

}
