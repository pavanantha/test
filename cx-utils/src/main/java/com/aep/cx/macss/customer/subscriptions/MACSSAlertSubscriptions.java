package com.aep.cx.macss.customer.subscriptions;

import java.util.ArrayList;

public class MACSSAlertSubscriptions {
	private String webID;
	private String endpoint;
	private ArrayList<SubscribedAlertNames> alertNames;
	public String getWebID() {
		return webID;
	}
	public void setWebID(String webID) {
		this.webID = webID;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public ArrayList<SubscribedAlertNames> getAlertNames() {
		return alertNames;
	}
	public void setAlertNames(ArrayList<SubscribedAlertNames> alertNames) {
		this.alertNames = alertNames;
	}	
}