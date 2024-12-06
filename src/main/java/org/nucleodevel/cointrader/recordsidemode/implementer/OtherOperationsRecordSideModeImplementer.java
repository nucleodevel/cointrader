package org.nucleodevel.cointrader.recordsidemode.implementer;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.nucleodevel.cointrader.beans.CoinCurrencyPair;
import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.exception.ApiProviderException;
import org.nucleodevel.cointrader.robot.ProviderReport;
import org.nucleodevel.cointrader.utils.Utils;

public class OtherOperationsRecordSideModeImplementer extends AbstractRecordSideModeImplementer {

	public OtherOperationsRecordSideModeImplementer(ProviderReport providerReport) throws ApiProviderException {
		super(providerReport);
	}

	@Override
	public void makeOrdersByLastRelevantPrice(ProviderReport providerReport, RecordSide side, boolean hasToWinCurrent)
			throws ApiProviderException {

		DecimalFormat decFmt = Utils.getDefaultDecimalFormat();

		for (CoinCurrencyPair ccp : providerReport.getCoinCurrencyPairList()) {

			System.out.println("");
			System.out.println("  ---- " + side + ": " + ccp);

			Boolean isBreakdown = false;

			BigDecimal lastRelevantPriceByOrders = providerReport.getLastRelevantPriceByOrders(ccp, side, false);
			BigDecimal lastRelevantPriceByOperations = providerReport.getLastRelevantPriceByOperations(ccp,
					side.getOther(), false);

			Double effectiveBreakdownRate = userConfiguration.getSideConfiguration(side).getEffeciveBreakdownRate();
			if (effectiveBreakdownRate != null) {
				BigDecimal breakdownPrice = lastRelevantPriceByOperations
						.multiply(new BigDecimal(effectiveBreakdownRate));

				int compareBreakdownToOrders = breakdownPrice.compareTo(lastRelevantPriceByOrders);
				isBreakdown = (side == RecordSide.BUY ? compareBreakdownToOrders < 0
						: (side == RecordSide.SELL ? compareBreakdownToOrders > 0 : false));

				System.out.println("  Breakdown if breakdown price " + decFmt.format(breakdownPrice) + " is "
						+ (side == RecordSide.BUY ? "less" : (side == RecordSide.SELL ? "greater" : "")) + " than "
						+ side + " average price " + decFmt.format(lastRelevantPriceByOrders));

				if (isBreakdown)
					System.out.println("  Breakdown was activated");
			}

			BigDecimal lastRelevantPrice = BigDecimal.ZERO;
			if (isBreakdown) {
				lastRelevantPrice = providerReport.getLastRelevantPriceByOrders(ccp, side, true);
				hasToWinCurrent = false;
			} else {
				Double effectiveRate = userConfiguration.getSideConfiguration(side).getEffeciveRegularRate();
				lastRelevantPrice = providerReport.getLastRelevantPriceByOperations(ccp, side.getOther(), true)
						.multiply(new BigDecimal(effectiveRate));
			}

			System.out.println("  Price to win: " + decFmt.format(lastRelevantPrice));
			providerReport.makeOrdersByLastRelevantPrice(ccp, side, lastRelevantPrice, hasToWinCurrent);
		}
	}

}
