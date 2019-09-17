package com.aep.cx.billing.events;

import com.aep.cx.preferences.dao.CustomerInfo;
import com.aep.cx.utils.opco.*;

public class Header {

	// private String region;
	// private String tdat;
	private String alertType;
	private String accountNumber;
	private String premiseNumber;
	private String ezid;
	private String macssEmailContent;
	private String emailContent;
	private String macssID;
	private String externalID;
	private String webID;
	private String endPoint;
	private String alertDetails;
	private CustomerInfo customerInfo;
	private String opcoAbbreviatedName;

	public Header() {
	}

	public String getAlertType() {
		return alertType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getPremiseNumber() {
		return premiseNumber;
	}

	public void setPremiseNumber(String premiseNumber) {
		this.premiseNumber = premiseNumber;
	}

	public String getMacssEmailContent() {
		return macssEmailContent;
	}

	public void setMacssEmailContent(String macssEmailContent) {
		this.macssEmailContent = macssEmailContent;
	}

	public String getEmailContent() {
		return emailContent;
	}

	public void setEmailContent(String emailContent) {
		this.emailContent = emailContent;
	}

	public String getMacssID() {
		return macssID;
	}

	public void setMacssID(String macssID) {
		this.macssID = macssID;
	}

	public String getExternalID() {
		return externalID;
	}

	public void setExternalID(String externalID) {
		this.externalID = externalID;
	}

	public String getWebID() {
		return webID;
	}

	public void setWebID(String webID) {
		this.webID = webID;
	}

	public String getEzid() {
		return ezid;
	}

	public void setEzid(String ezid) {
		this.ezid = ezid;
	}

	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public String getAlertDetails() {
		return alertDetails;
	}

	public void setAlertDetails(String alertDetails) {
		this.alertDetails = alertDetails;
	}

	public CustomerInfo getCustomerInfo() {
		return customerInfo;
	}

	public void setCustomerInfo(CustomerInfo customerInfo) {
		this.customerInfo = customerInfo;
	}

	public String getOpcoAbbreviatedName() {
		return opcoAbbreviatedName;
	}

	public void setOpcoAbbreviatedName(String opcoAbbreviatedName) {
		this.opcoAbbreviatedName = opcoAbbreviatedName;
	}
}