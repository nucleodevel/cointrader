/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import net.mercadobitcoin.util.EnumValue;
import net.trader.beans.Operation;
import net.trader.beans.Order;
import net.trader.beans.RecordSide;

public class MbOrder extends Order implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	
	public enum OrderStatus implements EnumValue {
		ACTIVE("active"),
		CANCELED("canceled"),
		COMPLETED("completed");
		private final String value;

		private OrderStatus(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	public static final BigDecimal MINIMUM_VOLUME = new BigDecimal(0.01);
	public static final BigDecimal BITCOIN_24H_WITHDRAWAL_LIMIT = new BigDecimal(25);
	public static final int BITCOIN_DEPOSIT_CONFIRMATIONS = 6;
	
	public static final BigDecimal LITECOIN_24H_WITHDRAWAL_LIMIT = new BigDecimal(25);
	public static final int LITECOIN_DEPOSIT_CONFIRMATIONS = 15;
	
	private Long orderId;
	private String status;
	
	private List<Operation> operations;
	
	private Boolean flagSmall = false;

	
	public MbOrder() {
		
	}

	public MbOrder(String coin, String currency, RecordSide side, BigDecimal coinAmount, BigDecimal currencyPrice) {
		this.coin = coin;
		this.currency = currency;
		this.coinAmount = coinAmount;
		this.currencyPrice = currencyPrice;
		this.side = side;
		this.flagSmall = true;
	}

	public MbOrder(Long orderId) {
		
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}

	public Calendar getCreatedDate() {
		return creationDate;
	}

	public void setCreatedDate(Calendar creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public String toString() {
		if (this.flagSmall == true) {
			return "\nOrder [coin=" + coin + ", currency=" + currency 
					+ ", side=" + side + ", coinAmount=" + coinAmount
					+ ", price=" + currencyPrice + "]";
		} else {
			return "Order [coin=" + coin + ", currency=" + currency 
					+ ", side=" + side + ", coinAmount=" + coinAmount
					+ ", price=" + currencyPrice + ", orderId=" + orderId + ", status="
					+ status + ", created=" + creationDate.getTime() + ", operations="
					+ operations + "]";
		}
	}

	@Override
	public String toDisplayString() {
		return toString();
	}
	
}