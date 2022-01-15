package org.nucleodevel.cointrader.beans;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class Balance {
	
	private static final BigDecimal CURRENCY_MARGIN = new BigDecimal(0.00001);

	private CoinCurrencyPair coinCurrencyPair;
	private BigDecimal coinAmount;
	private BigDecimal coinLocked;
	private BigDecimal currencyAmount;
	private BigDecimal currencyLocked;
	private String clientId;

	public Balance(Coin coin, Currency currency) {
		this.coinCurrencyPair = new CoinCurrencyPair(coin, currency);
	}

	public CoinCurrencyPair getCoinCurrencyPair() {
		return coinCurrencyPair;
	}

	public void setCoinCurrencyPair(CoinCurrencyPair coinCurrencyPair) {
		this.coinCurrencyPair = coinCurrencyPair;
	}
	
	public Coin getCoin() {
		return coinCurrencyPair.getCoin();
	}

	public void setCoin(Coin coin) {
		coinCurrencyPair.setCoin(coin);
	}

	public Currency getCurrency() {
		return coinCurrencyPair.getCurrency();
	}

	public void setCurrency(Currency currency) {
		coinCurrencyPair.setCurrency(currency);
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

	public BigDecimal getSideAmount(RecordSide side) {
		BigDecimal amount = new BigDecimal(0);
		switch (side) {
			case BUY:
				amount = currencyAmount.subtract(CURRENCY_MARGIN);
			break;
			case SELL:
				amount = coinAmount;
			break;
		}
		return amount;
	}

	public BigDecimal getEstimatedCoinAmount(RecordSide side, BigDecimal currencyPrice) {
		BigDecimal amount = new BigDecimal(0);
		switch (side) {
			case BUY:
				amount = new BigDecimal(
					(currencyAmount.doubleValue() - CURRENCY_MARGIN.doubleValue()) / 
					currencyPrice.doubleValue()
				);
			break;
			case SELL:
				amount = coinAmount;
			break;
		}
		return amount;
	}

	public BigDecimal getEstimatedCurrencyAmount(RecordSide side, BigDecimal currencyPrice) {
		BigDecimal amount = new BigDecimal(0);
		switch (side) {
			case BUY:
				amount = currencyAmount;
			break;
			case SELL:
				amount = new BigDecimal(coinAmount.doubleValue() * currencyPrice.doubleValue());
			break;
		}
		return amount;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String toString() {
		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(8);
		DecimalFormatSymbols symbols=decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.getClass().getSimpleName() + ": ["); 
		sb.append("coin: " + getCoin());
		sb.append("; currency: " + getCurrency());
		sb.append("; coinAmount: " + (coinAmount == null? "0.0": decFmt.format(coinAmount)));
		if (coinLocked != null)
			sb.append("; coinLocked: " + decFmt.format(coinLocked));
		sb.append("; currencyAmount: " + decFmt.format(currencyAmount));
		if (currencyLocked != null)
			sb.append("; currencyLocked: " + decFmt.format(currencyLocked));
		sb.append("; clientId: " + clientId);
		sb.append(']');
		
		return sb.toString();
		
	}

}