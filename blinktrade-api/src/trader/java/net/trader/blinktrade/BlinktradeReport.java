package net.trader.blinktrade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.eti.claudiney.blinktrade.api.BlinktradeAPI;
import br.eti.claudiney.blinktrade.api.beans.Ask;
import br.eti.claudiney.blinktrade.api.beans.Balance;
import br.eti.claudiney.blinktrade.api.beans.Bid;
import br.eti.claudiney.blinktrade.api.beans.BlinktradeCurrency;
import br.eti.claudiney.blinktrade.api.beans.OpenOrder;
import br.eti.claudiney.blinktrade.api.beans.OrderBookResponse;
import br.eti.claudiney.blinktrade.api.beans.SimpleOrder;
import br.eti.claudiney.blinktrade.enums.BlinktradeSymbol;
import br.eti.claudiney.blinktrade.exception.BlinktradeAPIException;
import br.eti.claudiney.blinktrade.utils.Utils;

public class BlinktradeReport {
	
	private static long numOfConsideredOrdersForLastRelevantSellPrice = 5;
	
	private BlinktradeUserInformation userInformation;
	private BlinktradeSymbol blinktradeSymbol;
	
	private BlinktradeAPI api;
	
	private Balance balance;
	private OrderBookResponse orderBook;
	
	private List<Bid> activeBuyOrders;
	private List<Ask> activeSellOrders;
	
	private SimpleOrder currentTopBuy;
	private SimpleOrder currentTopSell;

	private List<OpenOrder> myActiveOrders;
	private List<OpenOrder> myActiveBuyOrders;
	private List<OpenOrder> myActiveSellOrders;
	
	private List<OpenOrder> openOrders;
	private List<OpenOrder> completedOrders;
	
	private BigDecimal lastRelevantBuyPrice;
	private BigDecimal lastRelevantSellPrice;
	
	
	
	public BlinktradeReport(BlinktradeSymbol blinktradeSymbol) {
		this.blinktradeSymbol = blinktradeSymbol;
	}

	public BlinktradeSymbol getBlinktradeSymbol() {
		return blinktradeSymbol;
	}

	public void setBlinktradeSymbol(BlinktradeSymbol blinktradeSymbol) {
		this.blinktradeSymbol = blinktradeSymbol;
	}

	public BlinktradeUserInformation getUserInformation() {
		if (userInformation == null)
			userInformation = new BlinktradeUserInformation();
		return userInformation;
	}
	
	public BlinktradeAPI getApi() throws BlinktradeAPIException {
		if (api == null)
			api = new BlinktradeAPI(
				getUserInformation().getMyApiKey(), getUserInformation().getMyApiSecret(), 
				getUserInformation().getBroker()
			);
		return api;
	}
	
	public Balance getBalance() throws BlinktradeAPIException, Exception {
		if (balance == null) {
			balance = new Balance();
			String response = getApi().getBalance(new Integer((int)(System.currentTimeMillis()/1000)));
			JsonParser jsonParser = new JsonParser();
	        JsonObject jo = (JsonObject)jsonParser.parse(response);
	        
	        balance.setClientID(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonPrimitive("ClientID").getAsString());
	        balance.setBalanceRequestID(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonPrimitive("BalanceReqID").getAsInt());
	        
	        balance.setCurrencyAmount(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive("BRL").getAsBigDecimal());
	        balance.setCurrencyLocked(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive("BRL_locked").getAsBigDecimal());
	        balance.setBtcAmount(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive("BTC").getAsBigInteger());
	        balance.setBtcLocked(jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive("BTC_locked").getAsBigInteger());
		}
		return balance;
	}
	
	public OrderBookResponse getOrderBook() throws BlinktradeAPIException {
		if (orderBook == null)
			orderBook = getApi().getOrderBook();
		return orderBook;
	}

	public List<Bid> getActiveBuyOrders() throws BlinktradeAPIException {
		if (activeBuyOrders == null) {
			activeBuyOrders = getOrderBook().getBids();
		}
		return activeBuyOrders;
	}

	public List<Ask> getActiveSellOrders() throws BlinktradeAPIException {
		if (activeSellOrders == null) {
			activeSellOrders = getOrderBook().getAsks();
		}
		return activeSellOrders;
	}

	public SimpleOrder getCurrentTopBuy() throws BlinktradeAPIException {
		if (currentTopBuy == null)
			currentTopBuy = getActiveBuyOrders().get(0);
		return currentTopBuy;
	}

	public SimpleOrder getCurrentTopSell() throws BlinktradeAPIException {
		if (currentTopSell == null)
			currentTopSell = getActiveSellOrders().get(0);
		return currentTopSell;
	}

	public List<OpenOrder> getOpenOrders() throws BlinktradeAPIException {
		if (openOrders == null) {
			String response = getApi().requestOpenOrders(new Integer((int)(System.currentTimeMillis()/1000)));
			JsonParser jsonParser = new JsonParser();
			JsonObject jo = (JsonObject) jsonParser.parse(response);
			JsonArray openOrdListGrp = jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonArray("OrdListGrp");
			openOrders = new ArrayList<OpenOrder>();
			if(openOrdListGrp != null) {
				for (JsonElement o: openOrdListGrp) {
					if (o != null) {
						OpenOrder oo = new OpenOrder();
						openOrders.add(oo);
						JsonArray objArray = o.getAsJsonArray();
						oo.setClientCustomOrderID(objArray.get(0).getAsBigInteger());
						oo.setOrderID(objArray.get(1).getAsString());
						oo.setCumQty(objArray.get(2).getAsBigDecimal());
						oo.setOrdStatus(objArray.get(3).getAsString());
						oo.setLeavesQty(objArray.get(4).getAsBigDecimal());
						oo.setCxlQty(objArray.get(5).getAsBigDecimal());
						oo.setAvgPx(objArray.get(6).getAsBigDecimal());
						oo.setSymbol(objArray.get(7).getAsString());
						oo.setSide(objArray.get(8).getAsString());
						oo.setOrdType(objArray.get(9).getAsString());
						oo.setOrderQty(objArray.get(10).getAsBigDecimal());
						
						BlinktradeCurrency c = BlinktradeCurrency.getCurrencyBySimbol(oo.getSymbol());
						oo.setPrice(objArray.get(11).getAsBigDecimal().divide(
								c.getRate(),
								c.getRateSize(), RoundingMode.DOWN) );
						oo.setOrderDate( Utils.getCalendar(objArray.get(12).getAsString()));
						oo.setVolume(objArray.get(13).getAsBigDecimal());
						oo.setTimeInForce(objArray.get(14).getAsString());
					}
				}
			}
		}
		
		return openOrders;
		
	}

	public List<OpenOrder> getCompletedOrders() throws BlinktradeAPIException {
		if (completedOrders == null) {
			String response = getApi().requestCompletedOrders(new Integer((int)(System.currentTimeMillis()/1000)));
			JsonParser jsonParser = new JsonParser();
			JsonObject jo = (JsonObject) jsonParser.parse(response);
			JsonArray completedOrdListGrp = jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonArray("OrdListGrp");
			completedOrders = new ArrayList<OpenOrder>();
			if(completedOrdListGrp != null) {
				for (JsonElement o: completedOrdListGrp) {
					if (o != null) {
						OpenOrder oo = new OpenOrder();
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
						oo.setSide(objArray.get(8).getAsString());
						oo.setOrdType(objArray.get(9).getAsString());
						oo.setOrderQty(objArray.get(10).getAsBigDecimal());
						
						BlinktradeCurrency c = BlinktradeCurrency.getCurrencyBySimbol(oo.getSymbol());
						oo.setPrice(objArray.get(11).getAsBigDecimal().divide(
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
	
	public OpenOrder getLastBuy() throws BlinktradeAPIException {
		for (OpenOrder order: getCompletedOrders())
			if (order.getSide().equals("1"))
				return order;
		
		return null;
	}
	
	public OpenOrder getLastSell() throws BlinktradeAPIException {
		for (OpenOrder order: getCompletedOrders())
			if (order.getSide().equals("2"))
				return order;
		
		return null;
	}
	
	public BigDecimal getLastRelevantBuyPrice() throws BlinktradeAPIException, Exception {
		if (lastRelevantBuyPrice == null) {
			
			lastRelevantBuyPrice = new BigDecimal(0);
			
			double btcWithOpenOrders = 
				getBalance().getBtcAmount().doubleValue();
			
			List<OpenOrder> groupOfOperations = new ArrayList<OpenOrder>(); 
			double sumOfBtc = 0;
			
			for (OpenOrder operation: getCompletedOrders()) {
				if (operation.getSide().equals("1")) {
					if (sumOfBtc + operation.getCumQty().doubleValue() <= btcWithOpenOrders) {
						sumOfBtc += operation.getCumQty().doubleValue();
						groupOfOperations.add(operation);
					}
					else {
						OpenOrder newOperation = new OpenOrder(operation);
						newOperation.setVolume(new BigDecimal(btcWithOpenOrders - sumOfBtc));
						groupOfOperations.add(newOperation);
						sumOfBtc += btcWithOpenOrders - sumOfBtc;
						break;
					}
				}
			}
			if (sumOfBtc != 0) {
				for (OpenOrder operation: groupOfOperations) {
					lastRelevantBuyPrice = new BigDecimal(
						lastRelevantBuyPrice.doubleValue() +	
						(operation.getCumQty().doubleValue() * 
						operation.getPrice().doubleValue() / sumOfBtc)
					); 
				}
			}
			System.out.println("Calculating last relevant buy price: ");
			System.out.println("  BTC with open orders: " + btcWithOpenOrders);
			System.out.println("  Considered BTC sum: " + sumOfBtc);
			System.out.println("  Considered buy operations: " + groupOfOperations.size());
			System.out.println("  Last relevant buy price: " + lastRelevantBuyPrice);
			System.out.println("  Considered operations: ");
			for (OpenOrder operation: groupOfOperations)
				System.out.println("    " + operation.toDisplayString()); 
			System.out.println("");
		}
		return lastRelevantBuyPrice;
	}
	
	public BigDecimal getLastRelevantSellPrice() throws BlinktradeAPIException {
		if (lastRelevantSellPrice == null) {
			
			lastRelevantSellPrice = new BigDecimal(0);
			
			double sumOfBtc = 0;
			double sumOfNumerators = 0;
			
			List<SimpleOrder> groupOfOrders = new ArrayList<SimpleOrder>();
			
			for (int i = 0; i < numOfConsideredOrdersForLastRelevantSellPrice; i++) {
				SimpleOrder order = getActiveSellOrders().get(i);				
				sumOfBtc +=  order.getBitcoins().doubleValue();
				sumOfNumerators += 
					order.getBitcoins().doubleValue() * order.getCurrencyPrice().doubleValue();
				groupOfOrders.add(order);
			}
			
			if (sumOfBtc != 0) {
				lastRelevantSellPrice = new BigDecimal(sumOfNumerators / sumOfBtc);
			}
			
			System.out.println("Calculating last relevant sell price: ");
			System.out.println("  Considered numerator sum: " + sumOfNumerators);
			System.out.println("  Considered denominator sum: " + sumOfBtc);
			System.out.println("  Considered sell orders: " + groupOfOrders.size());
			System.out.println("  Last relevant sell price: " + lastRelevantSellPrice);
			System.out.println("  Considered orders: ");
			for (SimpleOrder order: groupOfOrders)
				System.out.println("    " + order); 
			System.out.println("");
		}
		return lastRelevantSellPrice;
	}

	public List<OpenOrder> getMyActiveOrders() throws BlinktradeAPIException {
		if (myActiveOrders == null) {
			myActiveOrders = getOpenOrders();
		}
		return myActiveOrders;
	}

	public List<OpenOrder> getMyActiveBuyOrders() throws BlinktradeAPIException {
		if (myActiveBuyOrders == null) {
			myActiveBuyOrders = new ArrayList<OpenOrder>();
			for (OpenOrder order: getMyActiveOrders())
				if (order.getSide().equals("1"))
					myActiveBuyOrders.add(order);
		}
		return myActiveBuyOrders;
	}

	public List<OpenOrder> getMyActiveSellOrders() throws BlinktradeAPIException {
		if (myActiveSellOrders == null) {
			myActiveSellOrders = new ArrayList<OpenOrder>();
			for (OpenOrder order: getMyActiveOrders())
				if (order.getSide().equals("2"))
					myActiveSellOrders.add(order);
		}
		return myActiveSellOrders;
	}

}
