package com.aep.cx.billing.alerts.enrollment.events;

import com.aep.cx.billing.events.BillingAlerts;
import com.aep.cx.billing.events.Header;

public class EnrollmentAlerts extends Header {

	public EnrollmentAlerts() {
	}

	public EnrollmentAlerts(String mqMessage) {
		BillingAlerts ba = new BillingAlerts(mqMessage);
		this.setAlertType(ba.getAlertType());
		this.setAlertDetails(ba.getAlertDetails());
		this.setEndPoint(ba.getEndPoint());
		this.setEzid(ba.getEzid());
		this.setPremiseNumber(ba.getPremiseNumber());
		this.setAccountNumber(ba.getAccountNumber());
		this.setWebID(ba.getWebID());
		this.setExternalID(ba.getExternalID());
		this.setMacssID(ba.getMacssID());
		this.setMacssEmailContent(ba.getMacssEmailContent());
		this.setEmailContent(ba.getEmailContent());
	}

	public EnrollmentAlerts(BillingAlerts ba) {
		this.setAlertType(ba.getAlertType());
		this.setAlertDetails(ba.getAlertDetails());
		this.setEndPoint(ba.getEndPoint());
		this.setEzid(ba.getEzid());
		this.setPremiseNumber(ba.getPremiseNumber());
		this.setAccountNumber(ba.getAccountNumber());
		this.setWebID(ba.getWebID());
		this.setExternalID(ba.getExternalID());
		this.setMacssID(ba.getMacssID());
		this.setMacssEmailContent(ba.getMacssEmailContent());
		this.setEmailContent(ba.getEmailContent());
		this.setCustomerInfo(ba.getCustomerInfo());
	}
}
