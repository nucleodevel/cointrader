package net.trader.beans;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;

public class Record implements Comparable<Record> {
	
	protected BigInteger id;
	protected BigInteger clientId;
	protected String coin;
	protected String currency;
	protected RecordSide side;
	protected BigDecimal coinAmount;
	protected BigDecimal currencyPrice;
	protected Calendar creationDate;
	
	public Record() {
		
	}

	public Record(BigInteger id) {
		this.id = id;
	}

	public Record(
		String coin, String currency, RecordSide side,
		BigDecimal coinAmount, BigDecimal currencyPrice
	) {
		this.coin = coin;
		this.currency = currency;
		this.side = side;
		this.coinAmount = coinAmount;
		this.currencyPrice = currencyPrice;
	}

	public Record getClone() {
		Record newOperation = new Record();
		newOperation.side = this.side;
		newOperation.coin = this.coin;
		newOperation.currency = this.currency;
		newOperation.coinAmount = this.coinAmount;
		newOperation.currencyPrice = this.currencyPrice;
		return newOperation;
	}

	public BigInteger getId() {
		return id;
	}

	public void setId(BigInteger id) {
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

	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
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
	
	public Calendar getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Calendar creationDate) {
		this.creationDate = creationDate;
	}
	
	@Override
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
		sb.append("; side: " + side);
		sb.append("; coinAmount: " + decFmt.format(coinAmount));
		sb.append("; currencyPrice: " + decFmt.format(currencyPrice));
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
