package br.eti.claudiney.blinktrade.example;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import br.eti.claudiney.blinktrade.api.BlinktradeAPI;
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
		
	}

}
