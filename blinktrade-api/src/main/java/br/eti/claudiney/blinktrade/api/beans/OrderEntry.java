package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import br.eti.claudiney.blinktrade.utils.Utils;

@SuppressWarnings("serial")
public class OrderEntry implements Serializable {
	
	private JsonArray ordListGrp;
	private List<OpenOrder> openOrders;
	
	
	public OrderEntry(JsonArray ordListGrp) {
		super();
		this.ordListGrp = ordListGrp;
	}

	public List<OpenOrder> getOpenOrders() {
		if (openOrders == null) {
		
			if(ordListGrp != null) {
				for (JsonElement o: ordListGrp) {
					if (o != null) {
						OpenOrder oo = new OpenOrder();
						openOrders.add(oo);
						JsonArray objArray = o.getAsJsonArray();
						oo.setClientCustomOrderID(objArray.get(0).getAsBigInteger());
						oo.setOrderID(objArray.get(1).getAsString());
						oo.setCumQty(objArray.get(2).getAsBigDecimal());
						oo.setOrdStatus(objArray.get(3).getAsString());
						oo.setLeavesQty(objArray.get(4).getAsBigDecimal());
						oo.setCxlQty(objArray.get(5).getAsBigDecimal());
						oo.setAvgPx(objArray.get(6).getAsBigDecimal());
						oo.setSymbol(objArray.get(7).getAsString());
						oo.setSide(objArray.get(8).getAsString());
						oo.setOrdType(objArray.get(9).getAsString());
						oo.setOrderQty(objArray.get(10).getAsBigDecimal());
						
						BlinktradeCurrency c = BlinktradeCurrency.getCurrencyBySimbol(oo.getSymbol());
						oo.setPrice(objArray.get(11).getAsBigDecimal().divide(
								c.getRate(),
								c.getRateSize(), RoundingMode.DOWN) );
						oo.setOrderDate( Utils.getCalendar(objArray.get(12).getAsString()));
						oo.setVolume(objArray.get(13).getAsBigDecimal());
						oo.setTimeInForce(objArray.get(14).getAsString());
					}
				}
			}
		}
		
		return openOrders;
		
	}
	
	public OpenOrder getFirstResult() {
		
		List<OpenOrder> l = getOpenOrders();
		if(l.size() > 0) {
			return l.get(0);
		}
		
		return null;
		
	}
	
	public OpenOrder getLastBuy() {
		for (OpenOrder order: getOpenOrders())
			if (order.getSide().equals("1"))
				return order;
		
		return null;
	}
	
	public OpenOrder getLastSell() {
		for (OpenOrder order: getOpenOrders())
			if (order.getSide().equals("2"))
				return order;
		
		return null;
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append('{');
		sb.append("OrdListGrp=").append(getOpenOrders());
		sb.append('}');
		return sb.toString();
	}

}
