package net.trader.blinktrade;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import br.eti.claudiney.blinktrade.api.beans.OpenOrder;
import br.eti.claudiney.blinktrade.api.beans.SimpleOrder;
import br.eti.claudiney.blinktrade.enums.BlinktradeOrderSide;
import br.eti.claudiney.blinktrade.enums.BlinktradeOrderType;
import br.eti.claudiney.blinktrade.enums.BlinktradeSymbol;
import br.eti.claudiney.blinktrade.exception.BlinktradeAPIException;
import net.trader.exception.NetworkErrorException;
import net.trader.exception.ParamLabelErrorException;
import net.trader.exception.ParamSyntaxErrorException;
import net.trader.exception.ParamValueErrorException;

public class BlinktradeBtcBrlRobot {
	
	private static BlinktradeSymbol myCoinPair = BlinktradeSymbol.BTCBRL;
	
	private static BlinktradeRobot robot;
	private static BlinktradeReport report;
	
	public static void main(String[] args) {
		
		robot = new BlinktradeRobot();
		try {
			robot.readParams(args);
		} catch (ParamLabelErrorException e) {
			System.out.println("There is no parameter " + e.getParamLabel() + "!");
			return;
		} catch (ParamSyntaxErrorException e) {
			System.out.println("There is no value for the parameter " + e.getParamLabel() + "!");
			return;
		} catch (ParamValueErrorException e) {
			System.out.println("The parameter " + e.getParamLabel() + " can't accept this value!");
			return;
		}
		
		for (;;) {
			
			try {
			
				// configurations
				
				DecimalFormat decFmt = new DecimalFormat();
				decFmt.setMaximumFractionDigits(5);
				DecimalFormatSymbols symbols=decFmt.getDecimalFormatSymbols();
				symbols.setDecimalSeparator('.');
				symbols.setGroupingSeparator(',');
				decFmt.setDecimalFormatSymbols(symbols);
				
				
				// creating robot and reading APIs
				
				System.out.println("");
				System.out.println("\n---- Start reading: " + (new Date()));		
				report = new BlinktradeReport(myCoinPair);
				
				System.out.println("");
				System.out.println(
					"Delay time: " + robot.getDelayTime() + "s  /  " +
					"Minimum rate -> buy: " + decFmt.format(robot.getMinimumBuyRate() * 100) + "%; " +
					"sell: " + decFmt.format(robot.getMinimumSellRate() * 100) + "%  /  "
				);

				System.out.println(
					"Inc/Dec: " + decFmt.format(robot.getIncDecPrice()) + "  /  " +
					"Sell rate after breakdown: " + 
					decFmt.format(robot.getSellRateAfterBreakdown() * 100) + "%"
				);
				
				
				// descriptions
				
				System.out.println("");
				System.out.println("My account");
				System.out.println(report.getBalance());
				
				System.out.println("");
				System.out.println("Reading my last orders... ");
	            System.out.println("Number of open orders: " + report.getOpenOrders().size());
	            System.out.println("Number of completed orders: " + report.getCompletedOrders().size());
				
				System.out.println("");
				System.out.println("My last operations by type");
				if (report.getLastBuy() != null)
					System.out.println(report.getLastBuy().toDisplayString());
				if (report.getLastSell() != null)
					System.out.println(report.getLastSell().toDisplayString());
				if (report.getLastRelevantBuyPrice() != null)
					System.out.println("");
				if (report.getLastRelevantSellPrice() != null)
					System.out.println("");
				
				System.out.println("");
				System.out.println("Current top orders by type");
				System.out.println(
					"BUY - " + decFmt.format(report.getCurrentTopBuy().getCurrencyPrice())
				);
				System.out.println(
					"SELL - " + decFmt.format(report.getCurrentTopSell().getCurrencyPrice())
				);
				
				
				// analise and make orders
				
				makeBuyOrders();
				makeSellOrders();
				
				System.out.println("\n---- Finish reading: " + (new Date()));
				
			} catch (NetworkErrorException e) {
				System.out.println("Network error: after 10 seconds, try again");
				try {
					TimeUnit.SECONDS.sleep(10);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			} catch (BlinktradeAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// putting delay time
			
			try {
				TimeUnit.SECONDS.sleep(robot.getDelayTime());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
				
	}
	
	private static void makeBuyOrders() throws BlinktradeAPIException, Exception {		
		
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(8);
		
		DecimalFormatSymbols symbols = decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
		System.out.println("");
		System.out.println("Analising buy order");
		
		for (int i = 0; i < report.getActiveBuyOrders().size(); i++) {
			
			SimpleOrder order = report.getActiveBuyOrders().get(i);
			SimpleOrder nextOrder = report.getActiveBuyOrders().size() - 1 == i? 
				null: report.getActiveBuyOrders().get(i + 1);
			
			boolean isAGoodBuyOrder =  
					order.getCurrencyPrice().doubleValue() / 
					report.getLastRelevantSellPrice().doubleValue() <= 
					1 - robot.getMinimumBuyRate();
			
			if (isAGoodBuyOrder) {
				
				BigDecimal brl = new BigDecimal(order.getCurrencyPrice().doubleValue() + robot.getIncDecPrice());
				Double btcDouble = (report.getBalance().getCurrencyAmount().doubleValue() - 0.01) / brl.doubleValue();
				BigDecimal btc = new BigDecimal(btcDouble);
				
				// get the unique buy order or null
				OpenOrder myBuyOrder = report.getMyActiveBuyOrders().size() > 0?
					report.getMyActiveBuyOrders().get(0): null;
				
				System.out.println(decFmt.format(order.getCurrencyPrice()) + "-" + (decFmt.format(myBuyOrder.getPrice())));
				System.out.println(order.getBitcoins().doubleValue() - (btc.doubleValue() / 100000000));
				System.out.println(order.getCurrencyPrice().doubleValue() - nextOrder.getCurrencyPrice().doubleValue());
				// if my order isn't the best, delete it and create another 
				if (
					myBuyOrder == null || 
					!decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(myBuyOrder.getPrice()))
				) {
					if (myBuyOrder != null)
						report.getApi().cancelOrder(myBuyOrder);
					try {
						if (btc.doubleValue() / 100000000 > robot.getMinimumCoinAmount()) {
							report.getApi().sendNewOrder(
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
					order.getBitcoins().doubleValue() - (btc.doubleValue() / 100000000) <= robot.getMinimumCoinAmount() &&
					order.getCurrencyPrice().doubleValue() - nextOrder.getCurrencyPrice().doubleValue() <= robot.getIncDecPrice()
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
		
		for (int i = 0; i < report.getActiveSellOrders().size(); i++) {
			
			SimpleOrder order = report.getActiveSellOrders().get(i);
			SimpleOrder nextOrder = report.getActiveSellOrders().size() - 1 == i? 
				null: report.getActiveSellOrders().get(i + 1);
			
			boolean isAGoodSellOrder = 
				report.getLastRelevantBuyPrice() != null && 
				report.getLastRelevantBuyPrice().doubleValue() > 0 ?
					(order.getCurrencyPrice().doubleValue() / 
					report.getLastRelevantBuyPrice().doubleValue() >= 
					1 + robot.getMinimumSellRate()): true;
				
			boolean isToSellSoon = 
				report.getLastRelevantBuyPrice() != null && 
				report.getLastRelevantBuyPrice().doubleValue() > 0 ?
					(order.getCurrencyPrice().doubleValue() / 
					report.getLastRelevantBuyPrice().doubleValue() <= 
					1 + robot.getSellRateAfterBreakdown()): true;
				
			if (isAGoodSellOrder || isToSellSoon) {
				
				BigDecimal brl = new BigDecimal(order.getCurrencyPrice().doubleValue() - robot.getIncDecPrice());
				BigDecimal btc = new BigDecimal(report.getBalance().getBtcAmount().doubleValue());
				
				// get the unique buy order or null
				OpenOrder mySellOrder = report.getMyActiveSellOrders().size() > 0?
					report.getMyActiveSellOrders().get(0): null;
					
				// if my order isn't the best, delete it and create another 
				if (
					mySellOrder == null || 
					!decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(mySellOrder.getPrice()))
				) {
					if (mySellOrder != null)
						report.getApi().cancelOrder(mySellOrder);
					try {
						if (btc.doubleValue() / 100000000 > robot.getMinimumCoinAmount()) {
							report.getApi().sendNewOrder(
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
					order.getBitcoins().toString().equals(decFmt.format(btc.doubleValue() / 100000000)) &&
					order.getCurrencyPrice().doubleValue() - nextOrder.getCurrencyPrice().doubleValue() <= robot.getIncDecPrice()
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

}
