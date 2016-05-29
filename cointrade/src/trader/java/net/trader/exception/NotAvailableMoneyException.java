package net.trader.exception;

@SuppressWarnings("serial")
public class NotAvailableMoneyException extends Exception {

	public NotAvailableMoneyException() {
		super();
	}
	
	public NotAvailableMoneyException(String message) {
		super(message);
	}
	
	public NotAvailableMoneyException(Throwable t) {
		super(t);
	}
	
	public NotAvailableMoneyException(String message, Throwable t) {
		super(message, t);
	}
	
}
