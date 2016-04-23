package net.trader.robot;

import java.math.BigDecimal;
import java.util.List;

import net.trader.beans.Order;
import net.trader.exception.ApiProviderException;

public abstract class RobotReport {
	
	protected static long numOfConsideredOrdersForLastRelevantSellPrice = 5;
	
	private UserConfiguration userConfiguration;
	private String currency;
	private String coin;
	
	protected List<Order> activeBuyOrders;
	protected List<Order> activeSellOrders;
	
	protected Order currentTopBuy;
	protected Order currentTopSell;

	protected List<Order> myActiveBuyOrders;
	protected List<Order> myActiveSellOrders;
	
	protected List<Order> activeOrders;
	protected List<Order> completedOrders;
	
	protected Order lastBuy;
	protected Order lastSell;
	
	protected BigDecimal lastRelevantBuyPrice;
	protected BigDecimal lastRelevantSellPrice;
	
	public RobotReport(UserConfiguration userConfiguration, String currency, String coin) {
		this.userConfiguration = userConfiguration;
		this.currency = currency;
		this.coin = coin;
	}
	
	public UserConfiguration getUserConfiguration() {
		return userConfiguration;
	}

	public void setUserConfiguration(UserConfiguration userConfiguration) {
		this.userConfiguration = userConfiguration;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}
	
	public abstract List<Order> getActiveBuyOrders() throws ApiProviderException;

	public abstract List<Order> getActiveSellOrders() throws ApiProviderException;
	
	public abstract Order getCurrentTopBuy() throws ApiProviderException;
	
	public abstract Order getCurrentTopSell() throws ApiProviderException;
	
	public abstract List<Order> getMyCompletedOrders() throws ApiProviderException;
	
	public abstract List<Order> getMyActiveOrders() throws ApiProviderException;
	
	public abstract List<Order> getMyActiveBuyOrders() throws ApiProviderException;
	
	public abstract List<Order> getMyActiveSellOrders() throws ApiProviderException;
	
	public abstract BigDecimal getLastRelevantBuyPrice() throws ApiProviderException;
	
	public abstract BigDecimal getLastRelevantSellPrice() throws ApiProviderException;

	public abstract void cancelOrder(Order order) throws ApiProviderException;

	public abstract void createBuyOrder(BigDecimal currency, BigDecimal coin) throws ApiProviderException;

	public abstract void createSellOrder(BigDecimal currency, BigDecimal coin) throws ApiProviderException;

}
