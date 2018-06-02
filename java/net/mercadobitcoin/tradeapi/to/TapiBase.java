/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.tradeapi.to;

import java.io.Serializable;

import net.mercadobitcoin.common.exception.MercadoBitcoinException;
import net.mercadobitcoin.util.JsonHashMap;
import net.mercadobitcoin.util.ReflectionUtils;

public abstract class TapiBase implements Serializable {

	private static final long serialVersionUID = 6302408184251869680L;

	/**
	 * Get the Parameters of the Object and return them as a list with the name and the value of each parameter.
	 * 
	 * @throws MercadoBitcoinException Generic exception to point any error with the execution.
	 */
	public JsonHashMap toParams() throws MercadoBitcoinException {
		JsonHashMap params = new JsonHashMap();
		try {
			params.putAll(ReflectionUtils.getParameters(this));
		} catch (Throwable e) {
			throw new MercadoBitcoinException("Internal error: Unable to transform the parameters in a request.");
		}
		return params;
	}
	
}