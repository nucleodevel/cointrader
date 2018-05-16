package br.net.dallan.cointrader.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Funções utilitárias
 */
public class Utils {
	
	private Utils() {
	}
	
	public static BigInteger getBigInteger(Object o) {
		
		if(o instanceof Number) {
			return new BigInteger(new Integer(((Number) o).intValue()).toString());
		}
		
		return new BigDecimal(o.toString()).toBigInteger();
		
	}
	
	public static BigDecimal getBigDecimal(Object o) {
		
		if(o instanceof Number) {
			return new BigDecimal( ((Number) o).doubleValue() ) ;
		}
		
		return new BigDecimal(o.toString());
		
	}
	
	public static String getString(Object o) {
		if(o == null) return null;
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
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
		
		return null;
		
	}

}
