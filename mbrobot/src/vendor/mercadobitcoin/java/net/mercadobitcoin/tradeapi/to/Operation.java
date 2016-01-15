/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;

import net.mercadobitcoin.tradeapi.to.Order.OrderType;

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
public class Operation implements Serializable, Comparable<Operation> {

	private static final long serialVersionUID = -3345636873296069825L;

	private Long operationId;
	private BigDecimal volume;
	private BigDecimal price;
	private BigDecimal rate;
	private Integer created;
	private Calendar createdDate;
	private OrderType type;

	/**
	 * Constructor based on JSON response.
	 * 
	 * @param jsonObject Trade API JSON response
	 */
	public Operation(JsonObject jsonObject) {
		this.created = Integer.valueOf(jsonObject.get("date").toString());
		this.price = new BigDecimal(jsonObject.get("price").toString());
		this.volume = new BigDecimal(jsonObject.get("amount").toString());
		this.operationId = jsonObject.get("tid").asLong();
		this.type = OrderType.valueOf(jsonObject.get("type").asString()
				.toUpperCase());

		this.rate = null;
		
		this.createdDate = Calendar.getInstance();
		this.createdDate.setTimeInMillis((long)created * 1000);
	}

	/**
	 * Constructor based on JSON response.
	 * 
	 * @param operationId Operation Identifier
	 * @param jsonObject Trade API JSON response
	 */
	public Operation(Long operationId, JsonObject jsonObject) {
		this.operationId = operationId;
		this.volume = new BigDecimal(jsonObject.get("volume").asString());
		this.price = new BigDecimal(jsonObject.get("price").asString());
		this.rate = new BigDecimal(jsonObject.get("rate").asString());
		this.created = Integer.valueOf(jsonObject.get("created").asString());

		this.type = null;
		
		this.createdDate = Calendar.getInstance();
		this.createdDate.setTimeInMillis((long)created * 1000);
	}

	public Integer getDate() {
		return created;
	}

	public BigDecimal getAmount() {
		return volume;
	}

	public Long getTid() {
		return operationId;
	}

	public OrderType getType() {
		return type;
	}

	public void setType(OrderType type) {
		this.type = type;
	}

	public BigDecimal getVolume() {
		return volume;
	}

	public BigDecimal getPrice() {
		return price;
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
		if (this.type != null) {
			return "\nOperation [date=" + createdDate.getTime() + ", price=" + price
					+ ", amount=" + volume + ", tid=" + operationId + ", type="
					+ type + "]";
		} else {
			return "Operation [operationId=" + operationId + ", volume="
					+ volume + ", price=" + price + ", rate=" + rate
					+ ", created=" + createdDate.getTime() + "]";
		}
	}

	public int compareTo(Operation another) {
		return -1 * this.created.compareTo(another.created);
	}

}
