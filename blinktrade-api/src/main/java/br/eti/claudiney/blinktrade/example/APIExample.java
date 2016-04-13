package br.eti.claudiney.blinktrade.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
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
import br.eti.claudiney.blinktrade.enums.BlinktradeBroker;
import br.eti.claudiney.blinktrade.enums.BlinktradeOrderSide;
import br.eti.claudiney.blinktrade.enums.BlinktradeOrderType;
import br.eti.claudiney.blinktrade.enums.BlinktradeSymbol;
import br.eti.claudiney.blinktrade.exception.BlinktradeAPIException;
import br.eti.claudiney.blinktrade.utils.Utils;

/**
 * API examples.
 * 
 * @author Claudiney Nascimento e Silva
 * 
 * @version 1.0 2015-09-27
 * 
 * @since September/2015
 *
 */
public class APIExample {
	
	private static final String API_KEY = "vXTiWeOGgT3O3Aa6h53BcqaVP41PwqOXygr3KPZbM7Q";
	private static final String API_SECRET = "Ri095LLsAhwCLwS9xgRBDKaOq0LlQ6Zv1hoCOq2jJMQ";
	
	private static long numOfConsideredOrdersForLastRelevantSellPrice = 3;
	
	private static BlinktradeAPI api;
	
	private static Balance balance;
	private static OrderBookResponse orderBook;
	
	private static List<Bid> activeBuyOrders;
	private static List<Ask> activeSellOrders;
	
	private static SimpleOrder currentTopBuy;
	private static SimpleOrder currentTopSell;

	private static List<OpenOrder> myActiveOrders;
	private static List<OpenOrder> myActiveBuyOrders;
	private static List<OpenOrder> myActiveSellOrders;
	
	private static List<OpenOrder> openOrders;
	private static List<OpenOrder> completedOrders;
	
	private static BigDecimal lastRelevantBuyPrice;
	private static BigDecimal lastRelevantSellPrice;
	
	public static void main(String[] args) throws Exception {
		
		/*
		 *  Initialize API
		 */
		
		/*
		 *  Request Balance 
		 */
		
		/*System.out.println("========== Balance ==========");
		Integer balanceReqID = new Integer(1); // It can be any random number.
		response = api.getBalance(balanceReqID);
		
		System.out.println(response);*/
		
		/*
		 *  Request Open Orders 
		 */
		
		/*System.out.println("========== Open Orders ==========");
		Integer orderReqID = new Integer(1); // It can be any random number.
		response = api.requestOpenOrders(orderReqID);
		
		System.out.println(response);*/
		
		/*
		 *  Send New Order
		 */
		
		//BigDecimal satoshiBase = new BigDecimal("100000000"); // Keep constant
		
		// Current amount (in native currency)
		//BigDecimal amount = new BigDecimal("1.99");
		
		// Desired price
		//BigDecimal price = new BigDecimal("1658"); 
		
		// This line calculates the amount of bitcoin (in satoshis) required for buy order .
		/*BigInteger satoshis = amount
				.multiply(satoshiBase)
				.divide(price, 8, RoundingMode.DOWN)
				.toBigInteger();*/
		
		/*System.out.println("========== Send Order ==========");
		Integer clientOrderID = new Integer((int)(System.currentTimeMillis()/1000)); // Must be an unique ID. 
		response = api.sendNewOrder(
				clientOrderID,
				BlinktradeSymbol.BTCBRL,
				BlinktradeOrderSide.BUY,
				BlinktradeOrderType.LIMITED,
				price,
				satoshis);
		
		System.out.println(response);*/
		
		
		/*System.out.println("========== Orderbook ==========");
		OrderBookResponse ob = api.getOrderBook();
		
		System.out.println("========== Bids ==========");
		for (int i = 0; i < ob.getBids().size(); i++)
			System.out.println(ob.getBids().get(i));

		System.out.println("========== Asks ==========");
		for (int i = 0; i < ob.getBids().size(); i++)
			System.out.println(ob.getAsks().get(i));*/
		
		
		
		
		
		
		try {
		
			// configurations
			
			DecimalFormat decFmt = new DecimalFormat();
			decFmt.setMaximumFractionDigits(8);
			DecimalFormatSymbols symbols = decFmt.getDecimalFormatSymbols();
			symbols.setDecimalSeparator('.');
			symbols.setGroupingSeparator(',');
			decFmt.setDecimalFormatSymbols(symbols);
			
			
			// creating robot and reading APIs
			
			System.out.println("");
			System.out.println("\n---- Start reading: " + (new Date()));
			
			
			// descriptions
			
			System.out.println("");
			System.out.println("My account");
			System.out.println(getBalance());
            /*BigDecimal totalBrl = jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive("BRL").getAsBigDecimal().divide(new BigDecimal(100000000));
            BigDecimal totalBtc = jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive("BTC").getAsBigDecimal().divide(new BigDecimal(100000000));
            System.out.println("Total BRL: " + totalBrl);
			System.out.println("Total BTC: " + totalBtc);*/
			
			System.out.println("");
			System.out.println("Reading my last orders... ");
			/*for (OpenOrder order: getOpenOrders())
				System.out.println(order.toDisplayString());*/
            System.out.println("Number of open orders: " + getOpenOrders().size());
            System.out.println("Number of completed orders: " + getCompletedOrders().size());
			
			System.out.println("");
			System.out.println("My last operations by type");
			if (getLastBuy() != null)
				System.out.println(getLastBuy().toDisplayString());
			if (getLastSell() != null)
				System.out.println(getLastSell().toDisplayString());
			if (getLastRelevantBuyPrice() != null)
				System.out.println("");
			if (getLastRelevantSellPrice() != null)
				System.out.println("");
			
			System.out.println("");
			System.out.println("Current top orders by type");
			System.out.println(
				"BUY - " + decFmt.format(getCurrentTopBuy().getCurrencyPrice())
			);
			System.out.println(
				"SELL - " + decFmt.format(getCurrentTopSell().getCurrencyPrice())
			);
			
			
			// analise and make orders
			
			makeBuyOrders();
			makeSellOrders();
			
			System.out.println("\n---- Finish reading: " + (new Date()));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static BlinktradeAPI getApi() throws BlinktradeAPIException {
		if (api == null)
			api = new BlinktradeAPI(API_KEY, API_SECRET, BlinktradeBroker.FOXBIT);
		return api;
	}
	
	public static Balance getBalance() throws BlinktradeAPIException, Exception {
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
	
	public static OrderBookResponse getOrderBook() throws BlinktradeAPIException {
		if (orderBook == null)
			orderBook = getApi().getOrderBook();
		return orderBook;
	}

	public static List<Bid> getActiveBuyOrders() throws BlinktradeAPIException {
		if (activeBuyOrders == null) {
			activeBuyOrders = getOrderBook().getBids();
		}
		return activeBuyOrders;
	}

	public static List<Ask> getActiveSellOrders() throws BlinktradeAPIException {
		if (activeSellOrders == null) {
			activeSellOrders = getOrderBook().getAsks();
		}
		return activeSellOrders;
	}

	public static SimpleOrder getCurrentTopBuy() throws BlinktradeAPIException {
		if (currentTopBuy == null)
			currentTopBuy = getActiveBuyOrders().get(0);
		return currentTopBuy;
	}

	public static SimpleOrder getCurrentTopSell() throws BlinktradeAPIException {
		if (currentTopSell == null)
			currentTopSell = getActiveSellOrders().get(0);
		return currentTopSell;
	}

	public static List<OpenOrder> getOpenOrders() throws BlinktradeAPIException {
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

	public static List<OpenOrder> getCompletedOrders() throws BlinktradeAPIException {
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
	
	public static OpenOrder getLastBuy() throws BlinktradeAPIException {
		for (OpenOrder order: getCompletedOrders())
			if (order.getSide().equals("1"))
				return order;
		
		return null;
	}
	
	public static OpenOrder getLastSell() throws BlinktradeAPIException {
		for (OpenOrder order: getCompletedOrders())
			if (order.getSide().equals("2"))
				return order;
		
		return null;
	}
	
	public static BigDecimal getLastRelevantBuyPrice() throws BlinktradeAPIException, Exception {
		if (lastRelevantBuyPrice == null) {
			
			lastRelevantBuyPrice = new BigDecimal(0);
			
			double btcWithOpenOrders = 
				getBalance().getBtcAmount().doubleValue();
			
			List<OpenOrder> groupOfOperations = new ArrayList<OpenOrder>(); 
			double sumOfBtc = 0;
			
			for (OpenOrder operation: getCompletedOrders()) {
				if (operation.getSide().equals("1")) {
					if (sumOfBtc + operation.getVolume().doubleValue() <= btcWithOpenOrders) {
						sumOfBtc += operation.getVolume().doubleValue();
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
						(operation.getVolume().doubleValue() * 
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
				System.out.print("    " + operation.toDisplayString()); 
			System.out.println("");
		}
		return lastRelevantBuyPrice;
	}
	
	public static BigDecimal getLastRelevantSellPrice() throws BlinktradeAPIException {
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
				System.out.print("    " + order); 
			System.out.println("");
		}
		return lastRelevantSellPrice;
	}

	public static List<OpenOrder> getMyActiveOrders() throws BlinktradeAPIException {
		if (myActiveOrders == null) {
			myActiveOrders = getOpenOrders();
		}
		return myActiveOrders;
	}

	public static List<OpenOrder> getMyActiveBuyOrders() throws BlinktradeAPIException {
		if (myActiveBuyOrders == null) {
			myActiveBuyOrders = new ArrayList<OpenOrder>();
			for (OpenOrder order: getMyActiveOrders())
				if (order.getSide().equals("1"))
					myActiveBuyOrders.add(order);
		}
		return myActiveBuyOrders;
	}

	public static List<OpenOrder> getMyActiveSellOrders() throws BlinktradeAPIException {
		if (myActiveSellOrders == null) {
			myActiveSellOrders = new ArrayList<OpenOrder>();
			for (OpenOrder order: getMyActiveOrders())
				if (order.getSide().equals("2"))
					myActiveSellOrders.add(order);
		}
		return myActiveSellOrders;
	}
	
	private static void makeBuyOrders() throws BlinktradeAPIException, Exception {		
		
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(8);
		DecimalFormatSymbols symbols=decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
		System.out.println("");
		System.out.println("Analising buy order");
		
		for (int i = 0; i < getActiveBuyOrders().size(); i++) {
			
			SimpleOrder order = getActiveBuyOrders().get(i);
			SimpleOrder nextOrder = getActiveBuyOrders().size() - 1 == i? 
				null: getActiveBuyOrders().get(i + 1);
			
			boolean isAGoodBuyOrder =  
					order.getCurrencyPrice().doubleValue() / 
					getLastRelevantSellPrice().doubleValue() <= 
					1 - getMinimumBuyRate();
			
			if (isAGoodBuyOrder) {
				
				BigDecimal brl = new BigDecimal(order.getCurrencyPrice().doubleValue() + getIncDecPrice());
				Double btcDouble = (getBalance().getCurrencyAmount().doubleValue() - 0.01) / brl.doubleValue();
				BigDecimal btc = new BigDecimal(btcDouble);
				
				// get the unique buy order or null
				OpenOrder myBuyOrder = getMyActiveBuyOrders().size() > 0?
					getMyActiveBuyOrders().get(0): null;
				
				// if my order isn't the best, delete it and create another 
				if (
					myBuyOrder == null || 
					!decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(myBuyOrder.getPrice()))
				) {
					if (myBuyOrder != null)
						getApi().cancelOrder(myBuyOrder);
					try {
						if (btc.doubleValue() / 100000000 > getMinimumCoinAmount()) {
							getApi().sendNewOrder(
								new Integer((int)(System.currentTimeMillis()/1000)),
								BlinktradeSymbol.BTCBRL,
								BlinktradeOrderSide.BUY,
								BlinktradeOrderType.LIMITED,
								brl, btc.toBigInteger()
							);
							System.out.println(
								"Buy order created: " +
								(i + 1) + "° - R$ " + 
								decFmt.format(brl) + " - BTC " + btc.divide(new BigDecimal(100000000))
							);
						}
						else
							System.out.println(
								"There are no BRL available for " +
								(i + 1) + "° - R$ " + 
								decFmt.format(brl) + " - BTC " + btc.divide(new BigDecimal(100000000))
							);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				}
				else if (
					decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(myBuyOrder.getPrice())) &&
					decFmt.format(order.getBitcoins()).equals(btc) &&
					decFmt.format(order.getCurrencyPrice().doubleValue() - nextOrder.getCurrencyPrice().doubleValue()).
						equals(decFmt.format(getIncDecPrice()))
				) {
					System.out.println(
						"Maintaining previous order " +
						(i + 1) + "° - R$ " + 
						decFmt.format(order.getCurrencyPrice()) + " - BTC " + 
						order.getBitcoins().divide(new BigDecimal(100000000))
					);
					break;
				}
			}
		}
	}
		
	private static void makeSellOrders() throws Exception {	
		
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(8);
		DecimalFormatSymbols symbols=decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
		System.out.println("");
		System.out.println("Analising sell order");
		
		for (int i = 0; i < getActiveSellOrders().size(); i++) {
			
			SimpleOrder order = getActiveSellOrders().get(i);
			SimpleOrder nextOrder = getActiveSellOrders().size() - 1 == i? 
				null: getActiveSellOrders().get(i + 1);
			
			boolean isAGoodSellOrder = 
				getLastRelevantBuyPrice() != null && 
				getLastRelevantBuyPrice().doubleValue() > 0 ?
					(order.getCurrencyPrice().doubleValue() / 
					getLastRelevantBuyPrice().doubleValue() >= 
					1 + getMinimumSellRate()): true;
				
			boolean isToSellSoon = 
				getLastRelevantBuyPrice() != null && 
				getLastRelevantBuyPrice().doubleValue() > 0 ?
					(order.getCurrencyPrice().doubleValue() / 
					getLastRelevantBuyPrice().doubleValue() <= 
					1 + getSellRateAfterBreakdown()): true;
				
			if (isAGoodSellOrder || isToSellSoon) {
				
				BigDecimal brl = new BigDecimal(order.getCurrencyPrice().doubleValue() - getIncDecPrice());
				BigDecimal btc = new BigDecimal(getBalance().getBtcAmount().doubleValue());
				
				// get the unique buy order or null
				OpenOrder mySellOrder = getMyActiveSellOrders().size() > 0?
					getMyActiveSellOrders().get(0): null;
					
				// if my order isn't the best, delete it and create another 
				if (
					mySellOrder == null || 
					!decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(mySellOrder.getPrice()))
				) {
					if (mySellOrder != null)
						getApi().cancelOrder(mySellOrder);
					try {
						if (btc.doubleValue() > getMinimumCoinAmount()) {
							getApi().sendNewOrder(
								new Integer((int)(System.currentTimeMillis()/1000)),
								BlinktradeSymbol.BTCBRL,
								BlinktradeOrderSide.SELL,
								BlinktradeOrderType.LIMITED,
								brl, btc.toBigInteger()
							);
							System.out.println(
								"Sell order created: " +
								(i + 1) + "° - R$ " + 
								decFmt.format(brl) + " - BTC " + btc.divide(new BigDecimal(100000000))
							);
						}
						else
							System.out.println(
								"There are no BTC available for " +
								(i + 1) + "° - R$ " + 
								decFmt.format(brl) + " - BTC " + btc.divide(new BigDecimal(100000000))
							);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				}
				else if (
					decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(mySellOrder.getPrice())) &&
					order.getBitcoins().equals(btc) &&
					decFmt.format(nextOrder.getCurrencyPrice().doubleValue() - order.getCurrencyPrice().doubleValue()).
						equals(decFmt.format(getIncDecPrice()))
				) {
					System.out.println(
						"Maintaining previous order " +
						(i + 1) + "° - R$ " + 
						decFmt.format(order.getCurrencyPrice()) + " - BTC " + 
						order.getBitcoins().divide(new BigDecimal(100000000))
					);
					break;
				}
			}
		}
	}
	
	public static Double getMinimumBuyRate() {
		return 0.01;
	}

	public static Double getMinimumSellRate() {
		return 0.008;
	}

	public static Double getIncDecPrice() {
		return 0.01;
	}

	public static Double getMinimumCoinAmount() {
		return 0.01;
	}

	public static double getSellRateAfterBreakdown() {
		return -0.05;
	}

}
