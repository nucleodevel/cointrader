package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

@SuppressWarnings("serial")
public class Balance implements Serializable {
	
	private BigInteger btcLocked;
	private BigDecimal currencyAmount;
	private BigInteger btcAmount;
	private BigDecimal currencyLocked;
	private String clientID;
	private Integer balanceRequestID;
	
	public void setBtcLocked(BigInteger btcLocked) {
		this.btcLocked = btcLocked;
	}
	
	public BigInteger getBtcLocked() {
		return btcLocked;
	}
	
	public void  setCurrencyAmount(BigDecimal currencyAmount) {
		this.currencyAmount = currencyAmount;
	}
	
	public BigDecimal getCurrencyAmount() {
		return currencyAmount;
	}
	
	public void setBtcAmount(BigInteger btcAmount) {
		this.btcAmount = btcAmount;
	}
	
	public BigInteger getBtcAmount() {
		return btcAmount;
	}
	
	public void setCurrencyLocked(BigDecimal currencyLocked) {
		this.currencyLocked = currencyLocked;
	}
	
	public BigDecimal getCurrencyLocked() {
		return currencyLocked;
	}
	
	public void setClientID(String clientID) {
		this.clientID = clientID;
	}
	
	public String getClientID() {
		return clientID;
	}
	
	public Integer getBalanceRequestID() {
		return balanceRequestID;
	}
	
	public void setBalanceRequestID(Integer balanceRequestID) {
		this.balanceRequestID = balanceRequestID;
	}

	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append('{');
		sb.append("btcLocked=").append(btcLocked);
		sb.append(", currencyAmount=").append(currencyAmount);
		sb.append(", btcAmount=").append(btcAmount);
		sb.append(", currencyLocked=").append(currencyLocked);
		sb.append(", clientID=").append(clientID);
		sb.append(", balanceRequestID=").append(balanceRequestID);
		sb.append('}');
		
		return sb.toString();
		
	}
	
}
