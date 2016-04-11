package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

@SuppressWarnings("serial")
public class OpenOrder implements Serializable {

	private BigInteger clientCustomOrderID;
	private String orderID;
	private BigDecimal cumQty;
	private String ordStatus;
	private BigDecimal leavesQty;
	private BigDecimal cxlQty;
	private BigDecimal avgPx;
	private String symbol;
	private String side;
	private String ordType;
	private BigDecimal orderQty;
	private BigDecimal price;
	private Calendar orderDate;
	private BigDecimal volume;
	private String timeInForce;
	
	public BigInteger getClientCustomOrderID() {
		return clientCustomOrderID;
	}
	
	void setClientCustomOrderID(BigInteger clientCustomOrderID) {
		this.clientCustomOrderID = clientCustomOrderID;
	}

	public String getOrderID() {
		return orderID;
	}

	void setOrderID(String orderID) {
		this.orderID = orderID;
	}

	public BigDecimal getCumQty() {
		return cumQty;
	}

	void setCumQty(BigDecimal cumQty) {
		this.cumQty = cumQty;
	}

	public String getOrdStatus() {
		return ordStatus;
	}

	void setOrdStatus(String ordStatus) {
		this.ordStatus = ordStatus;
	}

	public BigDecimal getLeavesQty() {
		return leavesQty;
	}

	void setLeavesQty(BigDecimal leavesQty) {
		this.leavesQty = leavesQty;
	}

	public BigDecimal getCxlQty() {
		return cxlQty;
	}

	void setCxlQty(BigDecimal cxlQty) {
		this.cxlQty = cxlQty;
	}

	public BigDecimal getAvgPx() {
		return avgPx;
	}

	void setAvgPx(BigDecimal avgPx) {
		this.avgPx = avgPx;
	}

	public String getSymbol() {
		return symbol;
	}

	void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getSide() {
		return side;
	}

	void setSide(String side) {
		this.side = side;
	}

	public String getOrdType() {
		return ordType;
	}

	void setOrdType(String ordType) {
		this.ordType = ordType;
	}

	public BigDecimal getOrderQty() {
		return orderQty;
	}

	void setOrderQty(BigDecimal orderQty) {
		this.orderQty = orderQty;
	}

	public BigDecimal getPrice() {
		return price;
	}

	void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Calendar getOrderDate() {
		return orderDate;
	}

	void setOrderDate(Calendar orderDate) {
		this.orderDate = orderDate;
	}

	public BigDecimal getVolume() {
		return volume;
	}

	void setVolume(BigDecimal volume) {
		this.volume = volume;
	}

	public String getTimeInForce() {
		return timeInForce;
	}

	void setTimeInForce(String timeInForce) {
		this.timeInForce = timeInForce;
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(getClass().getSimpleName());
		
		sb.append('{');
		sb.append("clientCustomOrderID=").append(clientCustomOrderID);
		sb.append(", orderID=").append(orderID);
		sb.append(", cumQty=").append(cumQty);
		sb.append(", ordStatus=").append(ordStatus);
		sb.append(", leavesQty=").append(leavesQty);
		sb.append(", cxlQty=").append(cxlQty);
		sb.append(", avgPx=").append(avgPx);
		sb.append(", symbol=").append(symbol);
		sb.append(", side=").append(side);
		sb.append(", ordType=").append(ordType);
		sb.append(", orderQty=").append(orderQty);
		sb.append(", price=").append(price);
		sb.append(", orderDate=").append(orderDate);
		sb.append(", volume=").append(volume);
		sb.append(", timeInForce=").append(timeInForce);
		
		sb.append('}');
		
		return sb.toString();
		
	}
	
	public String toDisplayString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(getClass().getSimpleName());
		
		sb.append('{');
		sb.append("clientCustomOrderID=").append(clientCustomOrderID);
		sb.append("\n\torderID=").append(orderID);
		sb.append("\n\tprice=").append(price);
		sb.append("\n\tcumQty=").append(cumQty);
		sb.append("\n\tordStatus=").append(ordStatus);
		sb.append("\n\tleavesQty=").append(leavesQty);
		sb.append("\n\tcxlQty=").append(cxlQty);
		sb.append("\n\tavgPx=").append(avgPx);
		sb.append("\n\tsymbol=").append(symbol);
		sb.append("\n\tside=").append(side);
		sb.append("\n\tordType=").append(ordType);
		sb.append("\n\torderQty=").append(orderQty);
		sb.append("\n\tvolume=").append(volume);
		sb.append("\n}");
		
		return sb.toString();
		
	}

}
