package br.net.dallan.cointrader.utils;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import sun.security.util.HostnameChecker;

public class HostnameVerifierBag {

	public static final HostnameVerifier HOSTNAME_VERIFIER = new HostnameVerifier() {
		/**
		 * Checks if a given hostname matches the certificate of a given session.
		 */
		private boolean hostnameMatches(String hostname, SSLSession session) {
			HostnameChecker checker = HostnameChecker.getInstance(HostnameChecker.TYPE_TLS);

			boolean validCertificate = false;
			
			try {
				Certificate[] peerCertificates = session.getPeerCertificates();

				if ((peerCertificates.length > 0)
								&& (peerCertificates[0] instanceof X509Certificate)) {
					X509Certificate peerCertificate = (X509Certificate) peerCertificates[0];

					checker.match(hostname, peerCertificate);
					validCertificate = true;
				}
			} catch (CertificateException ex) {
				validCertificate = false;
			} catch (SSLPeerUnverifiedException e) {
				validCertificate = false;
			}
			
			return validCertificate;
		}

		/**
		 * Default method that invokes hostname validation.
		 */
		public boolean verify(String hostname, SSLSession session) {
			if (hostnameMatches(hostname, session)) {
				return true;
			} else {
				return false;
			}
		}
	};
	
	public static final HostnameVerifier BYPASS_HOSTNAME_VERIFIER = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

}