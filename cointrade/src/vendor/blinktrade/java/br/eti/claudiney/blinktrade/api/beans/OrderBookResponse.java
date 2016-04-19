package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class OrderBookResponse implements Serializable {
	
	private String pair;
	private List<List<BigDecimal>> bids;
	private List<List<BigDecimal>> asks;
	
	public String getPair() {
		return pair;
	}
	
	public List<Bid> getBids() {
		
		List<Bid> _bids = new ArrayList<Bid>();
		
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
	
	public List<Ask> getAsks() {
		
		List<Ask> _asks = new ArrayList<Ask>();
		
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
		List<Bid> bids = getBids();
		if(bids.size() == 0) return null;
		return bids.get(0);
	}
	
	public Bid getSecondBid() {
		List<Bid> bids = getBids();
		if(bids.size() <= 1) return null;
		return bids.get(1);
	}
	
	public Ask getBetterAsk() {
		List<Ask> asks = getAsks();
		if(asks.size() == 0) return null;
		return asks.get(0);
	}

}
