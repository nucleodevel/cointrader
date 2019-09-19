package org.nucleodevel.cointrader.robot;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.nucleodevel.cointrader.beans.Coin;
import org.nucleodevel.cointrader.beans.CoinCurrencyPair;
import org.nucleodevel.cointrader.beans.Order;
import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.beans.RecordSideMode;
import org.nucleodevel.cointrader.beans.UserConfiguration;
import org.nucleodevel.cointrader.exception.ApiProviderException;
import org.nucleodevel.cointrader.exception.ParamLabelErrorException;
import org.nucleodevel.cointrader.exception.ParamSyntaxErrorException;
import org.nucleodevel.cointrader.exception.ParamValueErrorException;
import org.nucleodevel.cointrader.robot.ParamReader;

public class Main {
	
	private static Map<Coin, ProviderReport> reportMap = new HashMap<Coin, ProviderReport>();
	
	private static DecimalFormat decFmt;
	
	public static void main(String[] args) {
		
		makeDecimalFormat();
		
		ParamReader paramReader = new ParamReader();
		
		try {
			paramReader.readParams(args);
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
				paramReader.readParamsFromFile();
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
				System.out.println("The file " + paramReader.getFileName() + " doesn't exist!");
				return;
			}
			
			UserConfiguration userConfiguration = null;
			
			try {
			
				System.out.println("");
				System.out.println("\n---- Start reading: " + (new Date()));	
				
				// creating paramReader and reading APIs
				userConfiguration = paramReader.getUserConfiguration();
				
				for (Coin coin: userConfiguration.getCoinList())
					reportMap.put(coin, new ProviderReport(
						new CoinCurrencyPair(coin, userConfiguration.getCurrency()), userConfiguration
					));
				
				System.out.println("\n---- Params");
				
				System.out.println(userConfiguration);
				
				for (ProviderReport report: reportMap.values()) {
					
					System.out.println("\n---- Coin currency pair: " + report.getCoinCurrencyPair() + "\n");
					
					// descriptions
					
					if (report.getTicker() != null) {
						System.out.println(report.getTicker());
						
						System.out.println("  My 24 hour coin volume: " + decFmt.format(report.getMy24hCoinVolume()));
						System.out.println("  Global 24 hour volume: " + decFmt.format(report.getTicker().getVol()));
						System.out.println(
							"  My participation: " + 
							decFmt.format(report.getMy24hCoinVolume().doubleValue() / report.getTicker().getVol().doubleValue())
						);
						System.out.println("  Last 3 hour volume: " + decFmt.format(report.getTicker().getLast3HourVolume()));
					}
					
					System.out.println("");
					System.out.println(report.getBalance());
					
					System.out.println("");
					System.out.println("  Reading my last orders... ");
		            System.out.println("  Number of active orders: " + report.getUserActiveOrders().size());
		            System.out.println("  Number of operations: " + report.getUserOperations().size());
					
		            System.out.println("");
					System.out.println("My last operations by type");
					if (report.getLastUserOperation(RecordSide.BUY) != null)
						System.out.println("  " + report.getLastUserOperation(RecordSide.BUY));
					if (report.getLastUserOperation(RecordSide.SELL) != null)
						System.out.println("  " + report.getLastUserOperation(RecordSide.SELL));
					if (report.getTicker() != null) {
						System.out.println(
							"  Last 3 hour volume: " + report.getTicker().getLast3HourVolume() 
							+ " " + report.getCoin()
						);
						if (userConfiguration.getMaxInterval(RecordSide.BUY) != null) 
							System.out.println(
								"  Max accepted inactivity time for buying: " 
								+ (double) (userConfiguration.getMaxInterval(RecordSide.BUY) / 
								  (report.getTicker().getLast3HourVolume().doubleValue()) / (60 * 1000))
								+ " minutes" 
							);
						if (userConfiguration.getMaxInterval(RecordSide.SELL) != null) 
							System.out.println(
								"  Max accepted inactivity time for selling: " 
								+ (double) (userConfiguration.getMaxInterval(RecordSide.SELL) / 
								  (report.getTicker().getLast3HourVolume().doubleValue()) / (60 * 1000))
								+ " minutes" 
							);
					}
					
					System.out.println("Current top orders by type");
					System.out.println("  " + report.getActiveOrders(RecordSide.BUY).get(0));
					System.out.println("  " + report.getActiveOrders(RecordSide.SELL).get(0));
					System.out.println("\nCurrent spread: " + report.getLastSpread());
					System.out.println("");
				}
				
				//report.readApiAtFirst();
				
				// analise and make orders
				
				RecordSideMode buyMode = userConfiguration.getBuyMode();
				makeOrdersByLastRelevantPrice(RecordSide.BUY, buyMode);
				
				RecordSideMode sellMode = userConfiguration.getSellMode();
				for (ProviderReport report: reportMap.values()) {
					report.makeOrdersByLastRelevantPrice(RecordSide.SELL, sellMode);
				}
				
				System.out.println("\n---- Finish reading: " + (new Date()));
			} catch (ApiProviderException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// putting delay time
			
			try {
				TimeUnit.SECONDS.sleep(userConfiguration.getDelayTime());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
				
	}
	
	private static void makeDecimalFormat() {
		if (decFmt == null) {
			decFmt = new DecimalFormat();
			decFmt.setMaximumFractionDigits(8);
			
			DecimalFormatSymbols symbols = decFmt.getDecimalFormatSymbols();
			symbols.setDecimalSeparator('.');
			symbols.setGroupingSeparator(',');
			decFmt.setDecimalFormatSymbols(symbols);
		}
	}
	
	public static void makeOrdersByLastRelevantPrice(RecordSide side, RecordSideMode mode) 
		throws ApiProviderException {
		System.out.println("");
		System.out.println("Analising " + side + " order for all coinCorrencyPair");
		System.out.println("");
		
		/*BigDecimal lastRelevantInactivityTime =
			getLastRelevantInactivityTimeByOperations(side.getOther());
		
		Double maxAcceptedInactivityTime = getMaxAcceptedInactivityTime(side);
		boolean isLongTimeWithoutOperation = 
			lastRelevantInactivityTime == null || maxAcceptedInactivityTime == null?
				false:
				lastRelevantInactivityTime.longValue() > maxAcceptedInactivityTime;
		
		if (lastRelevantInactivityTime != null && maxAcceptedInactivityTime != null) {
			System.out.println("  Last 3 hour volume: " + getTicker().getLast3HourVolume() + " " + getCoin());
			System.out.println(
				"  Inactivity time: " 
				+ decFmt.format(lastRelevantInactivityTime.doubleValue() / (60 * 1000))
				+ " minutes" 
			);
			System.out.println(
				"  Max accepted inactivity time: " 
				+ decFmt.format(maxAcceptedInactivityTime / (60 * 1000))
				+ " minutes" 
			);
		}*/
		
		BigDecimal lastRelevantInactivityTime = new BigDecimal(0.0);
		
		Boolean hasToWinCurrent = true;
		Coin lastCoin = null;
		BigDecimal lastSpreadMap = new BigDecimal(0.0);
		BigDecimal lastRelevantPrice = new BigDecimal(0.0);
		
		switch (mode) {
			case ORDERS: {
				for (ProviderReport report: reportMap.values()) {
					BigDecimal spread = report.getLastSpread();
					if (spread.compareTo(lastSpreadMap) > 0) {
						lastCoin = report.getCoin();
						lastSpreadMap = spread;
					}
				}
				ProviderReport report = reportMap.get(lastCoin);
				lastRelevantPrice = report.getLastRelevantPriceByOrders(side, true);
				break;
			}
			case OTHER_ORDERS: {	
				for (ProviderReport report: reportMap.values()) {
					BigDecimal spread = report.getLastSpread();
					if (spread.compareTo(lastSpreadMap) > 0) {
						lastCoin = report.getCoin();
						lastSpreadMap = spread;
					}
				}
				ProviderReport report = reportMap.get(lastCoin);
				lastRelevantPrice = report.getLastRelevantPriceByOrders(side.getOther(), true).multiply(
					new BigDecimal(report.getUserConfiguration().getMinimumRate(side))
				);
				break;
			}
			default:
				break;
		}
		
		if (lastCoin != null && mode != RecordSideMode.NONE) {
			
			for (ProviderReport report: reportMap.values())
				if (!report.getCoin().equals(lastCoin)) {
					Order myOrder = report.getUserActiveOrders(side).size() > 0?
						report.getUserActiveOrders(side).get(0): null;
					if (myOrder != null)
						report.cancelOrder(myOrder);
				}
			
			
			System.out.println("  Price to win: " + decFmt.format(lastRelevantPrice));
			
			ProviderReport report = reportMap.get(lastCoin);
			report.makeOrdersByLastRelevantPrice(side, lastRelevantPrice, lastRelevantInactivityTime, hasToWinCurrent);
		}
		else
			for (ProviderReport report: reportMap.values())
				report.makeOrdersByLastRelevantPrice(side, mode);
	}

}
