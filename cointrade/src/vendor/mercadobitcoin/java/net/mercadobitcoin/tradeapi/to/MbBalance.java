/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.io.Serializable;
import java.math.BigDecimal;

import net.trader.beans.Balance;

/**
 * User's information of funds, opened orders and server date/time.
 */
public class MbBalance extends Balance implements Serializable {

	private static final long serialVersionUID = 6922034267613306463L;

	private Integer serverTime;
	private Integer openOrders;
	private Funds funds;

	public MbBalance(String coin, String currency) {
		super(coin, currency);
		this.coinLocked = new BigDecimal(0);
		this.currencyLocked = new BigDecimal(0);
	}
	
	@Override
	public BigDecimal getCoinAmount() {
		BigDecimal coinAmount;
		if (getCoin().equals("BTC"))
			coinAmount = getFunds().getBtcWithOpenOrders();
		else if (getCoin().equals("LTC"))
			coinAmount = getFunds().getLtcWithOpenOrders();
		else
			coinAmount = null;
		return coinAmount;
	}
	
	@Override
	public BigDecimal getCurrencyAmount() {
		BigDecimal currencyAmount;
		if (getCurrency().equals("BRL"))
			currencyAmount = getFunds().getBrlWithOpenOrders();
		else
			currencyAmount = null;
		return currencyAmount;
	}

	public Integer getServerTime() {
		return serverTime;
	}

	public void setServerTime(Integer serverTime) {
		this.serverTime = serverTime;
	}

	public Integer getOpenOrders() {
		return openOrders;
	}

	public void setOpenOrders(Integer openOrders) {
		this.openOrders = openOrders;
	}

	public Funds getFunds() {
		return funds;
	}

	public void setFunds(Funds funds) {
		this.funds = funds;
	}

}
