package br.eti.claudiney.blinktrade.example;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import net.trader.blinktrade.BlinktradeUserInformation;
import br.eti.claudiney.blinktrade.api.BlinktradeAPI;
import br.eti.claudiney.blinktrade.api.beans.Ask;
import br.eti.claudiney.blinktrade.api.beans.Bid;
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
	
	public static void main(String[] args) throws Exception {
		
		String response = null;
		
		BlinktradeUserInformation userInfo = new BlinktradeUserInformation();
		
		/*
		 *  Initialize API
		 */
		
		BlinktradeAPI api = new BlinktradeAPI(
			userInfo.getMyTapiKey(), userInfo.getMyTapiCode(), userInfo.getBroker()
		); 

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
		
		/*
		 *  Orderbook
		 */
		
		System.out.println("========== Balance ==========");
		Integer orderbookReqID = new Integer((int)(System.currentTimeMillis()/1000)); // It can be any random number.
		OrderBookResponse obResponse = api.getOrderBook();
		
		System.out.println("========== Bids ==========");
		for (Bid bid: obResponse.getBids())
			System.out.println(bid.getCurrencyPrice() + " - " + bid.getBitcoins());
		
		System.out.println("========== Ask ==========");
		for (Ask ask: obResponse.getAsks())
			System.out.println(ask.getCurrencyPrice() + " - " + ask.getBitcoins());
		
	}

}
