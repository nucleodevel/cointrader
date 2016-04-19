/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.blinktrade.util;

import java.io.Serializable;
import java.util.Date;

import net.blinktrade.common.exception.BlinktradeException;

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
	public TimestampInterval(Date fromDate) throws BlinktradeException {
		if (fromDate == null) {
			throw new BlinktradeException("Date cannot be null");
		}
		this.fromTimestamp = fromDate.getTime();
	}
	
	/**
	 * Creates an interval with timestamps.
	 * @param fromTimestamp Initial date
	 * @param toTimestamp Final date
	 */
	public TimestampInterval(long fromTimestamp, long toTimestamp) throws BlinktradeException {
		if ((fromTimestamp < 0L) || (toTimestamp < 0L)) {
			throw new BlinktradeException("Values must be greater than zero.");
		}
		
		if (fromTimestamp > toTimestamp) {
			throw new BlinktradeException("Initial timestamp must be before final timestamp");
		}

		this.fromTimestamp = fromTimestamp;
		this.toTimestamp = toTimestamp;
	}

	/**
	 * Creates an interval with dates.
	 * @param fromDate Initial date
	 * @param toDate Final date
	 */
	public TimestampInterval(Date fromDate, Date toDate) throws BlinktradeException {
		if (fromDate == null || toDate == null) {
			throw new BlinktradeException("Date cannot be null");
		}

		this.fromTimestamp = fromDate.getTime();
		this.toTimestamp = toDate.getTime();

		if (fromDate.after(toDate)) {
			throw new BlinktradeException("Initial date must be before final date");
		}
	}

	public Long getFromTimestamp() {
		return fromTimestamp;
	}
	
	public Long getToTimestamp() {
		return toTimestamp;
	}
	
}
