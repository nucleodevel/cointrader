package net.trader.robot;

public abstract class RobotReport {
	
	private UserConfiguration userConfiguration;
	private String currency;
	private String coin;
	
	public RobotReport(UserConfiguration userConfiguration, String currency, String coin) {
		this.userConfiguration = userConfiguration;
		this.currency = currency;
		this.coin = coin;
	}
	
	public UserConfiguration getUserConfiguration() {
		return userConfiguration;
	}

	public void setUserConfiguration(UserConfiguration userConfiguration) {
		this.userConfiguration = userConfiguration;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

}
