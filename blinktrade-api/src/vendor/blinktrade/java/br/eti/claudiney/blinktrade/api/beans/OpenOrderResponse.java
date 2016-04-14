package br.eti.claudiney.blinktrade.api.beans;

import java.util.List;

public class OpenOrderResponse {
	
	private int Status;
	private String Description;
	private List<OpenOrderEntry> Responses;
	
	public int getStatus() {
		return Status;
	}
	
	public String getDescription() {
		return Description;
	}
	
	public List<OpenOrderEntry> getResponses() {
		return Responses;
	}
	
	public OpenOrderEntry getResponse() {
		
		List<OpenOrderEntry> l = getResponses();
		if(l.size() > 0) {
			return l.get(0);
		}
		
		return null;
		
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append('{');
		sb.append("Status=").append(getStatus());
		sb.append(",Description=").append(getDescription());
		sb.append(",Responses=").append(getResponses());
		sb.append('}');
		return sb.toString();
	}

}
