package org.nucleodevel.cointrader.recordsidemode.implementer;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.nucleodevel.cointrader.beans.CoinCurrencyPair;
import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.exception.ApiProviderException;
import org.nucleodevel.cointrader.recordsidemode.AbstractRecordSideModeImplementer;
import org.nucleodevel.cointrader.robot.ProviderReport;
import org.nucleodevel.cointrader.utils.Utils;

public class OtherOperationsRecordSideModeImplementer extends AbstractRecordSideModeImplementer {

	public OtherOperationsRecordSideModeImplementer(ProviderReport providerReport, RecordSide side)
			throws ApiProviderException {
		super(providerReport, side);
	}

	@Override
	public void tryToMakeOrders() throws ApiProviderException {

		DecimalFormat decFmt = Utils.getDefaultDecimalFormat();
		boolean hasToWinCurrent = true;

		for (CoinCurrencyPair ccp : providerReport.getCoinCurrencyPairList()) {

			System.out.println("");
			System.out.println("  ---- " + side + ": " + ccp);

			Boolean isBreakdown = false;

			BigDecimal lastRelevantPriceByOrders = getLastRelevantPriceByOrdersAndTheirPositions(ccp, side, false);
			BigDecimal lastRelevantPriceByOperations = getLastRelevantPriceByOperationsAndTheirAmounts(ccp,
					side.getOther(), false);

			BigDecimal effectiveBreakdownRate = userConfiguration.getSideConfiguration(side).getEffeciveBreakdownRate();
			if (effectiveBreakdownRate != null) {
				BigDecimal breakdownPrice = lastRelevantPriceByOperations.multiply(effectiveBreakdownRate);

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
				lastRelevantPrice = getLastRelevantPriceByOrdersAndTheirPositions(ccp, side, true);
				hasToWinCurrent = false;
			} else {
				BigDecimal effectiveRate = userConfiguration.getSideConfiguration(side).getEffeciveRegularRate();
				lastRelevantPrice = getLastRelevantPriceByOperationsAndTheirAmounts(ccp, side.getOther(), true)
						.multiply(effectiveRate);
			}

			System.out.println("  Price to win: " + decFmt.format(lastRelevantPrice));
			makeOrdersByLastRelevantPrice(ccp, side, lastRelevantPrice, hasToWinCurrent);
		}
	}

}
