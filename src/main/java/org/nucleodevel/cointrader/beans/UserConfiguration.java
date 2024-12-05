package org.nucleodevel.cointrader.beans;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class UserConfiguration {

	private String key;
	private String secret;
	private Provider provider;

	private Currency currency;
	private List<Coin> coinList;
	private Integer delayTime;
	private RecordSideMode buyMode;
	private RecordSideMode sellMode;
	private Double minimumBuyRate;
	private Double minimumSellRate;
	private Double breakdownBuyRate;
	private Double breakdownSellRate;
	private Double minimumCoinAmount;
	private Double incDecPrice;

	public UserConfiguration() {
		currency = null;
		coinList = new ArrayList<>();

		minimumBuyRate = null;
		minimumSellRate = null;
		breakdownBuyRate = null;
		breakdownSellRate = null;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public List<Coin> getCoinList() {
		return coinList;
	}

	public void setCoinList(List<Coin> coinList) {
		this.coinList = coinList;
	}

	public List<CoinCurrencyPair> getCoinCurrencyPairList() {
		List<CoinCurrencyPair> coinCurrencyPairList = new ArrayList<>();

		coinList.stream().forEach((coin) -> coinCurrencyPairList.add(new CoinCurrencyPair(coin, getCurrency())));

		return coinCurrencyPairList;
	}

	public Integer getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(Integer delayTime) {
		this.delayTime = delayTime;
	}

	public RecordSideMode getMode(RecordSide side) {
		RecordSideMode mode = RecordSideMode.NONE;
		switch (side) {
		case BUY:
			mode = buyMode;
			break;
		case SELL:
			mode = sellMode;
			break;
		}
		return mode;
	}

	public RecordSideMode getBuyMode() {
		return buyMode;
	}

	public void setBuyMode(RecordSideMode buyMode) {
		this.buyMode = buyMode;
	}

	public RecordSideMode getSellMode() {
		return sellMode;
	}

	public void setSellMode(RecordSideMode sellMode) {
		this.sellMode = sellMode;
	}

	public Double getMinimumRate(RecordSide side) {
		Double rate = 0.0;
		switch (side) {
		case BUY:
			rate = 1 - minimumBuyRate;
			break;
		case SELL:
			rate = 1 + minimumSellRate;
			break;
		}
		return rate;
	}

	public void setMinimumBuyRate(Double minimumBuyRate) {
		this.minimumBuyRate = minimumBuyRate;
	}

	public void setMinimumSellRate(Double minimumSellRate) {
		this.minimumSellRate = minimumSellRate;
	}

	public Double getBreakdownRate(RecordSide side) {
		Double rate = 0.0;
		switch (side) {
		case BUY:
			if (breakdownBuyRate != null)
				rate = 1 - breakdownBuyRate;
			break;
		case SELL:
			if (breakdownSellRate != null)
				rate = 1 + breakdownSellRate;
			break;
		}
		return rate;
	}

	public Double getBreakdownBuyRate() {
		return breakdownBuyRate;
	}

	public void setBreakdownBuyRate(Double breakdownBuyRate) {
		this.breakdownBuyRate = breakdownBuyRate;
	}

	public Double getBreakdownSellRate() {
		return breakdownSellRate;
	}

	public void setBreakdownSellRate(Double breakdownSellRate) {
		this.breakdownSellRate = breakdownSellRate;
	}

	public Double getMinimumCoinAmount() {
		return minimumCoinAmount;
	}

	public void setMinimumCoinAmount(Double minimumCoinAmount) {
		this.minimumCoinAmount = minimumCoinAmount;
	}

	public void setIncDecPrice(Double incDecPrice) {
		this.incDecPrice = incDecPrice;
	}

	public Double getIncDecPrice(RecordSide side) {
		Double incDecPrice = 0.0;
		switch (side) {
		case BUY:
			incDecPrice = Math.abs(this.incDecPrice);
			break;
		case SELL:
			incDecPrice = Math.abs(this.incDecPrice) * (-1);
			break;
		}
		return incDecPrice;
	}

	public boolean isSingleCoin() {
		return coinList == null || coinList.size() == 1;
	}

	@Override
	public String toString() {
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(5);
		DecimalFormatSymbols symbols = decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);

		StringBuilder sb = new StringBuilder();

		sb.append(this.getClass().getSimpleName() + ": [");
		sb.append("\n  key: " + key.substring(0, 8) + "...");
		sb.append(";\n  secret: " + secret.substring(0, 8) + "...");
		sb.append(";\n  provider: " + provider);
		sb.append(";\n  coinList: " + coinList);
		sb.append(";\n  currency: " + currency);
		sb.append(";\n  delayTime: " + delayTime);
		sb.append(";\n  buyMode: " + buyMode);
		sb.append(";\n  sellMode: " + sellMode);

		if (minimumBuyRate != null)
			sb.append(";\n  minimumBuyRate: " + decFmt.format(minimumBuyRate));
		if (minimumSellRate != null)
			sb.append(";\n  minimumSellRate: " + decFmt.format(minimumSellRate));
		if (breakdownBuyRate != null)
			sb.append(";\n  breakdownBuyRate: " + decFmt.format(breakdownBuyRate));
		if (breakdownSellRate != null)
			sb.append(";\n  breakdownSellRate: " + decFmt.format(breakdownSellRate));
		if (minimumCoinAmount != null)
			sb.append(";\n  minimumCoinAmount: " + decFmt.format(minimumCoinAmount));
		if (incDecPrice != null)
			sb.append(";\n  incDecPrice: " + decFmt.format(incDecPrice));
		sb.append("\n]");

		return sb.toString();
	}

}