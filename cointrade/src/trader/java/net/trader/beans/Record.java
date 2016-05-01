package net.trader.beans;

import java.math.BigDecimal;
import java.util.Calendar;

public class Record implements Comparable<Record> {
	
	protected RecordSide side;
	protected String coin;
	protected String currency;
	protected BigDecimal coinAmount;
	protected BigDecimal currencyPrice;
	protected Calendar creationDate;
	
	public Record getClone() {
		Record newOperation = new Record();
		newOperation.side = this.side;
		newOperation.coin = this.coin;
		newOperation.currency = this.currency;
		newOperation.coinAmount = this.coinAmount;
		newOperation.currencyPrice = this.currencyPrice;
		return newOperation;
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

	public String toDisplayString() {
		return null;
	}

	@Override
	public int compareTo(Record another) {
		return -1 * this.getCreationDate().compareTo(another.getCreationDate());
	}

}
