/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;

import net.trader.beans.Order;

import com.eclipsesource.json.JsonObject;

/**
 * Operation of an order. Contains:
 *
 * <b>operationId</b>: Operation's ID.
 * <b>volume</b>: Volume dealt with by the operation.
 * <b>price</b>: Operation's unit price, in Brazilian Real.
 * <b>rate</b>: Tax's percentage applied to the operation.
 * <b>created</b>: Operation's Unix time.
 */
public class Operation extends Order implements Serializable {

	private static final long serialVersionUID = -3345636873296069825L;

	private Long operationId;
	private BigDecimal rate;
	private Integer created;
	private Calendar createdDate;

	/**
	 * Constructor based on JSON response.
	 * 
	 * @param jsonObject Trade API JSON response
	 */
	public Operation(JsonObject jsonObject) {
		this.created = Integer.valueOf(jsonObject.get("date").toString());
		this.currencyPrice = new BigDecimal(jsonObject.get("price").toString());
		this.coinAmount = new BigDecimal(jsonObject.get("amount").toString());
		this.operationId = jsonObject.get("tid").asLong();
		this.side = OrderSide.valueOf(jsonObject.get("side").asString()
				.toUpperCase());

		this.rate = null;
		
		this.createdDate = Calendar.getInstance();
		this.createdDate.setTimeInMillis((long)created * 1000);
	}
	
	public Operation(Operation another) {
		this.created = another.getCreated();
		this.currencyPrice = another.getCurrencyPrice();
		this.coinAmount = another.getCoinAmount();
		this.operationId = another.getOperationId();
		this.side = another.getSide();

		this.rate = another.getRate();
		
		this.createdDate = another.getCreatedDate();
	}

	/**
	 * Constructor based on JSON response.
	 * 
	 * @param operationId Operation Identifier
	 * @param jsonObject Trade API JSON response
	 */
	public Operation(Long operationId, JsonObject jsonObject) {
		this.operationId = operationId;
		this.coinAmount = new BigDecimal(jsonObject.get("volume").asString());
		this.currencyPrice = new BigDecimal(jsonObject.get("price").asString());
		this.rate = new BigDecimal(jsonObject.get("rate").asString());
		this.created = Integer.valueOf(jsonObject.get("created").asString());

		this.side = null;
		
		this.createdDate = Calendar.getInstance();
		this.createdDate.setTimeInMillis((long)created * 1000);
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

	public Integer getCreated() {
		return created;
	}

	public Long getOperationId() {
		return operationId;
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

	@Override
	public int compareTo(Order another) {
		return -1 * super.compareTo(another);
	}

}
