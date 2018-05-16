/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.mercadobitcoin.util;

import java.io.Serializable;
import java.util.Date;

import net.mercadobitcoin.common.exception.MercadoBitcoinException;

public class TimestampInterval implements Serializable {
	
	private static final long serialVersionUID = 1L;
	 
	private Long fromTimestamp;
	private Long toTimestamp;
	
	public TimestampInterval(long fromTimestamp) {
		this.fromTimestamp = fromTimestamp;
	}
	
	/**
	 * Creates an interval with only initial date.
	 * @param fromDate Initial date
	 */
	public TimestampInterval(Date fromDate) throws MercadoBitcoinException {
		if (fromDate == null) {
			throw new MercadoBitcoinException("Date cannot be null");
		}
		this.fromTimestamp = fromDate.getTime();
	}
	
	/**
	 * Creates an interval with timestamps.
	 * @param fromTimestamp Initial date
	 * @param toTimestamp Final date
	 */
	public TimestampInterval(long fromTimestamp, long toTimestamp) throws MercadoBitcoinException {
		if ((fromTimestamp < 0L) || (toTimestamp < 0L)) {
			throw new MercadoBitcoinException("Values must be greater than zero.");
		}
		
		if (fromTimestamp > toTimestamp) {
			throw new MercadoBitcoinException("Initial timestamp must be before final timestamp");
		}

		this.fromTimestamp = fromTimestamp;
		this.toTimestamp = toTimestamp;
	}

	/**
	 * Creates an interval with dates.
	 * @param fromDate Initial date
	 * @param toDate Final date
	 */
	public TimestampInterval(Date fromDate, Date toDate) throws MercadoBitcoinException {
		if (fromDate == null || toDate == null) {
			throw new MercadoBitcoinException("Date cannot be null");
		}

		this.fromTimestamp = fromDate.getTime();
		this.toTimestamp = toDate.getTime();

		if (fromDate.after(toDate)) {
			throw new MercadoBitcoinException("Initial date must be before final date");
		}
	}

	public Long getFromTimestamp() {
		return fromTimestamp;
	}
	
	public Long getToTimestamp() {
		return toTimestamp;
	}
	
}
