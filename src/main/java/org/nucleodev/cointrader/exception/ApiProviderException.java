package org.nucleodev.cointrader.exception;

@SuppressWarnings("serial")
public class ApiProviderException extends Exception {

	public ApiProviderException() {
		super();
	}
	
	public ApiProviderException(String message) {
		super(message);
	}
	
	public ApiProviderException(Throwable t) {
		super(t);
	}
	
	public ApiProviderException(String message, Throwable t) {
		super(message, t);
	}
	
}
