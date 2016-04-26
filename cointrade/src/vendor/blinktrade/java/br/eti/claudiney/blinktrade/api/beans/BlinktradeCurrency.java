package br.eti.claudiney.blinktrade.api.beans;

import java.math.BigDecimal;

public class BlinktradeCurrency {
	
	public static BlinktradeCurrency BRL = new BlinktradeCurrency("BRL", new BigDecimal("100000000.0"), (byte)2);
	public static BlinktradeCurrency VEF = new BlinktradeCurrency("VEF", new BigDecimal("1.0"), (byte)1);
	public static BlinktradeCurrency PKR = new BlinktradeCurrency("PKR", new BigDecimal("1.0"), (byte)1);
	public static BlinktradeCurrency CLP = new BlinktradeCurrency("CLP", new BigDecimal("1.0"), (byte)1);
	public static BlinktradeCurrency VND = new BlinktradeCurrency("VND", new BigDecimal("1.0"), (byte)1);
	
	public static final BlinktradeCurrency[] SUPPORTED_CURRENCIES = {
		BRL, VEF, PKR, CLP, VND
	};

	public BlinktradeCurrency(String id, BigDecimal rate, byte rateSize) {
		this.id = id;
		this.rate = rate;
		this.rateSize = rateSize;
	}

	private String id;

	public String getId() {
		return id;
	}

	private BigDecimal rate;

	public BigDecimal getRate() {
		return rate;
	}

	private byte rateSize;

	public byte getRateSize() {
		return rateSize;
	}
	
	public static BlinktradeCurrency getCurrencyBySimbol(String symbol) {
		
		if (symbol == null) 
			return null;
		for(BlinktradeCurrency c: SUPPORTED_CURRENCIES) {
			if( c.getId().equals(symbol) ) {
				return c;
			}
		}
		
		return null;
		
	}
	
}
