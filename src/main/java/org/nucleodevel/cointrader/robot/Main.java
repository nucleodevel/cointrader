package org.nucleodevel.cointrader.robot;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.nucleodevel.cointrader.beans.CoinCurrencyPair;
import org.nucleodevel.cointrader.beans.Order;
import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.beans.Ticker;
import org.nucleodevel.cointrader.beans.UserConfiguration;
import org.nucleodevel.cointrader.exception.ApiProviderException;
import org.nucleodevel.cointrader.exception.ParamLabelErrorException;
import org.nucleodevel.cointrader.exception.ParamSyntaxErrorException;
import org.nucleodevel.cointrader.exception.ParamValueErrorException;

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

				System.out.println("");
				System.out.println("\n---- Params");

				System.out.println(userConfiguration);

				List<CoinCurrencyPair> coinCurrencyPairList = report.getCoinCurrencyPairList();

				// descriptions
				for (CoinCurrencyPair ccp : coinCurrencyPairList) {
					System.out.println("\n---- Pair " + ccp);

					Ticker ticker = report.getTicker(ccp);
					System.out.println("");
					System.out.println(ticker);

					System.out.println("  My 24 hour coin volume: " + decFmt.format(report.getMy24hCoinVolume(ccp)));

					System.out.println("  Global 24 hour volume: " + decFmt.format(ticker.getVol()));

					System.out.println("  My participation: " + decFmt
							.format(report.getMy24hCoinVolume(ccp).doubleValue() / ticker.getVol().doubleValue()));

					System.out.println("");
					System.out.println(report.getBalance(ccp));

					System.out.println("");
					System.out.println("  Reading my last orders... ");
					System.out.println("  Number of active orders: " + report.getUserActiveOrders(ccp).size());
					System.out.println("  Number of operations: " + report.getUserOperations(ccp).size());

					System.out.println("");
					System.out.println("My last operations by type");

					if (report.getUserOperations(ccp).size() == 0) {
						System.out.println("  There were no operations yet!");
					} else {
						if (report.getLastUserOperation(ccp, RecordSide.BUY) != null)
							System.out.println("  " + report.getLastUserOperation(ccp, RecordSide.BUY));
						if (report.getLastUserOperation(ccp, RecordSide.SELL) != null)
							System.out.println("  " + report.getLastUserOperation(ccp, RecordSide.SELL));
					}

					System.out.println("");
					System.out.println("My active orders by type");

					if (report.getUserActiveOrders(ccp).size() == 0) {
						System.out.println("  There are no orders yet!");
					} else {
						if (report.getUserActiveOrders(ccp, RecordSide.BUY) != null) {
							for (Order o : report.getUserActiveOrders(ccp, RecordSide.BUY)) {
								System.out.println("  " + o);
							}
						}
						if (report.getUserActiveOrders(ccp, RecordSide.SELL) != null) {
							for (Order o : report.getUserActiveOrders(ccp, RecordSide.SELL)) {
								System.out.println("  " + o);
							}
						}
					}

					System.out.println("");
					System.out.println("Current spread: " + report.getSpread(ccp));
					System.out.println("");
					System.out.println("Current top orders by type");
					System.out.println("  " + report.getOrderBookBySide(ccp, RecordSide.BUY).get(0));
					System.out.println("  " + report.getOrderBookBySide(ccp, RecordSide.SELL).get(0));
				}

				System.out.println("");
				System.out.println("---- Analise and make orders");

				for (RecordSide side : RecordSide.values())
					report.makeOrdersByLastRelevantPrice(userConfiguration.getSideConfiguration(side));

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