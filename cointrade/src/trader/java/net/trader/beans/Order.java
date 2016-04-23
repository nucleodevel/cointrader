package net.trader.beans;

import java.math.BigDecimal;

import net.mercadobitcoin.util.EnumValue;

public abstract class Order implements Comparable<Order> {
	
	/**
	 * Defines the Type of the Order (Buy or Sell).
	 */
	public enum OrderSide implements EnumValue {
		BUY("buy"),
		SELL("sell");
		private final String value;

		private OrderSide(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	protected OrderSide side;
	protected BigDecimal currencyPrice;
	protected BigDecimal coinAmount;

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

	public abstract Long getDate();

	public OrderSide getSide() {
		return side;
	}

	public void setSide(OrderSide side) {
		this.side = side;
	}
	
	public abstract String toDisplayString();

	@Override
	public int compareTo(Order another) {
		return this.getDate().compareTo(another.getDate());
	}

}
