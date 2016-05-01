package net.trader.robot;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.trader.beans.Order;
import net.trader.exception.ApiProviderException;
import net.trader.exception.ParamLabelErrorException;
import net.trader.exception.ParamSyntaxErrorException;
import net.trader.exception.ParamValueErrorException;
import net.trader.robot.Robot;

public class Main {
	
	private static Robot robot;
	private static UserConfiguration userConfiguration;
	
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
				userConfiguration = robot.getUserConfiguration();
				ProviderReport report = new ProviderReport(userConfiguration);
				
				// descriptions
				
				System.out.println("");
				System.out.println("My account");
				System.out.println(report.getBalance());
				
				System.out.println("");
				System.out.println("Reading my last orders... ");
	            System.out.println("Number of open orders: " + report.getUserActiveOrders().size());
	            System.out.println("Number of completed orders: " + report.getUserCompletedOrders().size());
				
				System.out.println("");
				System.out.println("My last operations by type");
				if (report.getLastUserBuyOrder() != null)
					System.out.println(report.getLastUserBuyOrder().toDisplayString());
				if (report.getLastUserSellOrder() != null)
					System.out.println(report.getLastUserSellOrder().toDisplayString());
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
				if (!userConfiguration.getOperationMode().contains("b")) {
					// get the unique buy order or null
					Order myBuyOrder = report.getUserActiveBuyOrders().size() > 0?
						report.getUserActiveBuyOrders().get(0): null;
					if (myBuyOrder != null)
						report.cancelOrder(myBuyOrder);
					System.out.println("\nDon't make buy order but cancel any!");
				}
				else
					report.makeBuyOrders();
	
				if (!userConfiguration.getOperationMode().contains("s")) {
					// get the unique buy order or null
					Order mySellOrder = report.getUserActiveSellOrders().size() > 0?
						report.getUserActiveSellOrders().get(0): null;
					if (mySellOrder != null)
						report.cancelOrder(mySellOrder);
					System.out.println("\nDon't make sell order but cancel any!");
				}
				else
					report.makeSellOrders();
				
				System.out.println("\n---- Finish reading: " + (new Date()));
			} catch (ApiProviderException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
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
