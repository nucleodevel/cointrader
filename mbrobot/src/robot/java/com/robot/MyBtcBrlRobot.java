package com.robot;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.mercadobitcoin.common.exception.MercadoBitcoinException;
import net.mercadobitcoin.common.exception.NetworkErrorException;
import net.mercadobitcoin.tradeapi.to.Order;
import net.mercadobitcoin.tradeapi.to.Order.CoinPair;

public class MyBtcBrlRobot {
	
	private static CoinPair myCoinPair = CoinPair.BTC_BRL;
	
	private static Robot robot;
	private static Report report;
	
	private static BigDecimal totalBrl;
	private static BigDecimal totalBtc;
	
	private static DecimalFormat decFmt;
	
	public static void main(String[] args) {
		
		robot = new Robot();
		robot.readParams();
		
		for (;;) {
			
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
				report = new Report(myCoinPair);
				
				System.out.println("");
				System.out.println(
					"Minimum rate -> buy: " + (robot.getMinimumBuyRate() * 100) + "%; " +
					"sell: " + (robot.getMinimumSellRate() * 100) + "%  /  " +
					"Inc/Dec: " + decFmt.format(robot.getIncDecPrice())
				);
				
				
				// descriptions
				
				System.out.println("");
				System.out.println("My account");
				totalBrl = report.getAccountBalance().getFunds().getBrlWithOpenOrders();
				totalBtc = report.getAccountBalance().getFunds().getBtcWithOpenOrders();
				System.out.println("Total BRL: " + decFmt.format(totalBrl));
				System.out.println("Total BTC: " + decFmt.format(totalBtc));
				
				
				System.out.println("");
				System.out.println("My last operations by type");
				System.out.print(
					report.getLastBuy().getType() + " - Price " + 
					decFmt.format(report.getLastBuy().getPrice()) + 
					" - BTC " + decFmt.format(report.getLastBuy().getAmount()) + 
					" - R$ " + 
					decFmt.format(report.getLastBuy().getPrice().doubleValue() * 
					report.getLastBuy().getAmount().doubleValue()) +
					" - Rate " + report.getLastBuy().getRate() + "%"
				);
				System.out.println(
					"  /  " + report.getLastSell().getType() + " - Price " + 
					decFmt.format(report.getLastSell().getPrice()) + 
					" - BTC " + decFmt.format(report.getLastSell().getAmount()) + 
					" - R$ " + 
					decFmt.format(report.getLastSell().getPrice().doubleValue() * 
					report.getLastSell().getAmount().doubleValue()) +
					" - Rate " + report.getLastSell().getRate() + "%"
				);
				
				
				System.out.println("");
				System.out.println("Current top orders by type");
				System.out.print(
					report.getCurrentTopBuy().getType() + " - " + 
					decFmt.format(report.getCurrentTopBuy().getPrice())
				);
				System.out.println(
					"  /  " + report.getCurrentTopSell().getType() + " - " + 
					decFmt.format(report.getCurrentTopSell().getPrice()) 
				);
				
				
				// analise and make orders
				
				makeOrders();
				
				System.out.println("\n---- Finish reading: " + (new Date()));
				
				
				// putting delay time
				
				try {
					TimeUnit.SECONDS.sleep(robot.getDelayTime());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				
			} catch (MercadoBitcoinException e) {
				e.printStackTrace();
			} catch (NetworkErrorException e) {
				System.out.println("Network error: after 10 seconds, try again");
				try {
					TimeUnit.SECONDS.sleep(30);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			
		}
				
	}
	
	private static void makeOrders() throws NumberFormatException, MercadoBitcoinException, NetworkErrorException {
		
		
		
		System.out.println("");
		System.out.println("Analising buy order");
		int i = 0;
		for (Order o: report.getActiveBuyOrders()) {
			if (
				o.getPrice().doubleValue() / report.getCurrentTopSell().getPrice().doubleValue() <= 
				1 - robot.getMinimumBuyRate()
			) {
				
				BigDecimal brl = new BigDecimal(o.getPrice().doubleValue() + robot.getIncDecPrice());
				BigDecimal btc = new BigDecimal((totalBrl.doubleValue() - 0.01) / brl.doubleValue());
				
				// get the unique buy order or null
				Order myBuyOrder = report.getMyActiveBuyOrders().size() > 0?
					report.getMyActiveBuyOrders().get(0): null;
				
				// if my order isn't the best, delete it and create another 
				if (
					myBuyOrder == null || 
					o.getPrice().doubleValue() != myBuyOrder.getPrice().doubleValue()
				) {
					
					try {
						if (myBuyOrder != null)
							report.getTradeApiService().cancelOrder(myBuyOrder);
						if (btc.doubleValue() > robot.getMinimumCoinAmount()) {
							if (myBuyOrder != null && 
								decFmt.format(myBuyOrder.getPrice()).
								equals(decFmt.format(brl))
							)
								System.out.println(
									"Maintaining " +
									o.getType() + " - " + (i) + "° - R$ " + 
									decFmt.format(brl) + " - BTC " + decFmt.format(btc)
								);
							else
								System.out.println(
									"Buy order created: " +
									o.getType() + " - " + (i + 1) + "° - R$ " + 
									decFmt.format(brl) + " - BTC " + decFmt.format(btc)
								);
							report.getTradeApiService().createBuyOrder(
								myCoinPair, btc.toString(), brl.toString()
							);
						}
						else
							System.out.println(
								"There are no BRL available for " +
								o.getType() + " - " + (i + 1) + "° - R$ " + 
								decFmt.format(brl) + " - BTC " + decFmt.format(btc)
							);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				}
			}
			i++;
		}
		
		
		
		System.out.println("");
		System.out.println("Analising sell order");
		i = 0;
		for (Order o: report.getActiveSellOrders()) {
			if (
				o.getPrice().doubleValue() / report.getLastBuy().getPrice().doubleValue() >= 
				1 + robot.getMinimumSellRate()
			) {
				
				BigDecimal brl = new BigDecimal(o.getPrice().doubleValue() - robot.getIncDecPrice());
				BigDecimal btc = totalBtc;
				
				// get the unique buy order or null
				Order mySellOrder = report.getMyActiveSellOrders().size() > 0?
					report.getMyActiveSellOrders().get(0): null;
										
				// if my order isn't the best, delete it and create another 
				if (
					mySellOrder == null || 
					o.getPrice() != mySellOrder.getPrice()
				) {
					try {
						if (mySellOrder != null)
							report.getTradeApiService().cancelOrder(mySellOrder);
						if (btc.doubleValue() > robot.getMinimumCoinAmount()) {
							if (mySellOrder != null && 
								decFmt.format(mySellOrder.getPrice()).
								equals(decFmt.format(brl))
							)
								System.out.println(
									"Maintaining " +
									o.getType() + " - " + (i) + "° - R$ " + 
									decFmt.format(brl) + " - BTC " + decFmt.format(btc)
								);
							else
								System.out.println(
									"Sell order created: " +
									o.getType() + " - " + (i + 1) + "° - R$ " + 
									decFmt.format(brl) + " - BTC " + decFmt.format(btc)
								);
							report.getTradeApiService().createSellOrder(
								myCoinPair, btc.toString(), brl.toString()
							);
						}
						else
							System.out.println(
								"There are no BTC available for " +
								o.getType() + " - " + (i + 1) + "° - R$ " + 
								decFmt.format(brl) + " - BTC " + decFmt.format(btc)
							);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				}
			}
			i++;
		}
	}

}
