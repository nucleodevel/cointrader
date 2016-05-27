package net.trader.beans;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class UserConfiguration {
	
	private static long MILISSECONDS_PER_3H = 10800000;

	private String key;
	private String secret;
	private Provider provider;
	private Broker broker;
	
	private CoinCurrencyPair coinCurrencyPair;
	private Integer delayTime;
	private String buyMode;
	private String sellMode;
	private Double minimumBuyRate;
	private Double minimumSellRate;
	private Double maxBuyInterval;
	private Double maxSellInterval;
	private Double minimumCoinAmount;
	private Double incDecPrice;
	
	public UserConfiguration() {
		this.coinCurrencyPair = new CoinCurrencyPair(null, null);
		minimumBuyRate = 0.8;
		minimumSellRate = 1.2;
		maxBuyInterval = null;
		maxSellInterval = null;
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

	public Broker getBroker() {
		return broker;
	}

	public void setBroker(Broker broker) {
		this.broker = broker;
	}

	public CoinCurrencyPair getCoinCurrencyPair() {
		return coinCurrencyPair;
	}

	public void setCoinCurrencyPair(CoinCurrencyPair coinCurrencyPair) {
		this.coinCurrencyPair = coinCurrencyPair;
	}
	
	public Coin getCoin() {
		return coinCurrencyPair.getCoin();
	}

	public void setCoin(Coin coin) {
		coinCurrencyPair.setCoin(coin);
	}

	public Currency getCurrency() {
		return coinCurrencyPair.getCurrency();
	}

	public void setCurrency(Currency currency) {
		coinCurrencyPair.setCurrency(currency);
	}

	public Integer getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(Integer delayTime) {
		this.delayTime = delayTime;
	}

	public String getBuyMode() {
		return buyMode;
	}

	public void setBuyMode(String buyMode) {
		this.buyMode = buyMode;
	}

	public String getSellMode() {
		return sellMode;
	}

	public void setSellMode(String sellMode) {
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

	public Double getMaxInterval(RecordSide side) {
		Double interval = null;
		switch (side) {
			case BUY:
				interval = maxBuyInterval == null?
					null: MILISSECONDS_PER_3H * maxBuyInterval;
			break;
			case SELL:
				interval = maxSellInterval == null?
					null: MILISSECONDS_PER_3H * maxSellInterval;
			break;
		}
		return interval;
	}

	public void setMaxBuyInterval(Double maxBuyInterval) {
		this.maxBuyInterval = maxBuyInterval;
	}

	public void setMaxSellInterval(Double maxSellInterval) {
		this.maxSellInterval = maxSellInterval;
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
	
	@Override
	public String toString() {
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(5);
		DecimalFormatSymbols symbols=decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.getClass().getSimpleName() + ": ["); 
		sb.append("\n  key: " + key.substring(0, 8) + "...");
		sb.append(";\n  secret: " + secret.substring(0, 8) + "...");
		sb.append(";\n  provider: " + provider);
		sb.append(";\n  broker: " + broker);
		sb.append(";\n  coin: " + coinCurrencyPair.getCurrency());
		sb.append(";\n  currency: " + coinCurrencyPair.getCurrency());
		sb.append(";\n  delayTime: " + delayTime);
		sb.append(";\n  buyMode: " + buyMode);
		sb.append(";\n  sellMode: " + sellMode);
		sb.append(";\n  minimumBuyRate: " + decFmt.format(minimumBuyRate));
		sb.append(";\n  minimumSellRate: " + decFmt.format(minimumSellRate));
		sb.append(";\n  maxBuyInterval: " + maxBuyInterval);
		sb.append(";\n  maxSellInterval: " + maxSellInterval);
		sb.append(";\n  minimumCoinAmount: " + decFmt.format(minimumCoinAmount));
		sb.append(";\n  incDecPrice: " + decFmt.format(incDecPrice));
		sb.append("\n]");
		
		return sb.toString();
	}
	
}
