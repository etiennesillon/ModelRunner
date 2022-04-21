package com.indirectionsoftware.utils;

public class IDCTriplet {
	
	private String key;
	int operator;
	private String value;
	
	public static final int EQUALS=0, ADDTO=1;
	
	public IDCTriplet(String key, int operator, String value) {
		this.key = key;
		this.value = value;
		this.operator = operator;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public int getOperator() {
		return operator;
	}

	public boolean isEquals() {
		return operator == EQUALS;
	}
	
	public boolean isAddTo() {
		return operator == ADDTO;
	}
	
}
