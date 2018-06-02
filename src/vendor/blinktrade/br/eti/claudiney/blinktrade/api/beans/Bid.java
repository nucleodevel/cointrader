package br.eti.claudiney.blinktrade.api.beans;

import java.math.BigDecimal;

@SuppressWarnings("serial")
public class Bid extends SimpleOrder {
	
	public Bid() {
	}
	
	public Bid(BigDecimal currencyPrice, BigDecimal bitcoins) {
		super(currencyPrice, bitcoins); 
	}
	
}
