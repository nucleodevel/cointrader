package org.nucleodevel.cointrader.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Funções utilitárias
 */
public class Utils {

	public static String getString(Object o) {
		if (o == null)
			return null;
		return o.toString();
	}

	public static Calendar getCalendar(Object source) {

		String d = getString(source);
		Calendar c = Calendar.getInstance();
		TimeZone t = TimeZone.getTimeZone("Etc/Universal");
		c.setTimeZone(t);
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		s.setTimeZone(t);

		try {
			c.setTime(s.parse(d));
			return c;
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		return null;

	}

	public static String toISO8601UTC(Calendar calendar) {

		Date date = calendar.getTime();

		TimeZone tz = TimeZone.getTimeZone("UTC");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(tz);
		return df.format(date);
	}

	public static String toISO8601UTCWithoutMillisAndFinalZ(Calendar calendar) {

		Date date = calendar.getTime();

		TimeZone tz = TimeZone.getTimeZone("UTC");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(tz);
		return df.format(date);
	}

	public static Calendar fromISO8601UTC(String dateStr) {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(tz);

		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(df.parse(dateStr));

			return calendar;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static DecimalFormat getCustomDecimalFormat(int maximumFractionDigits) {

		DecimalFormat decFmt = new DecimalFormat();
		decFmt.setMaximumFractionDigits(maximumFractionDigits);

		DecimalFormatSymbols symbols = decFmt.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decFmt.setDecimalFormatSymbols(symbols);

		return decFmt;
	}

	public static DecimalFormat getDefaultDecimalFormat() {
		return getCustomDecimalFormat(8);
	}

}
