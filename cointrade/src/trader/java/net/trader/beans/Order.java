package net.trader.beans;

import java.math.BigDecimal;
import java.util.Calendar;

public abstract class Order implements Comparable<Order> {
	
	protected OrderSide side;
	private String currency;
	private String coin;
	protected BigDecimal currencyPrice;
	protected BigDecimal coinAmount;
	protected Calendar creationDate;

	public OrderSide getSide() {
		return side;
	}

	public void setSide(OrderSide side) {
		this.side = side;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

	public BigDecimal getCurrencyPrice() {
		return currencyPrice;
	}

	public void setCurrencyPrice(BigDecimal price) {
		this.currencyPrice = price;
	}
	
	public BigDecimal getCoinAmount() {
		return coinAmount;
	}

	public void setCoinAmount(BigDecimal coinAmount) {
		this.coinAmount = coinAmount;
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
	public int compareTo(Order another) {
		return this.getCreationDate().compareTo(another.getCreationDate());
	}

}
