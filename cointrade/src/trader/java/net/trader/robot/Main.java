package net.trader.robot;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.trader.beans.Order;
import net.trader.blinktrade.BlinktradeReport;
import net.trader.exception.ApiProviderException;
import net.trader.exception.ParamLabelErrorException;
import net.trader.exception.ParamSyntaxErrorException;
import net.trader.exception.ParamValueErrorException;
import net.trader.mercadobitcoin.MercadoBitcoinReport;
import net.trader.robot.Robot;

public class Main {
	
	private static Robot robot;
	
	public static void main(String[] args) {
		
		// configurations
		
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(5);
		DecimalFormatSymbols symbols=decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
		robot = new Robot();
		
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
				robot.readParamsFromFile();
			} catch (ParamLabelErrorException e) {
				System.out.println("There is no parameter " + e.getParamLabel() + "!");
				return;
			} catch (ParamSyntaxErrorException e) {
				System.out.println("There is no value for the parameter " + e.getParamLabel() + "!");
				return;
			} catch (ParamValueErrorException e) {
				System.out.println("The parameter " + e.getParamLabel() + " can't accept this value!");
				return;
			} catch (IOException e) {
				System.out.println("The file " + robot.getFileName() + " doesn't exist!");
				return;
			}
			
			try {
			
				System.out.println("");
				System.out.println("\n---- Start reading: " + (new Date()));	
				
				System.out.println("");
				System.out.println("\n---- Params");
				
				System.out.println(robot.getFileContent());
				
				// creating robot and reading APIs
				
				RobotReport report = robot.getUserConfiguration().getProvider().equals("Blinktrade")?
					new BlinktradeReport(
						robot.getUserConfiguration(), robot.getCoin(), robot.getCurrency() 
					): robot.getUserConfiguration().getProvider().equals("MercadoBitcoin")?
						new MercadoBitcoinReport(
							robot.getUserConfiguration(), robot.getCoin(), robot.getCurrency() 
						): null;
				
				// descriptions
				
				System.out.println("");
				System.out.println("My account");
				System.out.println(report.getBalance());
				
				System.out.println("");
				System.out.println("Reading my last orders... ");
	            System.out.println("Number of open orders: " + report.getMyActiveOrders().size());
	            System.out.println("Number of completed orders: " + report.getMyCompletedOrders().size());
				
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
				if (!robot.getOperationMode().contains("b")) {
					// get the unique buy order or null
					Order myBuyOrder = report.getMyActiveBuyOrders().size() > 0?
						report.getMyActiveBuyOrders().get(0): null;
					if (myBuyOrder != null)
						report.cancelOrder(myBuyOrder);
					System.out.println("\nDon't make buy order but cancel any!");
				}
				else
					makeBuyOrders(report);
	
				if (!robot.getOperationMode().contains("s")) {
					// get the unique buy order or null
					Order mySellOrder = report.getMyActiveSellOrders().size() > 0?
						report.getMyActiveSellOrders().get(0): null;
					if (mySellOrder != null)
						report.cancelOrder(mySellOrder);
					System.out.println("\nDon't make sell order but cancel any!");
				}
				else
					makeSellOrders(report);
				
				System.out.println("\n---- Finish reading: " + (new Date()));
			} catch (ApiProviderException e) {
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
	
	private static void makeBuyOrders(RobotReport report) 
		throws ApiProviderException, Exception {		
		
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(8);
		
		DecimalFormatSymbols symbols = decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
		System.out.println("");
		System.out.println("Analising buy order");
		
		for (int i = 0; i < report.getActiveBuyOrders().size(); i++) {
			
			Order order = report.getActiveBuyOrders().get(i);
			Order nextOrder = report.getActiveBuyOrders().size() - 1 == i? 
				null: report.getActiveBuyOrders().get(i + 1);
			
			boolean isAGoodBuyOrder =  
					order.getCurrencyPrice().doubleValue() / 
					report.getLastRelevantSellPrice().doubleValue() <= 
					1 - robot.getMinimumBuyRate();
			
			if (isAGoodBuyOrder) {
				
				BigDecimal currencyPrice = new BigDecimal(order.getCurrencyPrice().doubleValue() + robot.getIncDecPrice());
				Double coinDouble = (report.getBalance().getCurrencyAmount().doubleValue() - 0.01) / currencyPrice.doubleValue();
				BigDecimal coinAmount = new BigDecimal(coinDouble);
				
				// get the unique buy order or null
				Order myBuyOrder = report.getMyActiveBuyOrders().size() > 0?
					report.getMyActiveBuyOrders().get(0): null;
				
				if (myBuyOrder != null) {
					System.out.println(decFmt.format(order.getCurrencyPrice()) + "-" + (decFmt.format(myBuyOrder.getCurrencyPrice())));
					System.out.println(order.getCoinAmount().doubleValue() + " - " + coinAmount.doubleValue());
					System.out.println(order.getCurrencyPrice().doubleValue() - nextOrder.getCurrencyPrice().doubleValue());
				}
				// if my order isn't the best, delete it and create another 
				if (
					myBuyOrder == null || 
					!decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(myBuyOrder.getCurrencyPrice()))
				) {
					if (myBuyOrder != null)
						report.cancelOrder(myBuyOrder);
					try {
						if (coinAmount.doubleValue() > robot.getMinimumCoinAmount()) {
							report.createBuyOrder(coinAmount, currencyPrice);
							System.out.println(
								"Buy order created: " +
								(i + 1) + "° - " + report.getCurrency() + " " + 
								decFmt.format(currencyPrice) + " - " + report.getCoin() + " " + coinAmount
							);
						}
						else
							System.out.println(
								"There are no currency available for " +
								(i + 1) + "° - " + report.getCurrency() + " " + 
								decFmt.format(currencyPrice) + " - " + report.getCoin() + " " + coinAmount
							);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				}
				else if (
					decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(myBuyOrder.getCurrencyPrice())) &&
					Math.abs(order.getCoinAmount().doubleValue() - coinAmount.doubleValue()) <= robot.getMinimumCoinAmount() &&
					order.getCurrencyPrice().doubleValue() - nextOrder.getCurrencyPrice().doubleValue() <= robot.getIncDecPrice()
				) {
					System.out.println(
						"Maintaining previous order " +
						(i + 1) + "° - " + report.getCurrency() + " " + 
						decFmt.format(order.getCurrencyPrice()) + " - " + report.getCoin() + " " + 
						order.getCoinAmount()
					);
					break;
				}
			}
		}
	}
		
	private static void makeSellOrders(RobotReport report) 
		throws Exception {	
		
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(8);
		DecimalFormatSymbols symbols=decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
		System.out.println("");
		System.out.println("Analising sell order");
		
		for (int i = 0; i < report.getActiveSellOrders().size(); i++) {
			
			Order order = report.getActiveSellOrders().get(i);
			Order nextOrder = report.getActiveSellOrders().size() - 1 == i? 
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
				
				BigDecimal currencyPrice = new BigDecimal(order.getCurrencyPrice().doubleValue() - robot.getIncDecPrice());
				BigDecimal coinAmount = report.getBalance().getCoinAmount();
				
				// get the unique buy order or null
				Order mySellOrder = report.getMyActiveSellOrders().size() > 0?
					report.getMyActiveSellOrders().get(0): null;
					
				if (mySellOrder != null) {
					System.out.println(decFmt.format(order.getCurrencyPrice()) + "-" + (decFmt.format(mySellOrder.getCurrencyPrice())));
					System.out.println(Math.abs(order.getCoinAmount().doubleValue() - coinAmount.doubleValue()));
					System.out.println(nextOrder.getCurrencyPrice().doubleValue() - order.getCurrencyPrice().doubleValue());
				}
				// if my order isn't the best, delete it and create another 
				if (
					mySellOrder == null || 
					!decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(mySellOrder.getCurrencyPrice()))
				) {
					if (mySellOrder != null)
						report.cancelOrder(mySellOrder);
					try {
						if (coinAmount.doubleValue() > robot.getMinimumCoinAmount()) {
							report.createSellOrder(coinAmount, currencyPrice);
							System.out.println(
								"Sell order created: " +
								(i + 1) + "° - " + report.getCurrency() + " " + 
								decFmt.format(currencyPrice) + " - " + report.getCoin() + " " + coinAmount
							);
						}
						else
							System.out.println(
								"There are no " + report.getCoin() + " available for " +
								(i + 1) + "° - " + report.getCurrency() + " " + 
								decFmt.format(currencyPrice) + " - " + report.getCoin() + " " + coinAmount
							);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				}
				else if (
					decFmt.format(order.getCurrencyPrice()).equals(decFmt.format(mySellOrder.getCurrencyPrice())) &&
					Math.abs(order.getCoinAmount().doubleValue() - coinAmount.doubleValue()) <= robot.getMinimumCoinAmount() &&
					nextOrder.getCurrencyPrice().doubleValue() - order.getCurrencyPrice().doubleValue() <= robot.getIncDecPrice()
				) {
					System.out.println(
						"Maintaining previous order " +
						(i + 1) + "° - " + report.getCurrency() + " " + 
						decFmt.format(order.getCurrencyPrice()) + " - " + report.getCoin() + " " + 
						order.getCoinAmount()
					);
					break;
				}
			}
		}
	}

}
