package net.trader.blinktrade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import net.trader.beans.Order;
import net.trader.beans.Order.OrderSide;
import net.trader.exception.ApiProviderException;
import net.trader.robot.RobotReport;
import net.trader.robot.UserConfiguration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.eti.claudiney.blinktrade.api.BlinktradeAPI;
import br.eti.claudiney.blinktrade.api.beans.Balance;
import br.eti.claudiney.blinktrade.api.beans.BlinktradeCurrency;
import br.eti.claudiney.blinktrade.api.beans.BtOpenOrder;
import br.eti.claudiney.blinktrade.api.beans.OrderBookResponse;
import br.eti.claudiney.blinktrade.api.beans.BtSimpleOrder;
import br.eti.claudiney.blinktrade.enums.BlinktradeBroker;
import br.eti.claudiney.blinktrade.enums.BlinktradeOrderSide;
import br.eti.claudiney.blinktrade.enums.BlinktradeOrderType;
import br.eti.claudiney.blinktrade.enums.BlinktradeSymbol;
import br.eti.claudiney.blinktrade.utils.Utils;

public class BlinktradeReport extends RobotReport {
	
	private BlinktradeAPI api;
	
	private Balance balance;
	private OrderBookResponse orderBook;
	
	public BlinktradeReport(UserConfiguration userConfiguration, String currency, String coin) {
		super(userConfiguration, currency, coin);
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
	
	public Balance getBalance() throws ApiProviderException {
		if (balance == null) {
			balance = new Balance();
			String response = getApi().getBalance(new Integer((int)(System.currentTimeMillis()/1000)));
			JsonParser jsonParser = new JsonParser();
	        JsonObject jo = (JsonObject)jsonParser.parse(response);
	        
	        balance.setClientID(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonPrimitive("ClientID").getAsString());
	        balance.setBalanceRequestID(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonPrimitive("BalanceReqID").getAsInt());
	        
	        balance.setCurrencyAmount(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive(getCurrency()).getAsBigDecimal());
	        balance.setCurrencyLocked(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive(getCurrency() + "_locked").getAsBigDecimal());
	        balance.setBtcAmount(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive("BTC").getAsBigInteger());
	        balance.setBtcLocked(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive("BTC_locked").getAsBigInteger());
		}
		return balance;
	}
	
	public BigDecimal getCurrencyAmount() throws ApiProviderException, Exception {
		return getBalance().getCurrencyAmount();
	}
	
	public BigDecimal getCoinAmount() throws ApiProviderException {
		BigDecimal coinAmount;
		if (getCoin().equals("BTC"))
			coinAmount = new BigDecimal(getBalance().getBtcAmount().doubleValue());
		else
			coinAmount = null;
		return coinAmount;
	}
	
	public OrderBookResponse getOrderBook() throws ApiProviderException {
		if (orderBook == null)
			orderBook = getApi().getOrderBook();
		return orderBook;
	}

	@Override
	public List<Order> getActiveBuyOrders() throws ApiProviderException {
		if (activeBuyOrders == null) {
			activeBuyOrders = getOrderBook().getBids();
		}
		return activeBuyOrders;
	}

	@Override
	public List<Order> getActiveSellOrders() throws ApiProviderException {
		if (activeSellOrders == null) {
			activeSellOrders = getOrderBook().getAsks();
		}
		return activeSellOrders;
	}

	@Override
	public Order getCurrentTopBuy() throws ApiProviderException {
		if (currentTopBuy == null)
			currentTopBuy = getActiveBuyOrders().get(0);
		return currentTopBuy;
	}

	@Override
	public Order getCurrentTopSell() throws ApiProviderException {
		if (currentTopSell == null)
			currentTopSell = getActiveSellOrders().get(0);
		return currentTopSell;
	}

	@Override
	public List<Order> getMyActiveOrders() throws ApiProviderException {
		if (activeOrders == null) {
			String response = getApi().requestOpenOrders(new Integer((int)(System.currentTimeMillis()/1000)));
			JsonParser jsonParser = new JsonParser();
			JsonObject jo = (JsonObject) jsonParser.parse(response);
			JsonArray openOrdListGrp = jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonArray("OrdListGrp");
			activeOrders = new ArrayList<Order>();
			if(openOrdListGrp != null) {
				for (JsonElement o: openOrdListGrp) {
					if (o != null) {
						BtOpenOrder oo = new BtOpenOrder();
						activeOrders.add(oo);
						JsonArray objArray = o.getAsJsonArray();
						oo.setClientCustomOrderID(objArray.get(0).getAsBigInteger());
						oo.setOrderID(objArray.get(1).getAsString());
						oo.setCumQty(objArray.get(2).getAsBigDecimal());
						oo.setOrdStatus(objArray.get(3).getAsString());
						oo.setLeavesQty(objArray.get(4).getAsBigDecimal());
						oo.setCxlQty(objArray.get(5).getAsBigDecimal());
						oo.setAvgPx(objArray.get(6).getAsBigDecimal());
						oo.setSymbol(objArray.get(7).getAsString());
						String sideString = objArray.get(8).getAsString();
						OrderSide side = sideString.equals("1")? OrderSide.BUY:
							(sideString.equals("2")? OrderSide.SELL: null);
						oo.setSide(side);
						oo.setOrdType(objArray.get(9).getAsString());
						oo.setOrderQty(objArray.get(10).getAsBigDecimal());
						
						BlinktradeCurrency c = BlinktradeCurrency.getCurrencyBySimbol(oo.getSymbol());
						oo.setCurrencyPrice(objArray.get(11).getAsBigDecimal().divide(
								c.getRate(),
								c.getRateSize(), RoundingMode.DOWN) );
						oo.setOrderDate( Utils.getCalendar(objArray.get(12).getAsString()));
						oo.setVolume(objArray.get(13).getAsBigDecimal());
						oo.setTimeInForce(objArray.get(14).getAsString());
					}
				}
			}
		}
		
		return activeOrders;
		
	}

	@Override
	public List<Order> getMyCompletedOrders() throws ApiProviderException {
		if (completedOrders == null) {
			String response = getApi().requestCompletedOrders(new Integer((int)(System.currentTimeMillis()/1000)));
			JsonParser jsonParser = new JsonParser();
			JsonObject jo = (JsonObject) jsonParser.parse(response);
			JsonArray completedOrdListGrp = jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonArray("OrdListGrp");
			completedOrders = new ArrayList<Order>();
			if(completedOrdListGrp != null) {
				for (JsonElement o: completedOrdListGrp) {
					if (o != null) {
						BtOpenOrder oo = new BtOpenOrder();
						completedOrders.add(oo);
						JsonArray objArray = o.getAsJsonArray();
						oo.setClientCustomOrderID(objArray.get(0).getAsBigInteger());
						oo.setOrderID(objArray.get(1).getAsString());
						oo.setCumQty(objArray.get(2).getAsBigDecimal());
						oo.setOrdStatus(objArray.get(3).getAsString());
						oo.setLeavesQty(objArray.get(4).getAsBigDecimal());
						oo.setCxlQty(objArray.get(5).getAsBigDecimal());
						oo.setAvgPx(objArray.get(6).getAsBigDecimal());
						oo.setSymbol(objArray.get(7).getAsString());
						String sideString = objArray.get(8).getAsString();
						OrderSide side = sideString.equals("1")? OrderSide.BUY:
							(sideString.equals("2")? OrderSide.SELL: null);
						oo.setSide(side);
						oo.setOrdType(objArray.get(9).getAsString());
						oo.setOrderQty(objArray.get(10).getAsBigDecimal());
						
						BlinktradeCurrency c = BlinktradeCurrency.getCurrencyBySimbol(oo.getSymbol());
						oo.setCurrencyPrice(objArray.get(11).getAsBigDecimal().divide(
								c.getRate(),
								c.getRateSize(), RoundingMode.DOWN) );
						oo.setOrderDate( Utils.getCalendar(objArray.get(12).getAsString()));
						oo.setVolume(objArray.get(13).getAsBigDecimal());
						oo.setTimeInForce(objArray.get(14).getAsString());
					}
				}
			}
		}
		
		return completedOrders;
		
	}
	
	public Order getLastBuy() throws ApiProviderException {
		for (Order o: getMyCompletedOrders()) {
			BtOpenOrder order = (BtOpenOrder) o;
			if (order.getSide().equals("1"))
				return order;
		}
		
		return null;
	}
	
	public Order getLastSell() throws ApiProviderException {
		for (Order o: getMyCompletedOrders()) {
			BtOpenOrder order = (BtOpenOrder) o;
			if (order.getSide().equals("2"))
				return order;
		}
		
		return null;
	}
	
	@Override
	public BigDecimal getLastRelevantBuyPrice() throws ApiProviderException {
		if (lastRelevantBuyPrice == null) {
			
			lastRelevantBuyPrice = new BigDecimal(0);
			
			double coinWithOpenOrders = getCoinAmount().doubleValue();
			
			List<BtOpenOrder> groupOfOperations = new ArrayList<BtOpenOrder>(); 
			double sumOfCoin = 0;
			
			for (Order o: getMyCompletedOrders()) {
				BtOpenOrder operation = (BtOpenOrder) o;
				if (operation.getSide().equals("1")) {
					if (sumOfCoin + operation.getCumQty().doubleValue() <= coinWithOpenOrders) {
						sumOfCoin += operation.getCumQty().doubleValue();
						groupOfOperations.add(operation);
					}
					else {
						BtOpenOrder newOperation = new BtOpenOrder(operation);
						newOperation.setCumQty(new BigDecimal(coinWithOpenOrders - sumOfCoin));
						groupOfOperations.add(newOperation);
						sumOfCoin += coinWithOpenOrders - sumOfCoin;
						break;
					}
				}
			}
			if (sumOfCoin != 0) {
				for (BtOpenOrder operation: groupOfOperations) {
					lastRelevantBuyPrice = new BigDecimal(
						lastRelevantBuyPrice.doubleValue() +	
						(operation.getCumQty().doubleValue() * 
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
			for (BtOpenOrder operation: groupOfOperations)
				System.out.println("    " + operation.toDisplayString()); 
			System.out.println("");
		}
		return lastRelevantBuyPrice;
	}
	
	@Override
	public BigDecimal getLastRelevantSellPrice() throws ApiProviderException {
		if (lastRelevantSellPrice == null) {
			
			lastRelevantSellPrice = new BigDecimal(0);
			
			double sumOfCoin = 0;
			double sumOfNumerators = 0;
			
			List<BtSimpleOrder> groupOfOrders = new ArrayList<BtSimpleOrder>();
			
			for (int i = 0; i < numOfConsideredOrdersForLastRelevantSellPrice; i++) {
				BtSimpleOrder order = (BtSimpleOrder) getActiveSellOrders().get(i);				
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
			for (BtSimpleOrder order: groupOfOrders)
				System.out.println("    " + order); 
			System.out.println("");
		}
		return lastRelevantSellPrice;
	}

	@Override
	public List<Order> getMyActiveBuyOrders() throws ApiProviderException {
		if (myActiveBuyOrders == null) {
			myActiveBuyOrders = new ArrayList<Order>();
			for (Order o: getMyCompletedOrders()) {
				BtOpenOrder order = (BtOpenOrder) o;
				if (order.getSide().equals("1"))
					myActiveBuyOrders.add(order);
			}
		}
		return myActiveBuyOrders;
	}

	@Override
	public List<Order> getMyActiveSellOrders() throws ApiProviderException {
		if (myActiveSellOrders == null) {
			myActiveSellOrders = new ArrayList<Order>();
			for (Order o: getMyCompletedOrders()) {
				BtOpenOrder order = (BtOpenOrder) o;
				if (order.getSide().equals("2"))
					myActiveSellOrders.add(order);
			}
		}
		return myActiveSellOrders;
	}
	
	@Override
	public void cancelOrder(Order order) throws ApiProviderException {
		getApi().cancelOrder((BtOpenOrder) order);
	}
	
	@Override
	public void createBuyOrder(BigDecimal currency, BigDecimal coin) throws ApiProviderException {
		getApi().sendNewOrder(
			new Integer((int)(System.currentTimeMillis()/1000)),
			getCoinPair(),
			BlinktradeOrderSide.BUY,
			BlinktradeOrderType.LIMITED,
			currency, coin.toBigInteger()
		);
	}
	
	@Override
	public void createSellOrder(BigDecimal currency, BigDecimal coin) throws ApiProviderException {
		getApi().sendNewOrder(
			new Integer((int)(System.currentTimeMillis()/1000)),
			getCoinPair(),
			BlinktradeOrderSide.SELL,
			BlinktradeOrderType.LIMITED,
			currency, coin.toBigInteger()
		);
	}

}
