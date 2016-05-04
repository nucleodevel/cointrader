/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.trader.beans;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Withdrawal information.
 */
public class Withdrawal implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private CoinCurrencyPair coinCurrencyPair;
	private Long withdrawalId;
	private Integer status;
	private String statusDescrition;
	private String transaction;
	private String address;
	private BigDecimal volume;
	private long created;
	private long updated;
	
	public Withdrawal(Coin coin, Currency currency) {
		this.coinCurrencyPair = new CoinCurrencyPair(coin, currency);
	}

	public CoinCurrencyPair getCoinCurrencyPair() {
		return coinCurrencyPair;
	}

	public void setCoinCurrencyPair(CoinCurrencyPair coinCurrencyPair) {
		this.coinCurrencyPair = coinCurrencyPair;
	}
	
	public Coin getCoin() {
		return coinCurrencyPair.getCoin();
	}

	public void setCoin(Coin coin) {
		coinCurrencyPair.setCoin(coin);
	}

	public Currency getCurrency() {
		return coinCurrencyPair.getCurrency();
	}

	public void setCurrency(Currency currency) {
		coinCurrencyPair.setCurrency(currency);
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
				+ getCoin() + ", currency=" + getCurrency() + ", transaction=" + transaction 
				+ ", address=" + address + ", volume=" + volume + ", created=" 
				+ created + ", updated=" + updated + "]";
	}	

}