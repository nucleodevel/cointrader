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

	private String myApiKey;
	private String myApiSecret;
	private BlinktradeBroker broker;
	
	public TemplateOfBlinktradeUserInformation() {
		myApiKey = "";
		myApiSecret = "";
		broker = BlinktradeBroker.FOXBIT;
	}

	public String getMyApiKey() {
		return myApiKey;
	}

	public String getMyApiSecret() {
		return myApiSecret;
	}

	public BlinktradeBroker getBroker() {
		return broker;
	}
	
}
