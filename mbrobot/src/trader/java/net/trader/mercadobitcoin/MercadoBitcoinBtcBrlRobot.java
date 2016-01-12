package net.trader.mercadobitcoin;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.mercadobitcoin.common.exception.MercadoBitcoinException;
import net.mercadobitcoin.tradeapi.to.Order;
import net.mercadobitcoin.tradeapi.to.Order.CoinPair;
import net.trader.exception.NetworkErrorException;
import net.trader.exception.ParamLabelErrorException;
import net.trader.exception.ParamSyntaxErrorException;
import net.trader.exception.ParamValueErrorException;

public class MercadoBitcoinBtcBrlRobot {
	
	private static CoinPair myCoinPair = CoinPair.BTC_BRL;
	
	private static MercadoBitcoinRobot robot;
	private static MercadoBitcoinReport report;
	
	private static BigDecimal totalBrl;
	private static BigDecimal totalBtc;
	
	private static DecimalFormat decFmt;
	
	public static void main(String[] args) {
		
		robot = new MercadoBitcoinRobot();
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
					"Minimum rate -> buy: " + decFmt.format(robot.getMinimumBuyRate() * 100) + "%; " +
					"sell: " + decFmt.format(robot.getMinimumSellRate() * 100) + "%  /  " +
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
				
				
				// analise and make orders
				
				makeBuyOrders();
				makeSellOrders();
				
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
					TimeUnit.SECONDS.sleep(10);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			
		}
				
	}
	
	private static void makeBuyOrders() throws NumberFormatException, MercadoBitcoinException, NetworkErrorException {		
		
		System.out.println("");
		System.out.println("Analising buy order");
		
		for (int i = 0; i < report.getActiveBuyOrders().size(); i++) {
			
			Order order = report.getActiveBuyOrders().get(i);
			
			boolean isAGoodBuyOrder =
				order.getPrice().doubleValue() / report.getCurrentTopSell().getPrice().doubleValue() <= 
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
					order.getPrice().doubleValue() != myBuyOrder.getPrice().doubleValue()
				) {
					
					try {
						if (btc.doubleValue() > robot.getMinimumCoinAmount()) {
							if (myBuyOrder != null && 
								decFmt.format(myBuyOrder.getPrice()).
								equals(decFmt.format(brl.doubleValue() - robot.getIncDecPrice()))
							)
								System.out.println(
									"Maintaining " +
									myBuyOrder.getType() + " - " + (i + 1) + "° - R$ " + 
									decFmt.format(brl.doubleValue() - robot.getIncDecPrice()) + 
									" - BTC " + decFmt.format(btc)
								);
							else {
								if (myBuyOrder != null)
									report.getTradeApiService().cancelOrder(myBuyOrder);
								report.getTradeApiService().createBuyOrder(
									myCoinPair, btc.toString(), brl.toString()
								);
								System.out.println(
									"Buy order created: " +
									order.getType() + " - " + (i + 1) + "° - R$ " + 
									decFmt.format(brl) + " - BTC " + decFmt.format(btc)
								);
							}
						}
						else {
							if (myBuyOrder != null)
								report.getTradeApiService().cancelOrder(myBuyOrder);
							System.out.println(
								"There are no BRL available for " +
								order.getType() + " - " + (i + 1) + "° - R$ " + 
								decFmt.format(brl) + " - BTC " + decFmt.format(btc)
							);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
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
			
			boolean isAGoodSellOrder = 
				order.getPrice().doubleValue() / report.getLastBuy().getPrice().doubleValue() >= 
				1 + robot.getMinimumSellRate();				
			if (isAGoodSellOrder) {
				
				BigDecimal brl = new BigDecimal(order.getPrice().doubleValue() - robot.getIncDecPrice());
				BigDecimal btc = totalBtc;
				
				// get the unique buy order or null
				Order mySellOrder = report.getMyActiveSellOrders().size() > 0?
					report.getMyActiveSellOrders().get(0): null;
										
				// if my order isn't the best, delete it and create another 
				if (
					mySellOrder == null || 
					order.getPrice() != mySellOrder.getPrice()
				) {
					try {
						if (btc.doubleValue() > robot.getMinimumCoinAmount()) {
							if (mySellOrder != null && 
								decFmt.format(mySellOrder.getPrice()).
								equals(decFmt.format(brl.doubleValue() + robot.getIncDecPrice()))
							)
								System.out.println(
									"Maintaining " +
									order.getType() + " - " + (i + 1) + "° - R$ " + 
									decFmt.format(brl.doubleValue() + robot.getIncDecPrice()) + 
									" - BTC " + decFmt.format(btc)
								);
							else {
								if (mySellOrder != null)
									report.getTradeApiService().cancelOrder(mySellOrder);
								report.getTradeApiService().createSellOrder(
									myCoinPair, btc.toString(), brl.toString()
								);
								System.out.println(
									"Sell order created: " +
									order.getType() + " - " + (i + 1) + "° - R$ " + 
									decFmt.format(brl) + " - BTC " + decFmt.format(btc)
								);
							}
						}
						else {
							if (mySellOrder != null)
								report.getTradeApiService().cancelOrder(mySellOrder);
							System.out.println(
								"There are no BTC available for " +
								order.getType() + " - " + (i + 1) + "° - R$ " + 
								decFmt.format(brl) + " - BTC " + decFmt.format(btc)
							);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				}
			}
		}
	}

}
