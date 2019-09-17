package com.aep.cx.preferences.dao;

import java.util.List;

public class CustomerPreference {

	private CustomerInfo customerInfo;

	private List<CustomerChannel> customerChannels;

	public CustomerInfo getCustomerInfo() {
		return customerInfo;
	}

	public void setCustomerInfo(CustomerInfo customerInfo) {
		this.customerInfo = customerInfo;
	}

	public List<CustomerChannel> getCustomerChannels() {
		return customerChannels;
	}

	public void setCustomerChannel(List<CustomerChannel> customerChannels) {
		this.customerChannels = customerChannels;
	}
}
