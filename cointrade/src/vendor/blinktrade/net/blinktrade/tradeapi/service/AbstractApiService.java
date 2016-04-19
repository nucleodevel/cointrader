/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.blinktrade.tradeapi.service;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import net.bilnktrade.common.security.HostnameVerifierBag;
import net.bilnktrade.common.security.TrustManagerBag;
import net.bilnktrade.common.security.TrustManagerBag.SslContextTrustManager;
import net.blinktrade.common.exception.BlinktradeException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;


/**
 * Class base for HTTPS API.
 */
public abstract class AbstractApiService {

	private static final String DOMAIN = "https://www.mercadobitcoin.net";
	
	protected enum HttpMethod {
		GET,
		POST
	}

	protected final boolean usingHttps() {
		return DOMAIN.toUpperCase().startsWith("HTTPS");
	}
	
	/**
	 * Starts a SSL connection for HTTPS Requests
	 * @throws BlinktradeException Generic exception to point any error with the execution.
	 */
	public AbstractApiService() throws BlinktradeException {
		try {
			if (usingHttps()) {
				setSslContext(SslContextTrustManager.DEFAULT);
			}
		} catch (KeyManagementException e) {
			throw new BlinktradeException("Internal error: Invalid SSL Connection.");
		} catch (NoSuchAlgorithmException e) {
			throw new BlinktradeException("Internal error: Invalid SSL Algorithm.");
		}
	}
	
	protected String getDomain() {
		return DOMAIN + getApiPath();
	}
	
	public abstract String getApiPath();
	
	/**
	 * Setup SSL Context to perform HTTPS communication.
	 * 
	 * @param sctm Selected way to validate certificates
	 */
	private final void setSslContext(SslContextTrustManager sctm)
					throws NoSuchAlgorithmException, KeyManagementException {
		// Enables protocols "TLSv1", "TLSv1.1" and "TLSv1.2"
		SSLContext sc = SSLContext.getInstance("TLS");

		switch (sctm) {
			case BYPASS:
				sc.init(null, TrustManagerBag.BYPASS_TRUST_MANAGER_LIST, TrustManagerBag.SECURE_RANDOM);
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
				HttpsURLConnection.setDefaultHostnameVerifier(HostnameVerifierBag.BYPASS_HOSTNAME_VERIFIER);
				break;
			case DEFAULT:
				HttpsURLConnection.setDefaultSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
				break;
			case CUSTOM:
				throw new NotImplementedException();
			default:
				throw new NotImplementedException();
		}
	}
	
	protected static final String encodeHexString(byte[] bytes) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	protected static final long generateTonce() {
		long unixTime = System.currentTimeMillis() / 1000L;
		return unixTime;
	}
	
}
