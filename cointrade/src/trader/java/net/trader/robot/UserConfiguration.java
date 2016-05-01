package net.trader.robot;

public class UserConfiguration {

	private String key;
	private String secret;
	private String provider;
	private String broker;
	
	private Integer delayTime;
	private String operationMode;
	private String coin;
	private String currency;
	private Double minimumBuyRate;
	private Double minimumSellRate;
	private Double minimumCoinAmount;
	private Double incDecPrice;
	private Double sellRateAfterBreakdown;
	
	public UserConfiguration() {
		
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

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getBroker() {
		return broker;
	}

	public void setBroker(String broker) {
		this.broker = broker;
	}

	public Integer getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(Integer delayTime) {
		this.delayTime = delayTime;
	}

	public String getOperationMode() {
		return operationMode;
	}

	public void setOperationMode(String operationMode) {
		this.operationMode = operationMode;
	}

	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
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

	public Double getSellRateAfterBreakdown() {
		return sellRateAfterBreakdown;
	}

	public void setSellRateAfterBreakdown(Double sellRateAfterBreakdown) {
		this.sellRateAfterBreakdown = sellRateAfterBreakdown;
	}
	
}
