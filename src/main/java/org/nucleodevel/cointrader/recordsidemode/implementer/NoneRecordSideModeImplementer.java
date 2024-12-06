package org.nucleodevel.cointrader.recordsidemode.implementer;

import org.nucleodevel.cointrader.beans.CoinCurrencyPair;
import org.nucleodevel.cointrader.beans.Order;
import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.exception.ApiProviderException;
import org.nucleodevel.cointrader.robot.ProviderReport;

public class NoneRecordSideModeImplementer extends AbstractRecordSideModeImplementer {

	public NoneRecordSideModeImplementer(ProviderReport providerReport) throws ApiProviderException {
		super(providerReport);
	}

	@Override
	public void makeOrdersByLastRelevantPrice(ProviderReport providerReport, RecordSide side, boolean hasToWinCurrent)
			throws ApiProviderException {

		for (CoinCurrencyPair ccp : providerReport.getCoinCurrencyPairList()) {
			Order myOrder = providerReport.getUserActiveOrders(ccp, side).size() > 0
					? providerReport.getUserActiveOrders(ccp, side).get(0)
					: null;
			if (myOrder != null)
				providerReport.cancelOrder(myOrder);
			System.out.println("\n  Don't make buy order but cancel any!");
		}
	}

}
