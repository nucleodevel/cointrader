package org.nucleodevel.cointrader.recordsidemode.implementer;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.nucleodevel.cointrader.beans.CoinCurrencyPair;
import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.exception.ApiProviderException;
import org.nucleodevel.cointrader.robot.ProviderReport;
import org.nucleodevel.cointrader.utils.Utils;

public class OtherOrdersRecordSideModeImplementer extends AbstractRecordSideModeImplementer {

	public OtherOrdersRecordSideModeImplementer(ProviderReport providerReport) throws ApiProviderException {
		super(providerReport);
	}

	@Override
	public void makeOrdersByLastRelevantPrice(ProviderReport providerReport, RecordSide side, boolean hasToWinCurrent)
			throws ApiProviderException {

		DecimalFormat decFmt = Utils.getDefaultDecimalFormat();

		CoinCurrencyPair bestCoinCurrencyPairBySpread = null;
		BigDecimal bestSpread = new BigDecimal(0.0);

		String bestSpreadMessage = "\n  Analising spreads";

		for (CoinCurrencyPair ccp : providerReport.getCoinCurrencyPairList()) {
			BigDecimal ccpSpread = providerReport.getSpread(ccp);

			if (ccpSpread.compareTo(bestSpread) > 0) {
				bestCoinCurrencyPairBySpread = ccp;
				bestSpread = ccpSpread;
			}

			bestSpreadMessage += "\n    Spread of " + ccp + ": " + ccpSpread;
		}

		bestSpreadMessage += "\n   The best spread is " + bestCoinCurrencyPairBySpread + ": " + bestSpread + "\n";

		System.out.println(bestSpreadMessage);

		System.out.println("");
		System.out.println("  ---- " + side + ": " + coinCurrencyPair);

		Double effectiveRate = userConfiguration.getSideConfiguration(side).getEffeciveRegularRate().doubleValue();
		BigDecimal lastRelevantPrice = providerReport
				.getLastRelevantPriceByOrders(coinCurrencyPair, side.getOther(), true)
				.multiply(new BigDecimal(effectiveRate));

		System.out.println("  Price to win: " + decFmt.format(lastRelevantPrice));

		providerReport.cancelAllOrdersOfOtherCoinCurrencyPairsButSameSide(coinCurrencyPair, side);
		providerReport.makeOrdersByLastRelevantPrice(coinCurrencyPair, side, lastRelevantPrice, hasToWinCurrent);
	}

}
