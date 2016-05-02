package net.trader.beans;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class Balance {

	protected Coin coin;
	protected Currency currency;
	protected BigDecimal coinAmount;
	protected BigDecimal coinLocked;
	protected BigDecimal currencyAmount;
	protected BigDecimal currencyLocked;
	protected String clientId;
	
	public Balance(Coin coin, Currency currency) {
		this.coin = coin;
		this.currency = currency;
	}

	public Coin getCoin() {
		return coin;
	}

	public void setCoin(Coin coin) {
		this.coin = coin;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public BigDecimal getCoinAmount() {
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

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String toString() {
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(5);
		DecimalFormatSymbols symbols=decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.getClass().getSimpleName() + ": ["); 
		sb.append("coin: " + coin);
		sb.append("; currency: " + currency);
		sb.append("; coinAmount: " + decFmt.format(coinAmount));
		sb.append("; coinLocked: " + decFmt.format(coinLocked));
		sb.append("; currencyAmount: " + decFmt.format(currencyAmount));
		sb.append("; currencyLocked: " + decFmt.format(currencyLocked));
		sb.append("; clientId: " + clientId);
		sb.append(']');
		
		return sb.toString();
		
	}

}
