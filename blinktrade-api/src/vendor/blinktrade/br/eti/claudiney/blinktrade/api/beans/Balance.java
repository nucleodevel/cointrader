package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

@SuppressWarnings("serial")
public class Balance implements Serializable {
	
	private BigInteger btcLocked;
	private BigDecimal currencyAmount;
	private BigInteger bitcoinAmount;
	private BigDecimal currencyLocked;
	private String clientID;
	private Integer balanceRequestID;
	
	Balance(Integer balanceRequestId) {
		this.balanceRequestID = balanceRequestId;
	}
	
	Balance setBtcLocked(BigInteger btcLocked) {
		this.btcLocked = btcLocked;
		return this;
	}
	
	public BigInteger getBtcLocked() {
		return btcLocked;
	}
	
	Balance setCurrencyAmount(BigDecimal currencyAmount) {
		this.currencyAmount = currencyAmount;
		return this;
	}
	
	public BigDecimal getCurrencyAmount() {
		return currencyAmount;
	}
	
	Balance setBitcoinAmount(BigInteger bitcoinAmount) {
		this.bitcoinAmount = bitcoinAmount;
		return this;
	}
	
	public BigInteger getBitcoinAmount() {
		return bitcoinAmount;
	}
	
	Balance setCurrencyLocked(BigDecimal currencyLocked) {
		this.currencyLocked = currencyLocked;
		return this;
	}
	
	public BigDecimal getCurrencyLocked() {
		return currencyLocked;
	}
	
	Balance setClientID(String clientID) {
		this.clientID = clientID;
		return this;
	}
	
	public String getClientID() {
		return clientID;
	}
	
	public Integer getBalanceRequestID() {
		return balanceRequestID;
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(getClass().getSimpleName());
		sb.append('{');
		sb.append("btcLocked=").append(btcLocked);
		sb.append(", currencyAmount=").append(currencyAmount);
		sb.append(", bitcoinAmount=").append(bitcoinAmount);
		sb.append(", currencyLocked=").append(currencyLocked);
		sb.append(", clientID=").append(clientID);
		sb.append(", balanceRequestID=").append(balanceRequestID);
		sb.append('}');
		
		return sb.toString();
		
	}
	
}
