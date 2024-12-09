package org.nucleodevel.cointrader.recordsidemode.implementer;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.exception.ApiProviderException;
import org.nucleodevel.cointrader.robot.ProviderReport;
import org.nucleodevel.cointrader.utils.Utils;

public class OperationsRecordSideModeImplementer extends AbstractRecordSideModeImplementer {

	public OperationsRecordSideModeImplementer(ProviderReport providerReport) throws ApiProviderException {
		super(providerReport);
	}

	@Override
	public void makeOrdersByLastRelevantPrice(ProviderReport providerReport, RecordSide side, boolean hasToWinCurrent)
			throws ApiProviderException {

		DecimalFormat decFmt = Utils.getDefaultDecimalFormat();

		BigDecimal lastRelevantPrice = providerReport
				.getLastRelevantPriceByOperationsAndTheirPositions(coinCurrencyPair, side, true);

		System.out.println("  Price to win: " + decFmt.format(lastRelevantPrice));
		providerReport.makeOrdersByLastRelevantPrice(coinCurrencyPair, side, lastRelevantPrice, hasToWinCurrent);
	}

}
