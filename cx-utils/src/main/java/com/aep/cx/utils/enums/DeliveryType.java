package com.aep.cx.utils.enums;

public enum DeliveryType {
TEXT_DELIVERY("TEXT-DELIVERY"),EMAIL_DELIVERY("EMAIL-DELIVERY");
	private final String realName;
		
	DeliveryType(String realName) {
		this.realName = realName;		
	}
	
	public String getRealName() {
		return realName;
	}
}
