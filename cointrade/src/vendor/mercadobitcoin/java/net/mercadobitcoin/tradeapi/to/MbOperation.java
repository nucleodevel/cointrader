/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;

import net.trader.beans.Operation;

public class MbOperation extends Operation implements Serializable {

	private static final long serialVersionUID = -3345636873296069825L;

	private Long operationId;
	private BigDecimal rate;
	private Integer created;
	private Calendar createdDate;

	public MbOperation() {
		
	}
	
	public MbOperation(MbOperation another) {
		this.created = another.getCreated();
		this.coinAmount = another.getCoinAmount();
		this.currencyPrice = another.getCurrencyPrice();
		this.operationId = another.getOperationId();
		this.side = another.getSide();

		this.rate = another.getRate();
		
		this.createdDate = another.getCreatedDate();
	}

	public MbOperation(Long operationId) {
		this.operationId = operationId;
	}

	public Long getDate() {
		return (long) created;
	}

	public Long getTid() {
		return operationId;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public Integer getCreated() {
		return created;
	}

	public void setCreated(Integer created) {
		this.created = created;
	}

	public Long getOperationId() {
		return operationId;
	}

	public void setOperationId(Long operationId) {
		this.operationId = operationId;
	}

	public Calendar getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Calendar createdDate) {
		this.createdDate = createdDate;
	}

	@Override
	public String toString() {
		if (this.side != null) {
			return "\nOperation [date=" + createdDate.getTime() + ", price=" + currencyPrice
					+ ", amount=" + coinAmount + ", tid=" + operationId + ", side="
					+ side + "]";
		} else {
			return "Operation [operationId=" + operationId + ", volume="
					+ coinAmount + ", price=" + currencyPrice + ", rate=" + rate
					+ ", created=" + createdDate.getTime() + "]";
		}
	}

	@Override
	public String toDisplayString() {
		return toString();
	}

}
