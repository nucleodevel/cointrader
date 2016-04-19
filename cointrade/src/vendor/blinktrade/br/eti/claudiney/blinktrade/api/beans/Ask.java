package br.eti.claudiney.blinktrade.api.beans;

import java.math.BigDecimal;

@SuppressWarnings("serial")
public class Ask extends SimpleOrder {

	public Ask() {
	}
	
	public Ask(BigDecimal currencyPrice, BigDecimal bitcoins) {
		super(currencyPrice, bitcoins); 
	}

}
