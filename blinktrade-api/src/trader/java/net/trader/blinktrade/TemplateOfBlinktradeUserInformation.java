/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.trader.blinktrade;

import br.eti.claudiney.blinktrade.enums.BlinktradeBroker;

/**
 * User Trade API info
 * 
 * @see https://www.mercadobitcoin.net/trade-api/
 * 
 *  Edit para UserInformation
 */
public class TemplateOfBlinktradeUserInformation {

	private String myTapiKey;
	private String myTapiCode;
	private BlinktradeBroker broker;
	
	public TemplateOfBlinktradeUserInformation() {
		myTapiKey = "";
		myTapiCode = "";
		broker = BlinktradeBroker.FOXBIT;
	}

	public String getMyTapiKey() {
		return myTapiKey;
	}

	public String getMyTapiCode() {
		return myTapiCode;
	}

	public BlinktradeBroker getBroker() {
		return broker;
	}
	
}
