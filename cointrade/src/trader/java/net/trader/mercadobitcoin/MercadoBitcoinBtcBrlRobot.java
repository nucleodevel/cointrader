package net.trader.mercadobitcoin;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.mercadobitcoin.common.exception.MercadoBitcoinException;
import net.mercadobitcoin.tradeapi.to.Operation;
import net.mercadobitcoin.tradeapi.to.Order;
import net.mercadobitcoin.tradeapi.to.Order.CoinPair;
import net.trader.exception.NetworkErrorException;
import net.trader.exception.ParamLabelErrorException;
import net.trader.exception.ParamSyntaxErrorException;
import net.trader.exception.ParamValueErrorException;
import net.trader.robot.Robot;

public class MercadoBitcoinBtcBrlRobot {
	
	private static CoinPair myCoinPair = CoinPair.BTC_BRL;
	
	private static Robot robot;
	private static MercadoBitcoinReport report;
	
	private static BigDecimal totalBrl;
	private static BigDecimal totalBtc;
	
	private static DecimalFormat decFmt;
	
	public static void main(String[] args) {
		
		robot = new Robot(null, 10, "bs", 0.01, 0.008, 0.01, 0.00001, -0.05);
		
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
				// TODO Auto-generated catch block
				System.out.println("The file " + robot.getFileName() + " doesn't exist!");
				return;
			}
			
			try {
			
				// configurations
				
				decFmt = new DecimalFormat();
				decFmt.setMaximumFractionDigits(5);
				DecimalFormatSymbols symbols=decFmt.getDecimalFormatSymbols();
				symbols.setDecimalSeparator('.');
				symbols.setGroupingSeparator(',');
				decFmt.setDecimalFormatSymbols(symbols);
				
				
				// creating robot and reading APIs
				
				System.out.println("");
				System.out.println("\n---- Start reading: " + (new Date()));		
				report = new MercadoBitcoinReport(myCoinPair);
				
				System.out.println("");
				System.out.println(
					"Delay time: " + robot.getDelayTime() + "s  /  " +
					"Operation mode: " + robot.getOperationMode() + "  /  " +
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
				totalBrl = report.getAccountBalance().getFunds().getBrlWithOpenOrders();
				totalBtc = report.getAccountBalance().getFunds().getBtcWithOpenOrders();
				System.out.println("Total BRL: " + decFmt.format(totalBrl));
				System.out.println("Total BTC: " + decFmt.format(totalBtc));
				
				System.out.println("");
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
				if (!robot.getOperationMode().contains("b")) {
					// get the unique buy order or null
					Order myBuyOrder = report.getMyActiveBuyOrders().size() > 0?
						report.getMyActiveBuyOrders().get(0): null;
					if (myBuyOrder != null)
						report.getTradeApiService().cancelOrder(myBuyOrder);
					System.out.println("\nDon't make buy order but cancel any!");
				}
				else
					makeBuyOrders();

				if (!robot.getOperationMode().contains("s")) {
					// get the unique buy order or null
					Order mySellOrder = report.getMyActiveSellOrders().size() > 0?
						report.getMyActiveSellOrders().get(0): null;
					if (mySellOrder != null)
						report.getTradeApiService().cancelOrder(mySellOrder);
					System.out.println("\nDon't make sell order but cancel any!");
				}
				else
					makeSellOrders();
				
				System.out.println("\n---- Finish reading: " + (new Date()));
				
			} catch (MercadoBitcoinException e) {
				e.printStackTrace();
			} catch (NetworkErrorException e) {
				System.out.println("Network error: after 10 seconds, try again");
				try {
					TimeUnit.SECONDS.sleep(10);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			
			// putting delay time
			
			try {
				TimeUnit.SECONDS.sleep(robot.getDelayTime());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
				
	}
	
	private static void makeBuyOrders() throws NumberFormatException, MercadoBitcoinException, NetworkErrorException {		
		
		System.out.println("");
		System.out.println("Analising buy order");
		
		for (int i = 0; i < report.getActiveBuyOrders().size(); i++) {
			
			Order order = report.getActiveBuyOrders().get(i);
			Order nextOrder = report.getActiveBuyOrders().size() - 1 == i? 
				null: report.getActiveBuyOrders().get(i + 1);
			
			boolean isAGoodBuyOrder =  
					order.getPrice().doubleValue() / 
					report.getLastRelevantSellPrice().doubleValue() <= 
					1 - robot.getMinimumBuyRate();
			
			if (isAGoodBuyOrder) {
				
				BigDecimal brl = new BigDecimal(order.getPrice().doubleValue() + robot.getIncDecPrice());
				BigDecimal btc = new BigDecimal((totalBrl.doubleValue() - 0.01) / brl.doubleValue());
				
				// get the unique buy order or null
				Order myBuyOrder = report.getMyActiveBuyOrders().size() > 0?
					report.getMyActiveBuyOrders().get(0): null;
				
				// if my order isn't the best, delete it and create another 
				if (
					myBuyOrder == null || 
					!decFmt.format(order.getPrice()).equals(decFmt.format(myBuyOrder.getPrice()))
				) {
					if (myBuyOrder != null)
						report.getTradeApiService().cancelOrder(myBuyOrder);
					try {
						if (btc.doubleValue() > robot.getMinimumCoinAmount()) {
							report.getTradeApiService().createBuyOrder(
								myCoinPair, btc.toString(), brl.toString()
							);
							System.out.println(
								"Buy order created: " +
								order.getType() + " - " + (i + 1) + "° - R$ " + 
								decFmt.format(brl) + " - BTC " + decFmt.format(btc)
							);
						}
						else
							System.out.println(
								"There are no BRL available for " +
								order.getType() + " - " + (i + 1) + "° - R$ " + 
								decFmt.format(brl) + " - BTC " + decFmt.format(btc)
							);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				}
				else if (
					decFmt.format(order.getPrice()).equals(decFmt.format(myBuyOrder.getPrice())) &&
					decFmt.format(order.getVolume()).equals(decFmt.format(btc)) &&
					decFmt.format(order.getPrice().doubleValue() - nextOrder.getPrice().doubleValue()).
						equals(decFmt.format(robot.getIncDecPrice()))
				) {
					System.out.println(
						"Maintaining previous order " +
						order.getType() + " - " + (i + 1) + "° - R$ " + 
						decFmt.format(order.getPrice()) + " - BTC " + 
						decFmt.format(order.getVolume())
					);
					break;
				}
			}
		}
	}
		
	private static void makeSellOrders() throws NumberFormatException, MercadoBitcoinException, NetworkErrorException {	
		
		System.out.println("");
		System.out.println("Analising sell order");
		
		for (int i = 0; i < report.getActiveSellOrders().size(); i++) {
			
			Order order = report.getActiveSellOrders().get(i);
			Order nextOrder = report.getActiveSellOrders().size() - 1 == i? 
				null: report.getActiveSellOrders().get(i + 1);
			
			boolean isAGoodSellOrder = 
				report.getLastRelevantBuyPrice() != null && 
				report.getLastRelevantBuyPrice().doubleValue() > 0 ?
					(order.getPrice().doubleValue() / 
					report.getLastRelevantBuyPrice().doubleValue() >= 
					1 + robot.getMinimumSellRate()): true;
				
			boolean isToSellSoon = 
				report.getLastRelevantBuyPrice() != null && 
				report.getLastRelevantBuyPrice().doubleValue() > 0 ?
					(order.getPrice().doubleValue() / 
					report.getLastRelevantBuyPrice().doubleValue() <= 
					1 + robot.getSellRateAfterBreakdown()): true;
				
			if (isAGoodSellOrder || isToSellSoon) {
				
				BigDecimal brl = new BigDecimal(order.getPrice().doubleValue() - robot.getIncDecPrice());
				BigDecimal btc = totalBtc;
				
				// get the unique buy order or null
				Order mySellOrder = report.getMyActiveSellOrders().size() > 0?
					report.getMyActiveSellOrders().get(0): null;
					
				// if my order isn't the best, delete it and create another 
				if (
					mySellOrder == null || 
					!decFmt.format(order.getPrice()).equals(decFmt.format(mySellOrder.getPrice()))
				) {
					if (mySellOrder != null)
						report.getTradeApiService().cancelOrder(mySellOrder);
					try {
						if (btc.doubleValue() > robot.getMinimumCoinAmount()) {
							report.getTradeApiService().createSellOrder(
								myCoinPair, btc.toString(), brl.toString()
							);
							System.out.println(
								"Sell order created: " +
								order.getType() + " - " + (i + 1) + "° - R$ " + 
								decFmt.format(brl) + " - BTC " + decFmt.format(btc)
							);
						}
						else
							System.out.println(
								"There are no BTC available for " +
								order.getType() + " - " + (i + 1) + "° - R$ " + 
								decFmt.format(brl) + " - BTC " + decFmt.format(btc)
							);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				}
				else if (
					decFmt.format(order.getPrice()).equals(decFmt.format(mySellOrder.getPrice())) &&
					decFmt.format(order.getVolume()).equals(decFmt.format(btc)) &&
					decFmt.format(nextOrder.getPrice().doubleValue() - order.getPrice().doubleValue()).
						equals(decFmt.format(robot.getIncDecPrice()))
				) {
					System.out.println(
						"Maintaining previous order " +
						order.getType() + " - " + (i + 1) + "° - R$ " + 
						decFmt.format(order.getPrice()) + " - BTC " + 
						decFmt.format(order.getVolume())
					);
					break;
				}
			}
		}
	}

}