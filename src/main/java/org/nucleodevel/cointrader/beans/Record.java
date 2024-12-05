package org.nucleodevel.cointrader.beans;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;

public class Record implements Comparable<Record> {

	private CoinCurrencyPair coinCurrencyPair;
	private String id;
	private BigInteger clientId;
	private RecordSide side;
	private BigDecimal coinAmount;
	private BigDecimal currencyPrice;
	private Calendar creationDate;

	public Record(Coin coin, Currency currency, RecordSide side, BigDecimal coinAmount, BigDecimal currencyPrice) {
		this.coinCurrencyPair = new CoinCurrencyPair(coin, currency);
		this.side = side;
		this.coinAmount = coinAmount;
		this.currencyPrice = currencyPrice;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public BigInteger getClientId() {
		return clientId;
	}

	public void setClientId(BigInteger clientId) {
		this.clientId = clientId;
	}

	public RecordSide getSide() {
		return side;
	}

	public void setSide(RecordSide side) {
		this.side = side;
	}

	public BigDecimal getCoinAmount() {
		return coinAmount;
	}

	public void setCoinAmount(BigDecimal coinAmount) {
		this.coinAmount = coinAmount;
	}

	public BigDecimal getCurrencyPrice() {
		return currencyPrice;
	}

	public void setCurrencyPrice(BigDecimal price) {
		this.currencyPrice = price;
	}

	public BigDecimal getCurrencyAmount() {
		return coinAmount.multiply(currencyPrice);
	}

	public BigDecimal getSideAmount(RecordSide side) {
		return side == RecordSide.BUY ? getCurrencyAmount() : (side == RecordSide.SELL ? getCoinAmount() : null);
	}

	public Calendar getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Calendar creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public String toString() {
		DecimalFormat defaultDecFmt = new DecimalFormat();
		defaultDecFmt.setMaximumFractionDigits(8);
		DecimalFormatSymbols coinDecFmtSymbols = defaultDecFmt.getDecimalFormatSymbols();
		coinDecFmtSymbols.setDecimalSeparator('.');
		coinDecFmtSymbols.setGroupingSeparator(',');
		defaultDecFmt.setDecimalFormatSymbols(coinDecFmtSymbols);

		DecimalFormat currencyAmountDecFmt = new DecimalFormat();
		currencyAmountDecFmt.setMaximumFractionDigits(3);
		DecimalFormatSymbols currencyDecFmtSymbols = currencyAmountDecFmt.getDecimalFormatSymbols();
		currencyDecFmtSymbols.setDecimalSeparator('.');
		currencyDecFmtSymbols.setGroupingSeparator(',');
		currencyAmountDecFmt.setDecimalFormatSymbols(currencyDecFmtSymbols);

		StringBuilder sb = new StringBuilder();

		sb.append(this.getClass().getSimpleName() + ": [");
		sb.append("coin: " + getCoin());
		sb.append("; currency: " + getCurrency());
		sb.append("; side: " + side);
		sb.append("; coinAmount: " + defaultDecFmt.format(coinAmount));
		sb.append("; currencyPrice: " + defaultDecFmt.format(currencyPrice));
		sb.append("; estimatedCurrencyAmount: " + currencyAmountDecFmt.format(getCurrencyAmount()));
		if (creationDate != null)
			sb.append("; creationDate: " + creationDate.getTime());
		sb.append("]");

		return sb.toString();
	}

	@Override
	public int compareTo(Record another) {
		return -1 * this.getCreationDate().compareTo(another.getCreationDate());
	}

}
