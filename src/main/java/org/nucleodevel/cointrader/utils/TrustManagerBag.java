package org.nucleodevel.cointrader.utils;

import java.security.SecureRandom;

import javax.net.ssl.X509TrustManager;

public class TrustManagerBag {

	/**
	 * Options to validate certificate. Values: - BYPASS: do not validate at all; -
	 * DEFAULT: use Java embedded validation; - CUSTOM: use a own implemented
	 * validation;
	 */
	public enum SslContextTrustManager {
		BYPASS, DEFAULT, CUSTOM
	}

	public static final X509TrustManager[] BYPASS_TRUST_MANAGER_LIST = new X509TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new java.security.cert.X509Certificate[0];
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			// Do nothing
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			// Do nothing
		}
	} };

	public static final SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

}