/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.trader.cointrader.exception;

/**
 * Mercado Bitocin generic exception type used in internal errors.
 */
public class ParamValueErrorException extends Exception {

	private static final long serialVersionUID = 3299761335363609520L;
	
	private String paramLabel;

	public ParamValueErrorException(String paramLabel) {
		super();
		this.paramLabel = paramLabel;
	}

	public String getParamLabel() {
		return paramLabel;
	}

}
