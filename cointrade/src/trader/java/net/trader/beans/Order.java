package net.trader.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Order extends Record {

	private OrderType type;
	private OrderStatus status;
	private List<Operation> operations;

	public Order(
		Coin coin, Currency currency, RecordSide side,
		BigDecimal coinAmount, BigDecimal currencyPrice
	) {
		super(coin, currency, side, coinAmount, currencyPrice);
		this.operations = new ArrayList<Operation>();
	}

	public OrderType getType() {
		return type;
	}

	public void setType(OrderType type) {
		this.type = type;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}

}
