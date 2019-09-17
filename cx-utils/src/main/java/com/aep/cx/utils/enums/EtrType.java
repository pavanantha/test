package com.aep.cx.utils.enums;

public enum EtrType {
UNAVAILABLE("u"),FIELD("F");
	private String etrTypeLetter;
	private EtrType(String s) {
		etrTypeLetter = s;
	}
	public String getEtrTypeLetter() {
		return etrTypeLetter;
	}
}
