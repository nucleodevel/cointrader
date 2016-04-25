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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mercadobitcoin.util.JsonHashMap;
import net.trader.beans.EnumValue;
import net.trader.beans.Order;
import net.trader.beans.OrderSide;
import net.trader.exception.ApiProviderException;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

/**
 * Order information.
 */
public class MbOrder extends Order implements Serializable {

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
		
		public static CoinPair getSymbolById(String value) {
			for(CoinPair cp : values()) {
				if(cp.getValue().equals(value)) {
					return cp;
				}
			}
			return null;
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
	private Long orderId;
	private String status;
	
	private List<MbOperation> operations;
	
	private Boolean flagSmall = false;

	
	/**
	 * Constructor. Request a new Order with the specified parameters.
	 * @param pair The pair of coins of to be exchanged.
	 * @param side Define if it is a 'buy' or 'sell' order.
	 * @param coinAmount The amount to be exchanged.
	 * @param price The price the exchange should be dealt.
	 */
	public MbOrder(CoinPair pair, OrderSide side, BigDecimal coinAmount, BigDecimal currencyPrice) {
		this.currencyPrice = currencyPrice;
		this.coinAmount = coinAmount;
		this.pair = pair;
		this.side = side;
		
		this.flagSmall = true;
	}

	/**
	 * Constructor. Response from the Trade API, sent to the User.
	 */
	public MbOrder(Long orderId, JsonObject jsonObject) {
		this.orderId = orderId;
		this.pair = CoinPair.valueOf(jsonObject.get("pair").asString().toUpperCase());
		this.side = OrderSide.valueOf(jsonObject.get("type").asString().toUpperCase());
		this.coinAmount = new BigDecimal(jsonObject.get("volume").asString());
		this.currencyPrice = new BigDecimal(jsonObject.get("price").asString());
		this.status = jsonObject.get("status").asString();
		
		this.operations = new ArrayList<MbOperation>();
		for (String operationId: jsonObject.get("operations").asObject().names()) {
			if (operationId.matches("-?\\d+(\\.\\d+)?")) {
				operations.add(new MbOperation(
								Long.valueOf(operationId),
								jsonObject.get("operations").asObject().get(operationId).asObject() ));
			}
		}
		
		long created = Integer.valueOf(jsonObject.get("created").asString());
		this.creationDate = Calendar.getInstance();
		this.creationDate.setTimeInMillis((long)created * 1000);
	}
	
	/**
	 * Constructor. Response from the API, used by Orderbook.
	 */
	public MbOrder(JsonArray jsonArray, CoinPair pair, OrderSide side) {
		this.currencyPrice = new BigDecimal(jsonArray.get(0).toString());
		this.coinAmount = new BigDecimal(jsonArray.get(1).toString());
		this.pair = pair;
		this.side = side;
		
		this.flagSmall = true;
	}

	public CoinPair getPair() {
		return pair;
	}

	public Long getOrderId() {
		return orderId;
	}

	public String getStatus() {
		return status;
	}

	public List<MbOperation> getOperations() {
		return operations;
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
			return "\nOrder [pair=" + pair + ", side=" + side + ", coinAmount=" + coinAmount
					+ ", price=" + currencyPrice + "]";
		} else {
			return "Order [pair=" + pair + ", side=" + side + ", coinAmount=" + coinAmount
					+ ", price=" + currencyPrice + ", orderId=" + orderId + ", status="
					+ status + ", created=" + creationDate.getTime() + ", operations="
					+ operations + "]";
		}
	}

	@Override
	public String toDisplayString() {
		return toString();
	}

	public int compareTo(Order another) {
		return -1 * super.compareTo(another);
	}

	/**
	 * Get the Parameters of the Object and return them as a list with the name and the value of each parameter.
	 * 
	 * @throws ApiProviderException Generic exception to point any error with the execution.
	 */
	public JsonHashMap toParams() throws ApiProviderException {
		JsonHashMap hashMap = new JsonHashMap();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			
			if (pair != null)
				params.put("pair", pair.getValue());
			if (side != null)
				params.put("type", side == OrderSide.BUY? "buy": (side == OrderSide.SELL? "sell": null));
			if (coinAmount != null)
				params.put("volume", coinAmount);
			if (currencyPrice != null)
				params.put("price", currencyPrice);
			if (orderId != null)
				params.put("order_id", orderId);
			if (status != null)
				params.put("status", status);
			if (creationDate != null)
				params.put("created", creationDate.getTime());
			
			hashMap.putAll(params);
		} catch (Throwable e) {
			throw new ApiProviderException("Internal error: Unable to transform the parameters in a request.");
		}
		return hashMap;
	}
	
}