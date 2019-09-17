package com.aep.cx.preferences.dao;

import java.util.List;

public class CustomerInfo {

	private String premiseNumber;
	private String accountNumber;
	private String name;
	private String streetAddress;
	private String city;
	private String state;
	private String zipCode;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPremiseNumber() {
		return premiseNumber;
	}

	public void setPremiseNumber(String premiseNumber) {
		this.premiseNumber = premiseNumber;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	//private List<CustomerPreference> customerPreferences;
}
