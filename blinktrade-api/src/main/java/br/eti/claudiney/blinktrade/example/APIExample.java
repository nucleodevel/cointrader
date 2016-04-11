package br.eti.claudiney.blinktrade.example;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import br.eti.claudiney.blinktrade.api.BlinktradeAPI;
import br.eti.claudiney.blinktrade.api.beans.OrderBookResponse;
import br.eti.claudiney.blinktrade.enums.BlinktradeBroker;
import br.eti.claudiney.blinktrade.enums.BlinktradeOrderSide;
import br.eti.claudiney.blinktrade.enums.BlinktradeOrderType;
import br.eti.claudiney.blinktrade.enums.BlinktradeSymbol;

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
	
	public static void main(String[] args) throws Exception {
		
		String response = null;
		
		/*
		 *  Initialize API
		 */
		
		BlinktradeAPI api = new BlinktradeAPI(API_KEY, API_SECRET, BlinktradeBroker.FOXBIT); 

		/*
		 *  Request Balance 
		 */
		
		System.out.println("========== Balance ==========");
		Integer balanceReqID = new Integer(1); // It can be any random number.
		response = api.getBalance(balanceReqID);
		
		System.out.println(response);
		
		/*
		 *  Request Open Orders 
		 */
		
		System.out.println("========== Open Orders ==========");
		Integer orderReqID = new Integer(1); // It can be any random number.
		response = api.requestOpenOrders(orderReqID);
		
		System.out.println(response);
		
		/*
		 *  Send New Order
		 */
		
		BigDecimal satoshiBase = new BigDecimal("100000000"); // Keep constant
		
		// Current amount (in native currency)
		BigDecimal amount = new BigDecimal("1.99");
		
		// Desired price
		BigDecimal price = new BigDecimal("1658"); 
		
		// This line calculates the amount of bitcoin (in satoshis) required for buy order .
		BigInteger satoshis = amount
				.multiply(satoshiBase)
				.divide(price, 8, RoundingMode.DOWN)
				.toBigInteger();
		
		System.out.println("========== Send Order ==========");
		Integer clientOrderID = new Integer((int)(System.currentTimeMillis()/1000)); // Must be an unique ID. 
		/*response = api.sendNewOrder(
				clientOrderID,
				BlinktradeSymbol.BTCBRL,
				BlinktradeOrderSide.BUY,
				BlinktradeOrderType.LIMITED,
				price,
				satoshis);*/
		
		System.out.println(response);
		
		
		System.out.println("========== Orderbook ==========");
		OrderBookResponse ob = api.getOrderBook();
		
		System.out.println("========== Bids ==========");
		for (int i = 0; i < ob.getBids().size(); i++)
			System.out.println(ob.getBids().get(i));

		System.out.println("========== Asks ==========");
		for (int i = 0; i < ob.getBids().size(); i++)
			System.out.println(ob.getAsks().get(i));
		
		
		
		
		
		
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
			balanceReqID = new Integer((int)(System.currentTimeMillis()/1000)); // It can be any random number.
			response = api.getBalance(balanceReqID);
			JsonParser jsonParser = new JsonParser();
            JsonObject jo = (JsonObject)jsonParser.parse(response);
            BigDecimal totalBrl = jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive("BRL").getAsBigDecimal().divide(new BigDecimal(100000000));
            BigDecimal totalBtc = jo.getAsJsonArray("Responses").get(0).getAsJsonObject().getAsJsonObject("4").getAsJsonPrimitive("BTC").getAsBigDecimal().divide(new BigDecimal(100000000));
            System.out.println("Total BRL: " + totalBrl);
			System.out.println("Total BTC: " + totalBtc);
			
			/*System.out.println("");
			System.out.println("Reading my last orders... ");
			System.out.println("Number of new orders: " + report.getMyOrders().size());
			
			System.out.println("");
			System.out.println("My last operations by type");
			if (report.getLastBuy() != null) {
				System.out.println(
					report.getLastBuy().getType() + " - Price " + 
					decFmt.format(report.getLastBuy().getPrice()) + 
					" - BTC " + decFmt.format(report.getLastBuy().getAmount()) + 
					" - R$ " + 
					decFmt.format(report.getLastBuy().getPrice().doubleValue() * 
					report.getLastBuy().getAmount().doubleValue()) +
					" - Rate " + report.getLastBuy().getRate() + "%" +
					" - " + report.getLastBuy().getCreatedDate().getTime()
				);
			}
			if (report.getLastSell() != null) {
				System.out.println(
					report.getLastSell().getType() + " - Price " + 
					decFmt.format(report.getLastSell().getPrice()) + 
					" - BTC " + decFmt.format(report.getLastSell().getAmount()) + 
					" - R$ " + 
					decFmt.format(report.getLastSell().getPrice().doubleValue() * 
					report.getLastSell().getAmount().doubleValue()) +
					" - Rate " + report.getLastSell().getRate() + "%" +
					" - " + report.getLastSell().getCreatedDate().getTime()
				);
			}
			if (report.getLastRelevantBuyPrice() != null)
				System.out.println("");
			if (report.getLastRelevantSellPrice() != null)
				System.out.println("");
			
			System.out.println("");
			System.out.println("Current top orders by type");
			System.out.println(
				report.getCurrentTopBuy().getType() + " - " + 
				decFmt.format(report.getCurrentTopBuy().getPrice())
			);
			System.out.println(
				report.getCurrentTopSell().getType() + " - " + 
				decFmt.format(report.getCurrentTopSell().getPrice())
			);
			
			for (Operation operation: report.getMyOperations()) {
				System.out.print(operation);
			}
			
			
			// analise and make orders
			
			makeBuyOrders();
			makeSellOrders();
			
			System.out.println("\n---- Finish reading: " + (new Date()));*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		
		
	}

}
