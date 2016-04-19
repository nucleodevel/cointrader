/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.blinktrade.tradeapi.to;

import java.io.Serializable;

import net.blinktrade.common.exception.BlinktradeException;
import net.blinktrade.util.JsonHashMap;
import net.blinktrade.util.ReflectionUtils;

public abstract class TapiBase implements Serializable {

	private static final long serialVersionUID = 6302408184251869680L;

	/**
	 * Get the Parameters of the Object and return them as a list with the name and the value of each parameter.
	 * 
	 * @throws BlinktradeException Generic exception to point any error with the execution.
	 */
	public JsonHashMap toParams() throws BlinktradeException {
		JsonHashMap params = new JsonHashMap();
		try {
			params.putAll(ReflectionUtils.getParameters(this));
		} catch (Throwable e) {
			throw new BlinktradeException("Internal error: Unable to transform the parameters in a request.");
		}
		return params;
	}
	
}