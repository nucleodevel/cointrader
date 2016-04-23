package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import net.trader.beans.Order;

@SuppressWarnings("serial")
public class OrderBookResponse implements Serializable {
	
	private String pair;
	private List<List<BigDecimal>> bids;
	private List<List<BigDecimal>> asks;
	
	public String getPair() {
		return pair;
	}
	
	public List<Order> getBids() {
		
		List<Order> _bids = new ArrayList<Order>();
		
		if(bids == null) return _bids;

		for( List<BigDecimal> bid: bids ) {
			Bid b = new Bid(
					bid.get(0),
					bid.get(1))
			.setClientID(bid.get(2).toBigInteger().toString());
			_bids.add(b);
		}
		
		return _bids;
		
	}
	
	public List<Order> getAsks() {
		
		List<Order> _asks = new ArrayList<Order>();
		
		if(asks == null) return _asks;

		for( List<BigDecimal> ask: asks ) {
			Ask a = new Ask(
					ask.get(0),
					ask.get(1))
					.setClientID(ask.get(2).toBigInteger().toString());
			_asks.add(a);
		}
		
		return _asks;
		
	}
	
	public Bid getBetterBid() {
		List<Order> bids = getBids();
		if(bids.size() == 0) return null;
		return (Bid) bids.get(0);
	}
	
	public Ask getBetterAsk() {
		List<Order> asks = getAsks();
		if(asks.size() == 0) return null;
		return (Ask) asks.get(0);
	}

}
