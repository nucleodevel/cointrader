package br.net.dallan.cointrader.robot;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import br.net.dallan.cointrader.beans.RecordSide;
import br.net.dallan.cointrader.beans.RecordSideMode;
import br.net.dallan.cointrader.beans.UserConfiguration;
import br.net.dallan.cointrader.exception.ApiProviderException;
import br.net.dallan.cointrader.exception.ParamLabelErrorException;
import br.net.dallan.cointrader.exception.ParamSyntaxErrorException;
import br.net.dallan.cointrader.exception.ParamValueErrorException;
import br.net.dallan.cointrader.robot.ParamReader;

public class Main {
	
	public static void main(String[] args) {
		
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(8);
		
		DecimalFormatSymbols symbols = decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
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
				ProviderReport report = new ProviderReport(userConfiguration);
				//report.readApiAtFirst();
				
				System.out.println("");
				System.out.println("\n---- Params");
				
				System.out.println(userConfiguration);
				
				// descriptions
				
				if (report.getTicker() != null) {
					System.out.println("");
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
				
				System.out.println("");
				System.out.println("Current top orders by type");
				System.out.println("  " + report.getActiveOrders(RecordSide.BUY).get(0));
				System.out.println("  " + report.getActiveOrders(RecordSide.SELL).get(0));
				
				
				// analise and make orders
				
				RecordSideMode buyMode = userConfiguration.getBuyMode();
				report.makeOrdersByLastRelevantPrice(RecordSide.BUY, buyMode);
				
				RecordSideMode sellMode = userConfiguration.getSellMode();
				report.makeOrdersByLastRelevantPrice(RecordSide.SELL, sellMode);
				
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

}
