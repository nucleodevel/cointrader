package org.nucleodevel.cointrader.beans;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserConfiguration {

	private String key;
	private String secret;
	private Provider provider;

	private Currency currency;
	private List<Coin> coinList;
	private Integer delayTime;
	private BigDecimal minimumCoinAmount;
	private BigDecimal incDecPrice;

	private Map<RecordSide, UserSideConfiguration> sideConfigurationMap;

	public UserConfiguration() {
		currency = null;
		sideConfigurationMap = new HashMap<>();
		coinList = new ArrayList<>();
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

	public Integer getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(Integer delayTime) {
		this.delayTime = delayTime;
	}

	public BigDecimal getMinimumCoinAmount() {
		return minimumCoinAmount;
	}

	public void setMinimumCoinAmount(BigDecimal minimumCoinAmount) {
		this.minimumCoinAmount = minimumCoinAmount;
	}

	public BigDecimal getIncDecPrice() {
		return incDecPrice;
	}

	public void setIncDecPrice(BigDecimal incDecPrice) {
		this.incDecPrice = incDecPrice;
	}

	// --------- Calculated Getters and Setters ----------

	public List<CoinCurrencyPair> getCoinCurrencyPairList() {
		List<CoinCurrencyPair> coinCurrencyPairList = new ArrayList<>();

		coinList.stream().forEach((coin) -> coinCurrencyPairList.add(new CoinCurrencyPair(coin, getCurrency())));

		return coinCurrencyPairList;
	}

	public UserSideConfiguration getSideConfiguration(RecordSide side) {
		return sideConfigurationMap.containsKey(side) ? sideConfigurationMap.get(side) : null;
	}

	public void setSideConfiguration(RecordSide side, UserSideConfiguration sideConfiguration) {
		this.sideConfigurationMap.put(side, sideConfiguration);
	}

	public BigDecimal getEffectiveIncDecPrice(RecordSide side) {
		return this.incDecPrice.abs().multiply(BigDecimal.valueOf(-1)).multiply(side.getMultiplierFactor());
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

		if (key != null)
			sb.append("\n  key: " + key.substring(0, 8) + "...");
		if (secret != null)
			sb.append("\n  secret: " + secret.substring(0, 8) + "...");
		if (provider != null)
			sb.append("\n  provider: " + provider);
		if (coinList != null)
			sb.append("\n  coinList: " + coinList);
		if (currency != null)
			sb.append("\n  currency: " + currency);
		if (delayTime != null)
			sb.append("\n  delayTime: " + delayTime);
		if (minimumCoinAmount != null)
			sb.append("\n  minimumCoinAmount: " + decFmt.format(minimumCoinAmount));
		if (incDecPrice != null)
			sb.append("\n  incDecPrice: " + decFmt.format(incDecPrice));

		sb.append("\n  sideConfiguration: [");

		for (Map.Entry<RecordSide, UserSideConfiguration> entry : sideConfigurationMap.entrySet())
			sb.append("\n    " + entry.getValue());

		sb.append("\n  ]");
		sb.append("\n]");

		return sb.toString();
	}

}