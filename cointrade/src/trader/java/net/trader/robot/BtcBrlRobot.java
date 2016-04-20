package net.trader.robot;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import br.eti.claudiney.blinktrade.api.beans.OpenOrder;
import br.eti.claudiney.blinktrade.api.beans.SimpleOrder;
import br.eti.claudiney.blinktrade.enums.BlinktradeOrderSide;
import br.eti.claudiney.blinktrade.enums.BlinktradeOrderType;
import br.eti.claudiney.blinktrade.exception.BlinktradeAPIException;
import net.mercadobitcoin.common.exception.MercadoBitcoinException;
import net.mercadobitcoin.tradeapi.to.Operation;
import net.mercadobitcoin.tradeapi.to.Order;
import net.trader.blinktrade.BlinktradeReport;
import net.trader.exception.NetworkErrorException;
import net.trader.exception.ParamLabelErrorException;
import net.trader.exception.ParamSyntaxErrorException;
import net.trader.exception.ParamValueErrorException;
import net.trader.mercadobitcoin.MercadoBitcoinReport;
import net.trader.robot.Robot;

public class BtcBrlRobot {
	
	private static Robot robot;
	
	public static void main(String[] args) {
		
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
			
			if (robot.getUserConfiguration().getProvider().equals("Blinktrade"))
				runBlinktrade();
			else if (robot.getUserConfiguration().getProvider().equals("MercadoBitcoin"))
				runMercadoBitcoin();
		}
				
	}
		
	private static void runBlinktrade() {
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
			BlinktradeReport report = new BlinktradeReport(robot.getUserConfiguration(), "BRL", "BTC");
			
			System.out.println("");
			System.out.println(
				"Delay time: " + robot.getDelayTime() + "s  /  " +
				"Operation mode: " + robot.getOperationMode() + "  /  " +
				"Minimum rate -> buy: " + decFmt.format(robot.getMinimumBuyRate() * 100) + "%; " +
				"sell: " + decFmt.format(robot.getMinimumSellRate() * 100) + "%  /  "
			);

			System.out.println(
				"Inc/Dec: " + decFmt.format(robot.getIncDecPrice()) + "  /  " +
				"Minimum coin amount: " + decFmt.format(robot.getMinimumCoinAmount())
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
			if (!robot.getOperationMode().contains("b")) {
				// get the unique buy order or null
				OpenOrder myBuyOrder = report.getMyActiveBuyOrders().size() > 0?
					report.getMyActiveBuyOrders().get(0): null;
				if (myBuyOrder != null)
					report.getApi().cancelOrder(myBuyOrder);
				System.out.println("\nDon't make buy order but cancel any!");
			}
			else
				makeBlinktradeBuyOrders(report);

			if (!robot.getOperationMode().contains("s")) {
				// get the unique buy order or null
				OpenOrder mySellOrder = report.getMyActiveSellOrders().size() > 0?
					report.getMyActiveSellOrders().get(0): null;
				if (mySellOrder != null)
					report.getApi().cancelOrder(mySellOrder);
				System.out.println("\nDon't make sell order but cancel any!");
			}
			else
				makeBlinktradeSellOrders(report);
			
			System.out.println("\n---- Finish reading: " + (new Date()));
			
		} catch (NetworkErrorException e) {
			System.out.println("Network error: after 10 seconds, try again");
			try {
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		} catch (BlinktradeAPIException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// putting delay time
		
		try {
			TimeUnit.SECONDS.sleep(robot.getDelayTime());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void runMercadoBitcoin() {
		
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
			MercadoBitcoinReport report = new MercadoBitcoinReport(robot.getUserConfiguration(), "BRL", "BTC");
			
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
			BigDecimal totalBrl = report.getAccountBalance().getFunds().getBrlWithOpenOrders();
			BigDecimal totalBtc = report.getAccountBalance().getFunds().getBtcWithOpenOrders();
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
				makeMercadoBitcoinBuyOrders(report);

			if (!robot.getOperationMode().contains("s")) {
				// get the unique buy order or null
				Order mySellOrder = report.getMyActiveSellOrders().size() > 0?
					report.getMyActiveSellOrders().get(0): null;
				if (mySellOrder != null)
					report.getTradeApiService().cancelOrder(mySellOrder);
				System.out.println("\nDon't make sell order but cancel any!");
			}
			else
				makeMercadoBitcoinSellOrders(report);
			
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
	
	private static void makeBlinktradeBuyOrders(BlinktradeReport report) 
		throws BlinktradeAPIException, Exception {		
		
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
				
				if (myBuyOrder != null) {
					System.out.println(decFmt.format(order.getCurrencyPrice()) + "-" + (decFmt.format(myBuyOrder.getPrice())));
					System.out.println(Math.abs(order.getBitcoins().doubleValue() - (btc.doubleValue() / 100000000)));
					System.out.println(order.getCurrencyPrice().doubleValue() - nextOrder.getCurrencyPrice().doubleValue());
				}
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
								report.getCoinPair(),
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
					Math.abs(order.getBitcoins().doubleValue() - (btc.doubleValue() / 100000000)) <= robot.getMinimumCoinAmount() &&
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
		
	private static void makeBlinktradeSellOrders(BlinktradeReport report) 
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
					
				if (mySellOrder != null) {
					System.out.println(decFmt.format(order.getCurrencyPrice()) + "-" + (decFmt.format(mySellOrder.getPrice())));
					System.out.println(Math.abs(order.getBitcoins().doubleValue() - (btc.doubleValue() / 100000000)));
					System.out.println(nextOrder.getCurrencyPrice().doubleValue() - order.getCurrencyPrice().doubleValue());
				}
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
								report.getCoinPair(),
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
					Math.abs(order.getBitcoins().doubleValue() - (btc.doubleValue() / 100000000)) <= robot.getMinimumCoinAmount() &&
					nextOrder.getCurrencyPrice().doubleValue() - order.getCurrencyPrice().doubleValue() <= robot.getIncDecPrice()
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
	
	private static void makeMercadoBitcoinBuyOrders(MercadoBitcoinReport report) 
		throws NumberFormatException, MercadoBitcoinException, NetworkErrorException {		
		
		System.out.println("");
		System.out.println("Analising buy order");
		
		// configurations
		
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(5);
		DecimalFormatSymbols symbols=decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
		BigDecimal totalBrl = report.getAccountBalance().getFunds().getBrlWithOpenOrders();
		
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
								report.getCoinPair(), btc.toString(), brl.toString()
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
		
	private static void makeMercadoBitcoinSellOrders(MercadoBitcoinReport report) 
		throws NumberFormatException, MercadoBitcoinException, NetworkErrorException {	
		
		System.out.println("");
		System.out.println("Analising sell order");
		
		// configurations
		
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(5);
		DecimalFormatSymbols symbols=decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
		BigDecimal totalBtc = report.getAccountBalance().getFunds().getBtcWithOpenOrders();
		
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
								report.getCoinPair(), btc.toString(), brl.toString()
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
