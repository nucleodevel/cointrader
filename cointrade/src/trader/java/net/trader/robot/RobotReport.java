package net.trader.robot;

public abstract class RobotReport {
	
	private UserInformation userInformation;
	private String currency;
	private String coin;
	
	public RobotReport(UserInformation userInformation, String currency, String coin) {
		this.userInformation = userInformation;
		this.currency = currency;
		this.coin = coin;
	}
	
	public UserInformation getUserInformation() {
		return userInformation;
	}

	public void setUserInformation(UserInformation userInformation) {
		this.userInformation = userInformation;
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
