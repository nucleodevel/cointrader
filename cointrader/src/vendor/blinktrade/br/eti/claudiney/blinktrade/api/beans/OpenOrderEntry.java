package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import br.eti.claudiney.blinktrade.utils.Utils;

@SuppressWarnings("serial")
public class OpenOrderEntry implements Serializable {
	
	private List<Object[]> OrdListGrp;
	
	public List<OpenOrder> getOpenOrders() {
		
		List<OpenOrder> l = new ArrayList<OpenOrder>();
		
		if(OrdListGrp!=null) {
			for(Object[] o: OrdListGrp) {
				if(o != null) {
					OpenOrder oo = new OpenOrder();
					l.add(oo);
					for(int i = 0; i < o.length; ++i) {
						Object k = o[i];
						if(i == 0) {
							oo.setClientCustomOrderID( Utils.getBigInteger(k) );
							continue;
						}
						if(i == 1) {
							oo.setOrderID( Utils.getBigInteger(k).toString() );
							continue;
						}
						if(i == 2) {
							oo.setCumQty( Utils.getBigDecimal(k) );
							continue;
						}
						if(i == 3) {
							oo.setOrdStatus( Utils.getString(k) );
							continue;
						}
						if(i == 4) {
							oo.setLeavesQty( Utils.getBigDecimal(k) );
							continue;
						}
						if(i == 5) {
							oo.setCxlQty( Utils.getBigDecimal(k) );
							continue;
						}
						if(i == 6) {
							oo.setAvgPx( Utils.getBigDecimal(k) );
							continue;
						}
						if(i == 7) {
							oo.setSymbol( Utils.getString(k) );
							continue;
						}
						if(i == 8) {
							oo.setSide( Utils.getString(k) );
							continue;
						}
						if(i == 9) {
							oo.setOrdType( Utils.getString(k) );
							continue;
						}
						if(i == 10) {
							oo.setOrderQty( Utils.getBigDecimal(k) );
							continue;
						}
						if(i == 11) {
							BlinktradeCurrency c = BlinktradeCurrency.getCurrencyBySimbol(oo.getSymbol());
							oo.setPrice( Utils.getBigDecimal(k).divide(
									c.getRate(),
									c.getRateSize(), RoundingMode.DOWN) );
							continue;
						}
						if(i == 12) {
							oo.setOrderDate( Utils.getCalendar(k) );
							continue;
						}
						if(i == 13) {
							oo.setVolume( Utils.getBigDecimal(k) );
							continue;
						}
						if(i == 14) {
							oo.setTimeInForce( Utils.getString(k) );
							continue;
						}
						
					}
				}
			}
		}
		
		
		return l;
		
	}
	
	public OpenOrder getFirstResult() {
		
		List<OpenOrder> l = getOpenOrders();
		if(l.size() > 0) {
			return l.get(0);
		}
		
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
