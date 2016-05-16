package net.trader.beans;

public class UserConfiguration {

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
	private Long maxBuyInterval;
	private Long maxSellInterval;
	private Double minimumCoinAmount;
	private Double incDecPrice;
	
	public UserConfiguration() {
		this.coinCurrencyPair = new CoinCurrencyPair(null, null);
		minimumBuyRate = -1.0;
		minimumSellRate = 1.0;
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
				rate = minimumBuyRate;
			break;
			case SELL:
				rate = minimumSellRate;
			break;
		}
		return rate;
	}

	public Double getMinimumBuyRate() {
		return minimumBuyRate;
	}

	public void setMinimumBuyRate(Double minimumBuyRate) {
		this.minimumBuyRate = minimumBuyRate;
	}

	public Double getMinimumSellRate() {
		return minimumSellRate;
	}

	public void setMinimumSellRate(Double minimumSellRate) {
		this.minimumSellRate = minimumSellRate;
	}

	public Long getMaxInterval(RecordSide side) {
		Long interval = null;
		switch (side) {
			case BUY:
				interval = maxBuyInterval;
			break;
			case SELL:
				interval = maxSellInterval;
			break;
		}
		return interval;
	}

	public Long getMaxBuyInterval() {
		return maxBuyInterval;
	}

	public void setMaxBuyInterval(Long maxBuyInterval) {
		this.maxBuyInterval = maxBuyInterval;
	}

	public Long getMaxSellInterval() {
		return maxSellInterval;
	}

	public void setMaxSellInterval(Long maxSellInterval) {
		this.maxSellInterval = maxSellInterval;
	}

	public Double getMinimumCoinAmount() {
		return minimumCoinAmount;
	}

	public void setMinimumCoinAmount(Double minimumCoinAmount) {
		this.minimumCoinAmount = minimumCoinAmount;
	}

	public Double getIncDecPrice() {
		return incDecPrice;
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
	
}
