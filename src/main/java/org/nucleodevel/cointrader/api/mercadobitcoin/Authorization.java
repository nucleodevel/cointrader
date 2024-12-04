package org.nucleodevel.cointrader.api.mercadobitcoin;

import java.util.Calendar;

public class Authorization {

	private String accessToken;
	private Calendar expiration;
	private long secondsBeforeExpiringToRenew;

	public Authorization(String accessToken, Calendar expiration, long secondsBeforeExpiringToRenew) {
		super();
		this.accessToken = accessToken;
		this.expiration = expiration;
		this.secondsBeforeExpiringToRenew = secondsBeforeExpiringToRenew;
	}

	public Authorization(String accessToken, long expirationInSeconds, long secondsBeforeExpiringToRenew) {
		super();
		this.accessToken = accessToken;

		this.expiration = Calendar.getInstance();
		this.expiration.setTimeInMillis(expirationInSeconds * 1000);
		this.secondsBeforeExpiringToRenew = secondsBeforeExpiringToRenew;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Calendar getExpiration() {
		return expiration;
	}

	public void setExpiration(Calendar expiration) {
		this.expiration = expiration;
	}

	public long getSecondsBeforeExpiringToRenew() {
		return secondsBeforeExpiringToRenew;
	}

	public void setSecondsBeforeExpiringToRenew(long secondsBeforeExpiringToRenew) {
		this.secondsBeforeExpiringToRenew = secondsBeforeExpiringToRenew;
	}

	public boolean isAuthorized() {
		return accessToken != null;
	}

	public boolean isExpired() {
		Calendar now = Calendar.getInstance();
		long secondsToExpire = (expiration.getTimeInMillis() - now.getTimeInMillis()) / 1000;
		return !isAuthorized() || secondsToExpire < secondsBeforeExpiringToRenew;
	}

	@Override
	public String toString() {
		String expirationStr = expiration.getTime().toString();
		Calendar now = Calendar.getInstance();
		long secondsToExpire = (expiration.getTimeInMillis() - now.getTimeInMillis()) / 1000;
		return "Authorizaztion [expiration=" + expirationStr + ", secondsToExpire=" + secondsToExpire + ", accessToken="
				+ accessToken + "]";
	}

}
