package org.nucleodevel.cointrader.api.mercadobitcoin;

import java.util.Calendar;

import org.nucleodevel.cointrader.utils.Utils;

public class Authorized {

	private String accessToken;
	private Calendar expiration;

	public Authorized(String accessToken, Calendar expiration) {
		super();
		this.accessToken = accessToken;
		this.expiration = expiration;
	}

	public Authorized(String accessToken, long expirationInSeconds) {
		super();
		this.accessToken = accessToken;

		this.expiration = Calendar.getInstance();
		this.expiration.setTimeInMillis(expirationInSeconds * 1000);
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

	public boolean isAuthorized() {
		return accessToken != null;
	}

	public boolean isExpired() {
		Calendar now = Calendar.getInstance();
		return !isAuthorized() || expiration.compareTo(now) <= 0;
	}

	@Override
	public String toString() {
		String expirationStr = Utils.toISO8601UTC(expiration);
		return "Authorized [accessToken=" + accessToken + ", expiration=" + expirationStr + "]";
	}

}
