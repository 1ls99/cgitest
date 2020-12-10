package com.example.demo.route;

public class CSVCell {
	private static final String EMPTY_STRING = "";
	private static final String EXPECTED_INTEGER = "Expected an integer actual value: ->%s<-";
	static boolean isNull(String value) {
		return value == null ? true : false;
	}
	
	static boolean canCastAsInteger(String value) {
		if (isNull(value)) return false;
		
		value = value.trim();
		
		if (EMPTY_STRING.equals(value))
			return false;
		
		try {
			Integer.parseInt(value);
		} catch(NumberFormatException nfe) {
			return false;
		}
		
		return true;
	}
	
	static int asInteger(String value) {
		if (!canCastAsInteger(value))
			throw new RuntimeException(String.format(EXPECTED_INTEGER, value));
		return Integer.valueOf(value.trim());
	}
}
