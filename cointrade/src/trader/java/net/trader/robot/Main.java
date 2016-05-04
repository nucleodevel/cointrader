package net.trader.robot;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.trader.beans.Order;
import net.trader.beans.RecordSide;
import net.trader.beans.UserConfiguration;
import net.trader.exception.ApiProviderException;
import net.trader.exception.ParamLabelErrorException;
import net.trader.exception.ParamSyntaxErrorException;
import net.trader.exception.ParamValueErrorException;
import net.trader.robot.ParamReader;

public class Main {
	
	private static ParamReader paramReader;
	private static UserConfiguration userConfiguration;
	
	public static void main(String[] args) {
		
		paramReader = new ParamReader();
		
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
			
			try {
			
				System.out.println("");
				System.out.println("\n---- Start reading: " + (new Date()));	
				
				System.out.println("");
				System.out.println("\n---- Params");
				
				System.out.println(paramReader.getFileContent());
				
				// creating paramReader and reading APIs
				userConfiguration = paramReader.getUserConfiguration();
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
					System.out.println(report.getLastUserBuyOrder().toString());
				if (report.getLastUserSellOrder() != null)
					System.out.println(report.getLastUserSellOrder().toString());
				
				System.out.println("");
				System.out.println("Current top orders by type");
				System.out.println(report.getCurrentTopBuy().toString());
				System.out.println(report.getCurrentTopSell().toString());
				
				
				// analise and make orders
				
				String buyMode = userConfiguration.getBuyMode();
				switch (buyMode) {
					case "none":
						Order myBuyOrder = report.getUserActiveBuyOrders().size() > 0?
							report.getUserActiveBuyOrders().get(0): null;
						if (myBuyOrder != null)
							report.cancelOrder(myBuyOrder);
						System.out.println("\nDon't make buy order but cancel any!");
						break;
					case "operation":
						report.makeOrdersByLastRelevantPriceByOperations(RecordSide.BUY);
						break;
					case "order":
						report.makeOrdersByLastRelevantPriceByOrders(RecordSide.BUY);
						break;
				}
				
				String sellMode = userConfiguration.getSellMode();
				switch (sellMode) {
					case "none":
						Order mySellOrder = report.getUserActiveSellOrders().size() > 0?
							report.getUserActiveSellOrders().get(0): null;
						if (mySellOrder != null)
							report.cancelOrder(mySellOrder);
						System.out.println("\nDon't make sell order but cancel any!");
						break;
					case "operation":
						report.makeOrdersByLastRelevantPriceByOperations(RecordSide.SELL);
						break;
					case "order":
						report.makeOrdersByLastRelevantPriceByOrders(RecordSide.SELL);
						break;
				}
				
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
