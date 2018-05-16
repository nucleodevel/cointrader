package br.net.dallan.cointrader.beans;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class Order extends Record {

	private OrderType type;
	private OrderStatus status;
	private Integer position;
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

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}
	
	@Override
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
		if (position != null)
			sb.append("; position: " + getPosition()); 
		sb.append("; side: " + getSide());
		sb.append("; status: " + status);
		sb.append("; coinAmount: " + decFmt.format(getCoinAmount()));
		sb.append("; currencyPrice: " + decFmt.format(getCurrencyPrice()));
		sb.append("; estimatedCurrencyAmount: " + decFmt.format(getCurrencyAmount()));
		if (getCreationDate() != null)
			sb.append("; creationDate: " + getCreationDate().getTime());
		sb.append("]");
		
		return sb.toString();
	}

}
