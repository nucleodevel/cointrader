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
	
	public OpenOrder() {
		
	}
	
	
	
	public OpenOrder(OpenOrder another) {
		super();
		this.clientCustomOrderID = another.getClientCustomOrderID();
		this.orderID = another.getOrderID();
		this.cumQty = another.getCumQty();
		this.ordStatus = another.getOrdStatus();
		this.leavesQty = another.getLeavesQty();
		this.cxlQty = another.getCxlQty();
		this.avgPx = another.getAvgPx();
		this.symbol = another.getSymbol();
		this.side = another.getSide();
		this.ordType = another.getOrdType();
		this.orderQty = another.getOrderQty();
		this.price = another.getPrice();
		this.orderDate = another.getOrderDate();
		this.volume = another.getVolume();
		this.timeInForce = another.getTimeInForce();
	}



	public BigInteger getClientCustomOrderID() {
		return clientCustomOrderID;
	}
	
	public void setClientCustomOrderID(BigInteger clientCustomOrderID) {
		this.clientCustomOrderID = clientCustomOrderID;
	}

	public String getOrderID() {
		return orderID;
	}

	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

	public BigDecimal getCumQty() {
		return cumQty;
	}

	public void setCumQty(BigDecimal cumQty) {
		this.cumQty = cumQty;
	}

	public String getOrdStatus() {
		return ordStatus;
	}

	public void setOrdStatus(String ordStatus) {
		this.ordStatus = ordStatus;
	}

	public BigDecimal getLeavesQty() {
		return leavesQty;
	}

	public void setLeavesQty(BigDecimal leavesQty) {
		this.leavesQty = leavesQty;
	}

	public BigDecimal getCxlQty() {
		return cxlQty;
	}

	public void setCxlQty(BigDecimal cxlQty) {
		this.cxlQty = cxlQty;
	}

	public BigDecimal getAvgPx() {
		return avgPx;
	}

	public void setAvgPx(BigDecimal avgPx) {
		this.avgPx = avgPx;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getSide() {
		return side;
	}

	public void setSide(String side) {
		this.side = side;
	}

	public String getOrdType() {
		return ordType;
	}

	public void setOrdType(String ordType) {
		this.ordType = ordType;
	}

	public BigDecimal getOrderQty() {
		return orderQty;
	}

	public void setOrderQty(BigDecimal orderQty) {
		this.orderQty = orderQty;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Calendar getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Calendar orderDate) {
		this.orderDate = orderDate;
	}

	public BigDecimal getVolume() {
		return volume;
	}

	public void setVolume(BigDecimal volume) {
		this.volume = volume;
	}

	public String getTimeInForce() {
		return timeInForce;
	}

	public void setTimeInForce(String timeInForce) {
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
		
		sb.append('{');
		//sb.append("clientCustomOrderID=").append(clientCustomOrderID);
		//sb.append(", orderID=").append(orderID);
		sb.append("orderDate=").append(orderDate.getTime());
		sb.append(", side=").append(side.equals("1")? "BUY": "SELL");
		sb.append(", ordStatus=").append(ordStatus.equals("2")? "FINISHED": "NOT FINISHED");
		sb.append(", price=").append(price);
		sb.append(", volume=").append(volume.divide(new BigDecimal(100000000)));
		sb.append(", cumQty=").append(cumQty);
		/*sb.append(", leavesQty=").append(leavesQty);
		sb.append(", cxlQty=").append(cxlQty);
		sb.append(", avgPx=").append(avgPx);
		sb.append(", symbol=").append(symbol);
		sb.append(", ordType=").append(ordType);
		sb.append(", orderQty=").append(orderQty);*/
		sb.append("}");
		
		return sb.toString();
		
	}

}
