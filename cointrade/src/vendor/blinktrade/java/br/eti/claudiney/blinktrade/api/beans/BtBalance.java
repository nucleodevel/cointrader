package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;
import java.math.BigDecimal;

import net.trader.beans.Balance;
import net.trader.exception.ApiProviderException;

@SuppressWarnings("serial")
public class BtBalance extends Balance implements Serializable {

	private BigDecimal currencyAmount;
	private BigDecimal currencyLocked;
	private BigDecimal btcAmount;
	private BigDecimal btcLocked;
	private String clientID;
	private Integer balanceRequestID;
	
	public BtBalance(String currency, String coin) {
		super(currency, coin);
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

	@Override
	public BigDecimal getCoinAmount() throws ApiProviderException {
		return getCoin().equals("BTC")? getBtcAmount(): null;
	}

	public BigDecimal getBtcAmount() {
		return btcAmount;
	}

	public void setBtcAmount(BigDecimal btcAmount) {
		this.btcAmount = btcAmount;
	}

	public BigDecimal getBtcLocked() {
		return btcLocked;
	}

	public void setBtcLocked(BigDecimal btcLocked) {
		this.btcLocked = btcLocked;
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