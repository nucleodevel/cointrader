package br.eti.claudiney.blinktrade.enums;

public enum BlinktradeOrderSide {
	
	BUY		("1"),
	SELL	("2"),
	;
	
	private BlinktradeOrderSide(String side) {
		this.side = side;
	}
	
	private String side;
	
	public String getSide() {
		return side;
	}

}
