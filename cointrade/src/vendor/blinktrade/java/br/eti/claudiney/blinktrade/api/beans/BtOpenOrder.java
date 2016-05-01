package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;

import net.trader.beans.Order;

@SuppressWarnings("serial")
public class BtOpenOrder extends Order implements Serializable {

	private String ordStatus;
	private String ordType;
	
	public BtOpenOrder() {
		
	}
	
	public BtOpenOrder(BtOpenOrder another) {
		super();
		this.clientId = another.getClientId();
		this.ordStatus = another.getOrdStatus();
		this.side = another.getSide();
		this.ordType = another.getOrdType();
		this.creationDate = another.getCreationDate();
		this.coinAmount = another.getCoinAmount();
		this.currencyPrice = another.getCurrencyPrice();	
	}

	public String getOrdStatus() {
		return ordStatus;
	}

	public void setOrdStatus(String ordStatus) {
		this.ordStatus = ordStatus;
	}

	public String getOrdType() {
		return ordType;
	}

	public void setOrdType(String ordType) {
		this.ordType = ordType;
	}

}
