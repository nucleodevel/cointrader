package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;

import net.trader.beans.Balance;

@SuppressWarnings("serial")
public class BtBalance extends Balance implements Serializable {

	private Integer balanceRequestID;
	
	public BtBalance(String coin, String currency) {
		super(coin, currency);
	}
	
	public Integer getBalanceRequestID() {
		return balanceRequestID;
	}
	
	public void setBalanceRequestID(Integer balanceRequestID) {
		this.balanceRequestID = balanceRequestID;
	}
	
}
