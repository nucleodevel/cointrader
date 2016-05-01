/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import net.trader.beans.Operation;
import net.trader.beans.Order;
import net.trader.beans.RecordSide;

public class MbOrder extends Order implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String status;
	
	private List<Operation> operations;

	public MbOrder() {
		super();
	}

	public MbOrder(BigInteger id) {
		this.id = id;
	}

	public MbOrder(
		String coin, String currency, RecordSide side,
		BigDecimal coinAmount, BigDecimal currencyPrice
	) {
		super(coin, currency, side, coinAmount, currencyPrice);
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
	
}