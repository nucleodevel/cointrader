package br.eti.claudiney.blinktrade.exception;

@SuppressWarnings("serial")
public class BlinktradeAPIException extends Exception {

	public BlinktradeAPIException() {
		super();
	}
	
	public BlinktradeAPIException(String message) {
		super(message);
	}
	
	public BlinktradeAPIException(Throwable t) {
		super(t);
	}
	
	public BlinktradeAPIException(String message, Throwable t) {
		super(message, t);
	}
	
}
