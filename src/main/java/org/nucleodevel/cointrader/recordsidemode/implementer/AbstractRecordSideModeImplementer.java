package org.nucleodevel.cointrader.recordsidemode.implementer;

import org.nucleodevel.cointrader.beans.CoinCurrencyPair;
import org.nucleodevel.cointrader.beans.RecordSide;
import org.nucleodevel.cointrader.beans.UserConfiguration;
import org.nucleodevel.cointrader.exception.ApiProviderException;
import org.nucleodevel.cointrader.robot.ProviderReport;

public abstract class AbstractRecordSideModeImplementer {

	protected ProviderReport providerReport;

	protected UserConfiguration userConfiguration;
	protected CoinCurrencyPair coinCurrencyPair;

	public AbstractRecordSideModeImplementer(ProviderReport providerReport) throws ApiProviderException {
		super();
		this.providerReport = providerReport;
		this.userConfiguration = providerReport.getUserConfiguration();
		this.coinCurrencyPair = providerReport.getCoinCurrencyPair();
	}

	public abstract void makeOrdersByLastRelevantPrice(ProviderReport providerReport, RecordSide side,
			boolean hasToWinCurrent) throws ApiProviderException;

}
