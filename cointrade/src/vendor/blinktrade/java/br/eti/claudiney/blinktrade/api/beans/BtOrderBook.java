package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import net.trader.beans.Order;
import net.trader.beans.OrderBook;

@SuppressWarnings("serial")
public class BtOrderBook extends OrderBook implements Serializable {
	
	private String pair;
	private List<List<BigDecimal>> bids;
	private List<List<BigDecimal>> asks;
	
	public String getPair() {
		return pair;
	}
	
	@Override
	public List<Order> getBids() {
		
		List<Order> _bids = new ArrayList<Order>();
		
		if(bids == null) return _bids;

		for( List<BigDecimal> bid: bids ) {
			Order b = new BtSimpleOrder(
					bid.get(0),
					bid.get(1))
			.setClientID(bid.get(2).toBigInteger().toString());
			_bids.add(b);
		}
		
		return _bids;
		
	}
	
	@Override
	public List<Order> getAsks() {
		
		List<Order> _asks = new ArrayList<Order>();
		
		if(asks == null) return _asks;

		for( List<BigDecimal> ask: asks ) {
			Order a = new BtSimpleOrder(
					ask.get(0),
					ask.get(1))
					.setClientID(ask.get(2).toBigInteger().toString());
			_asks.add(a);
		}
		
		return _asks;
		
	}
	
	public Order getBetterBid() {
		List<Order> bids = getBids();
		if(bids.size() == 0) return null;
		return (Order) bids.get(0);
	}
	
	public Order getBetterAsk() {
		List<Order> asks = getAsks();
		if(asks.size() == 0) return null;
		return (Order) asks.get(0);
	}

}
