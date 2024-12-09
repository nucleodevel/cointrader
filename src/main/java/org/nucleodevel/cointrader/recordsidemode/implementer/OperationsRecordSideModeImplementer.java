package org.nucleodevel.cointrader.recordsidemode.implementer;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.exception.ApiProviderException;
import org.nucleodevel.cointrader.recordsidemode.AbstractRecordSideModeImplementer;
import org.nucleodevel.cointrader.robot.ProviderReport;
import org.nucleodevel.cointrader.utils.Utils;

public class OperationsRecordSideModeImplementer extends AbstractRecordSideModeImplementer {

	public OperationsRecordSideModeImplementer(ProviderReport providerReport, RecordSide side)
			throws ApiProviderException {
		super(providerReport, side);
	}

	@Override
	public void tryToMakeOrders() throws ApiProviderException {

		DecimalFormat decFmt = Utils.getDefaultDecimalFormat();
		boolean hasToWinCurrent = true;

		BigDecimal lastRelevantPrice = getLastRelevantPriceByOperationsAndTheirPositions(coinCurrencyPair, side, true);

		System.out.println("  Price to win: " + decFmt.format(lastRelevantPrice));
		makeOrdersByLastRelevantPrice(coinCurrencyPair, side, lastRelevantPrice, hasToWinCurrent);
	}

}
