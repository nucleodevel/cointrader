package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;
import java.math.BigDecimal;

import net.trader.beans.Order;

@SuppressWarnings("serial")
public class BtSimpleOrder extends Order implements Serializable {
	
	private String clientID;
	
	public BtSimpleOrder() {
	}
	
	BtSimpleOrder(BigDecimal currencyPrice, BigDecimal coinAmount) {
		this.currencyPrice = currencyPrice;
		this.coinAmount = coinAmount;
	}
	
	@SuppressWarnings("unchecked")
	<T> T setClientID(String clientID) {
		this.clientID = clientID;
		return (T) this;
	}
	
	public String getClientID() {
		return clientID;
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append('{');
		sb.append("currencyPrice=").append(currencyPrice);
		sb.append(", coinAmount=").append(coinAmount.divide(new BigDecimal(100000000)));
		sb.append(", clientID=").append(clientID);
		sb.append('}');
		
		return sb.toString();
		
	}

	@Override
	public String toDisplayString() {
		return toString();
	}

}