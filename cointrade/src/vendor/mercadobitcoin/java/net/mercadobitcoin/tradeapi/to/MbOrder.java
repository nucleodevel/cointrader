/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.mercadobitcoin.util.EnumValue;
import net.mercadobitcoin.util.JsonHashMap;
import net.mercadobitcoin.util.ReflectionUtils;
import net.trader.beans.Order;
import net.trader.exception.ApiProviderException;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

/**
 * Order information.
 */
public class MbOrder extends Order implements Serializable, Comparable<MbOrder> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The Coin Pairs that a operation can deal with.
	 */
	public enum CoinPair implements EnumValue {
		BTC_BRL("btc_brl"),
		LTC_BRL("ltc_brl");
		private final String value;

		private CoinPair(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}
	
	/**
	 * Defines the Type of the Order (Buy or Sell).
	 */
	public enum OrderType implements EnumValue {
		BUY("buy"),
		SELL("sell");
		private final String value;

		private OrderType(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}
	
	/**
	 * Define the Status of an Order (Active, Canceled or Completed).
	 */
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
	
	private CoinPair pair;
	private OrderType type;
	private BigDecimal volume;
	private BigDecimal price;
	
	private Long orderId;
	private String status;
	private Integer created;
	private Calendar createdDate;
	private List<Operation> operations;
	
	private Boolean flagSmall = false;

	
	/**
	 * Constructor. Request a new Order with the specified parameters.
	 * @param pair The pair of coins of to be exchanged.
	 * @param type Define if it is a 'buy' or 'sell' order.
	 * @param volume The amount to be exchanged.
	 * @param price The price the exchange should be dealt.
	 */
	public MbOrder(CoinPair pair, OrderType type, BigDecimal volume, BigDecimal price) {
		this.price = price;
		this.volume = volume;
		this.pair = pair;
		this.type = type;
		
		this.flagSmall = true;
	}

	/**
	 * Constructor. Response from the Trade API, sent to the User.
	 */
	public MbOrder(Long orderId, JsonObject jsonObject) {
		this.orderId = orderId;
		this.pair = CoinPair.valueOf(jsonObject.get("pair").asString().toUpperCase());
		this.type = OrderType.valueOf(jsonObject.get("type").asString().toUpperCase());
		this.volume = new BigDecimal(jsonObject.get("volume").asString());
		this.price = new BigDecimal(jsonObject.get("price").asString());
		this.status = jsonObject.get("status").asString();
		this.created = Integer.valueOf(jsonObject.get("created").asString());
		
		this.operations = new ArrayList<Operation>();
		for (String operationId: jsonObject.get("operations").asObject().names()) {
			if (operationId.matches("-?\\d+(\\.\\d+)?")) {
				operations.add(new Operation(
								Long.valueOf(operationId),
								jsonObject.get("operations").asObject().get(operationId).asObject() ));
			}
		}
		
		this.createdDate = Calendar.getInstance();
		this.createdDate.setTimeInMillis((long)created * 1000);
	}
	
	/**
	 * Constructor. Response from the API, used by Orderbook.
	 */
	public MbOrder(JsonArray jsonArray, CoinPair pair, OrderType type) {
		this.price = new BigDecimal(jsonArray.get(0).toString());
		this.volume = new BigDecimal(jsonArray.get(1).toString());
		this.pair = pair;
		this.type = type;
		
		this.flagSmall = true;
	}
	
	public CoinPair getPair() {
		return pair;
	}

	public OrderType getType() {
		return type;
	}

	public BigDecimal getVolume() {
		return volume;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public Long getOrderId() {
		return orderId;
	}

	public String getStatus() {
		return status;
	}

	public Integer getCreated() {
		return created;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public Calendar getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Calendar createdDate) {
		this.createdDate = createdDate;
	}

	@Override
	public String toString() {
		if (this.flagSmall == true) {
			return "\nOrder [pair=" + pair + ", type=" + type + ", volume=" + volume
					+ ", price=" + price + "]";
		} else {
			return "Order [pair=" + pair + ", type=" + type + ", volume=" + volume
					+ ", price=" + price + ", orderId=" + orderId + ", status="
					+ status + ", created=" + createdDate.getTime() + ", operations="
					+ operations + "]";
		}
	}

	public int compareTo(MbOrder another) {
		return -1 * this.created.compareTo(another.created);
	}

	/**
	 * Get the Parameters of the Object and return them as a list with the name and the value of each parameter.
	 * 
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public JsonHashMap toParams() throws ApiProviderException {
		JsonHashMap params = new JsonHashMap();
		try {
			params.putAll(ReflectionUtils.getParameters(this));
		} catch (Throwable e) {
			throw new ApiProviderException("Internal error: Unable to transform the parameters in a request.");
		}
		return params;
	}
	
}