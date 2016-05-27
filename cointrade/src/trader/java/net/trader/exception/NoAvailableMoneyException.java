package net.trader.exception;

@SuppressWarnings("serial")
public class NoAvailableMoneyException extends Exception {

	public NoAvailableMoneyException() {
		super();
	}
	
	public NoAvailableMoneyException(String message) {
		super(message);
	}
	
	public NoAvailableMoneyException(Throwable t) {
		super(t);
	}
	
	public NoAvailableMoneyException(String message, Throwable t) {
		super(message, t);
	}
	
}
