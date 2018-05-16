/**
 * under the MIT License (MIT)
 * Copyright (c) 2015 Mercado Bitcoin Servicos Digitais Ltda.
 * @see more details in /LICENSE.txt
 */

package net.blinktrade.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ReflectionUtils {
	
	private static boolean isGetter(Method method) {
		if (Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 0) {
			if (method.getName().matches("^get[A-Z].*") && !method.getReturnType().equals(void.class)) {
				return true;
			}
		}
		return false;
	}
	

	private static String getGetterFieldName(String value) {
		if (value != null) {
			value = value.replaceAll("get", "");
			value = Character.toLowerCase(value.charAt(0)) + value.substring(1);
			value = value.replaceAll("([A-Z])", "_$1").toLowerCase();
			return value;
		} else {
			return null;
		}
	}

	/**
	 * Get the name of the parameters of an Object, and pair them with their value.
	 * 
	 * @param obj The object to have the parameters extracted.
	 * @return A pair list of the parameters with their values.
	 */
	public static Map<String, Object> getParameters(Object obj)
					throws InvocationTargetException, IllegalAccessException, IllegalArgumentException {
		Map<String, Object> params = new HashMap<String, Object>();
		
		for (Method method : obj.getClass().getDeclaredMethods()) {
			if (isGetter(method)) {
				String fieldName =  getGetterFieldName(method.getName());
				Object value = method.invoke(obj);
				if (value != null) {
					if (method.getReturnType().isEnum()) {
						EnumValue newEnum = (EnumValue) method.invoke(obj);
						params.put(fieldName, ((EnumValue) newEnum).getValue());
					} else {
						params.put(fieldName, method.invoke(obj));
					}
				}
			}
		}
		return params;
	}
	
}