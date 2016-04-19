/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.blinktrade.tradeapi.to;

import java.math.BigDecimal;

import net.blinktrade.tradeapi.to.Order.CoinPair;

import com.eclipsesource.json.JsonObject;

/**
 * Withdrawal information.
 */
public class Withdrawal extends TapiBase {

	private static final long serialVersionUID = 1L;
	
	private Long withdrawalId;
	private Integer status;
	private String statusDescrition;
	private CoinPair pair;
	private String transaction;
	private String address;
	private BigDecimal volume;
	private long created;
	private long updated;
	
	/**
	 * Constructor based on JSON response.
	 * 
	 * @param jsonObject Trade API JSON response
	 */
	public Withdrawal(JsonObject jsonObject, CoinPair pair) {
		JsonObject jsonWithdrawal = jsonObject.get("withdrawal").asObject();
		this.withdrawalId = Long.valueOf(jsonWithdrawal.getString("id", "0"));
		this.volume = new BigDecimal(jsonWithdrawal.getString("volume", "0"));
		this.status = Long.valueOf(jsonWithdrawal.getString("status", "0")).intValue();
		this.statusDescrition = jsonWithdrawal.getString("status_description", "");
		this.transaction = jsonWithdrawal.getString("transaction", "");
		this.address = jsonWithdrawal.getString("bitcoin_address", "");
		this.created = Long.valueOf(jsonWithdrawal.getString("created_timestamp", "0"));
		this.updated = Long.valueOf(jsonWithdrawal.getString("updated_timestamp", "0"));
		this.pair = pair;
	}
	
	public Long getWithdrawalId() {
		return withdrawalId;
	}

	public void setWithdrawalId(Long withdrawalId) {
		this.withdrawalId = withdrawalId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getStatusDescrition() {
		return statusDescrition;
	}

	public void setStatusDescrition(String statusDescrition) {
		this.statusDescrition = statusDescrition;
	}

	public CoinPair getPair() {
		return pair;
	}

	public void setPair(CoinPair pair) {
		this.pair = pair;
	}

	public String getTransaction() {
		return transaction;
	}

	public void setTransaction(String transaction) {
		this.transaction = transaction;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public BigDecimal getVolume() {
		return volume;
	}

	public void setVolume(BigDecimal volume) {
		this.volume = volume;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public long getUpdated() {
		return updated;
	}

	public void setUpdated(long updated) {
		this.updated = updated;
	}

	@Override
	public String toString() {
		return "Withdrawal [withdrawalId=" + withdrawalId + ", status="
				+ status + ", statusDescrition=" + statusDescrition + ", pair="
				+ pair + ", transaction=" + transaction + ", address="
				+ address + ", volume=" + volume + ", created=" + created
				+ ", updated=" + updated + "]";
	}

}