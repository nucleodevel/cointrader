package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;
import java.math.BigDecimal;

import net.trader.beans.Balance;
import net.trader.exception.ApiProviderException;

@SuppressWarnings("serial")
public class BtBalance extends Balance implements Serializable {

	private BigDecimal coinAmount;
	private BigDecimal coinLocked;
	private BigDecimal currencyAmount;
	private BigDecimal currencyLocked;
	private String clientID;
	private Integer balanceRequestID;
	
	public BtBalance(String coin, String currency) {
		super(coin, currency);
	}

	@Override
	public BigDecimal getCoinAmount() throws ApiProviderException {
		return coinAmount;
	}

	public void setCoinAmount(BigDecimal coinAmount) {
		this.coinAmount = coinAmount;
	}
	
	public BigDecimal getCoinLocked() {
		return coinLocked;
	}

	public void setCoinLocked(BigDecimal coinLocked) {
		this.coinLocked = coinLocked;
	}

	@Override
	public BigDecimal getCurrencyAmount() {
		return currencyAmount;
	}

	public void setCurrencyAmount(BigDecimal currencyAmount) {
		this.currencyAmount = currencyAmount;
	}

	public BigDecimal getCurrencyLocked() {
		return currencyLocked;
	}

	public void setCurrencyLocked(BigDecimal currencyLocked) {
		this.currencyLocked = currencyLocked;
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
		sb.append("coinAmount=").append(coinAmount);
		sb.append(", coinLocked=").append(coinLocked);
		sb.append(", currencyAmount=").append(currencyAmount);
		sb.append(", currencyLocked=").append(currencyLocked);
		sb.append(", clientID=").append(clientID);
		sb.append(", balanceRequestID=").append(balanceRequestID);
		sb.append('}');
		
		return sb.toString();
		
	}
	
}
