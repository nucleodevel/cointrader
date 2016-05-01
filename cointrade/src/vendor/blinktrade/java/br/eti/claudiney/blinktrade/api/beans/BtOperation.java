package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

import net.trader.beans.Operation;

@SuppressWarnings("serial")
public class BtOperation extends Operation implements Serializable {

	private BigInteger clientCustomOrderID;
	private BigDecimal cumQty;
	private String ordStatus;
	private BigDecimal leavesQty;
	private BigDecimal cxlQty;
	private BigDecimal avgPx;
	private String ordType;
	private BigDecimal orderQty;
	private BigDecimal volume;
	private String timeInForce;
	
	public BtOperation() {
		
	}
	
	public BtOperation(BtOperation another) {
		super();
		this.clientCustomOrderID = another.getClientCustomOrderID();
		this.cumQty = another.getCumQty();
		this.ordStatus = another.getOrdStatus();
		this.leavesQty = another.getLeavesQty();
		this.cxlQty = another.getCxlQty();
		this.avgPx = another.getAvgPx();
		this.side = another.getSide();
		this.ordType = another.getOrdType();
		this.orderQty = another.getOrderQty();
		this.creationDate = another.getOrderDate();
		this.volume = another.getVolume();
		this.timeInForce = another.getTimeInForce();
		this.coinAmount = this.cumQty.add(this.leavesQty);	
		this.currencyPrice = another.getCurrencyPrice();
	}

	public BigInteger getClientCustomOrderID() {
		return clientCustomOrderID;
	}
	
	public void setClientCustomOrderID(BigInteger clientCustomOrderID) {
		this.clientCustomOrderID = clientCustomOrderID;
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
		return coin + currency;
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

	public Calendar getOrderDate() {
		return creationDate;
	}

	public void setOrderDate(Calendar creationDate) {
		this.creationDate = creationDate;
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

}
