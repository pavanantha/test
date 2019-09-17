package com.aep.cx.preferences.dao;

import java.util.List;

public class CustomerPreferences {
	private CustomerInfo customerInfo;
	private List<CustomerContacts> customerContacts;
	public CustomerInfo getCustomerInfo() {
		return customerInfo;
	}
	public void setCustomerInfo(CustomerInfo customerInfo) {
		this.customerInfo = customerInfo;
	}
	public List<CustomerContacts> getCustomerContacts() {
		return customerContacts;
	}
	public void setCustomerContacts(List<CustomerContacts> customerContacts) {
		this.customerContacts = customerContacts;
	}
}
