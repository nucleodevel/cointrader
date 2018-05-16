/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.blinktrade.util;

import java.util.HashMap;

public class JsonHashMap extends HashMap<String, Object> {

	private static final long serialVersionUID = -5411638158870881904L;

	/**
	 * Method to convert the Parameters of the Object to a String in json format.
	 * 
	 * @return String, in Json format, of the JsonHashMap containing the request's parameters.
	 */
	public String toJson() {
		StringBuffer json = new StringBuffer("{");
		
		int count = 0;
		
		for (String key : this.keySet()) {
			Object value = this.get(key);
			if (value instanceof String) {
				value = "\"" + value + "\"";
			}
			json.append(" \"" + key + "\"");
			json.append(": ");
			json.append(value);
			
			count++;
			if (count != this.keySet().size()) {
				json.append(",");
			}
		}
		json.append("}");
		json.deleteCharAt(1);
		return json.toString();
	}
	
	/**
	 * Method to convert the Parameters of the Object to a String in URL encoded format.
	 * 
	 * @return String, in urlEncoded format, of the JsonHashMap containing the request's parameters.
	 */
	public String toUrlEncoded() {
		StringBuffer newUrlEncoded = new StringBuffer();
		int count = 0;
		
		for (String key : this.keySet()) {
			Object value = this.get(key);
			newUrlEncoded.append(key + "=" + value);			
			
			count++;			
			if (count != this.keySet().size()) {
				newUrlEncoded.append("&");
			}
		}
		
		return newUrlEncoded.toString();
	}
	
}