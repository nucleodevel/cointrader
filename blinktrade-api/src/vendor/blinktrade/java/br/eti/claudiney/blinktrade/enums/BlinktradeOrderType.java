package br.eti.claudiney.blinktrade.enums;

public enum BlinktradeOrderType {
	
	MARKET		("1"),
	LIMITED		("2"),
	;
	
	private BlinktradeOrderType(String orderType) {
		this.orderType = orderType;
	}
	
	private String orderType;
	
	public String getOrderType() {
		return orderType;
	}

}
