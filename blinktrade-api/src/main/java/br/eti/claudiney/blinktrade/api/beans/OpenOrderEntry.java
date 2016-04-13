package br.eti.claudiney.blinktrade.api.beans;

import java.io.Serializable;

@SuppressWarnings("serial")
public class OpenOrderEntry implements Serializable {
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append('{');
		//sb.append("OrdListGrp=").append(getOpenOrders());
		sb.append('}');
		return sb.toString();
	}

}
