package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;
import java.math.BigDecimal;

import net.trader.beans.Order;

@SuppressWarnings("serial")
public class BtSimpleOrder extends Order implements Serializable {
	
	private BigDecimal currencyPrice;
	private BigDecimal bitcoins;
	private String clientID;
	
	public BtSimpleOrder() {
	}
	
	BtSimpleOrder(BigDecimal currencyPrice, BigDecimal bitcoins) {
		this.currencyPrice = currencyPrice;
		this.bitcoins = bitcoins;
	}
	
	public BigDecimal getCurrencyPrice() {
		return currencyPrice;
	}
	
	public BigDecimal getBitcoins() {
		return bitcoins;
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
		sb.append(", bitcoins=").append(bitcoins.divide(new BigDecimal(100000000)));
		sb.append(", clientID=").append(clientID);
		sb.append('}');
		
		return sb.toString();
		
	}

}
