/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.io.Serializable;
import java.math.BigDecimal;

public class Funds implements Serializable {

	private static final long serialVersionUID = -6033277213248384326L;

	private BigDecimal brl;
	private BigDecimal btc;
	private BigDecimal ltc;
	private BigDecimal brlWithOpenOrders;
	private BigDecimal btcWithOpenOrders;
	private BigDecimal ltcWithOpenOrders;

	/**
	 * Constructor based on JSON response.
	 * 
	 * @param jsonObject Trade API JSON response
	 */
	public Funds() {
		
	}

	public BigDecimal getBrl() {
		return brl;
	}

	public void setBrl(BigDecimal brl) {
		this.brl = brl;
	}

	public BigDecimal getBtc() {
		return btc;
	}

	public void setBtc(BigDecimal btc) {
		this.btc = btc;
	}

	public BigDecimal getLtc() {
		return ltc;
	}

	public void setLtc(BigDecimal ltc) {
		this.ltc = ltc;
	}

	public BigDecimal getBrlWithOpenOrders() {
		return brlWithOpenOrders;
	}

	public void setBrlWithOpenOrders(BigDecimal brlWithOpenOrders) {
		this.brlWithOpenOrders = brlWithOpenOrders;
	}

	public BigDecimal getBtcWithOpenOrders() {
		return btcWithOpenOrders;
	}

	public void setBtcWithOpenOrders(BigDecimal btcWithOpenOrders) {
		this.btcWithOpenOrders = btcWithOpenOrders;
	}

	public BigDecimal getLtcWithOpenOrders() {
		return ltcWithOpenOrders;
	}

	public void setLtcWithOpenOrders(BigDecimal ltcWithOpenOrders) {
		this.ltcWithOpenOrders = ltcWithOpenOrders;
	}

	@Override
	public String toString() {
		return "Funds [BRL=" + brl + ", BTC=" + btc + ", LTC=" + ltc
				 		+ ", BRL_ALL=" + brlWithOpenOrders + ", BTC_ALL=" + btcWithOpenOrders
				 		+ ", LTC_ALL=" + ltcWithOpenOrders + "]";
	}

}
