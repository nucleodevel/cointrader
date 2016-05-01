/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import net.mercadobitcoin.util.JsonHashMap;
import net.trader.exception.ApiProviderException;

/**
 * Withdrawal information.
 */
public class MbWithdrawal implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long withdrawalId;
	private Integer status;
	private String statusDescrition;
	private String coin;
	private String currency;
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
	public MbWithdrawal(String coin, String currency) {
		
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

	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
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
				+ status + ", statusDescrition=" + statusDescrition + ", coin="
				+ coin + ", currency=" + currency + ", transaction=" + transaction 
				+ ", address=" + address + ", volume=" + volume + ", created=" 
				+ created + ", updated=" + updated + "]";
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
			
			if (coin != null && currency != null)
				params.put("pair", coin.toLowerCase() + "_" + currency.toLowerCase());
			if (withdrawalId != null)
				params.put("withdrawal_id", withdrawalId);
			if (status != null)
				params.put("status", status);
			if (statusDescrition != null)
				params.put("status_descritition", statusDescrition);
			if (transaction != null)
				params.put("transaction", transaction);
			if (address != null)
				params.put("address", address);
			if (volume != null)
				params.put("volume", volume);
			params.put("created", created);
			params.put("updated", updated);
			
			hashMap.putAll(params);
		} catch (Throwable e) {
			throw new ApiProviderException("Internal error: Unable to transform the parameters in a request.");
		}
		return hashMap;
	}

}