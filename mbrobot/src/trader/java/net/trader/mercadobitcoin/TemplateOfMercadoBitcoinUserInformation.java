/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.trader.mercadobitcoin;

/**
 * User Trade API info
 * 
 * @see https://www.mercadobitcoin.net/trade-api/
 * 
 *  Edit para UserInformation
 */
public class TemplateOfMercadoBitcoinUserInformation {

	public String myTapiKey;
	public String myTapiCode;
	
	public TemplateOfMercadoBitcoinUserInformation() {
		myTapiKey = "";
		myTapiCode = "";
	}

	public String getMyTapiKey() {
		return myTapiKey;
	}

	public String getMyTapiCode() {
		return myTapiCode;
	}
	
}
