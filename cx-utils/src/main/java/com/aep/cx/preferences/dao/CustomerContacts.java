package com.aep.cx.preferences.dao;

public class CustomerContacts {
	private String webId;
	private String endPoint;
	
	public CustomerContacts(String webId, String endPoint) {
		super();
		this.webId = webId;
		this.endPoint = endPoint;
	}
	
	public CustomerContacts() {
		// TODO Auto-generated constructor stub
	}
	
	public String getWebId() {
		return webId;
	}
	
	public void setWebId(String webId) {
		this.webId = webId;
	}
	
	public String getEndPoint() {
		return endPoint;
	}
	
	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}
	
	@Override
	public String toString() {
		return "CustomerContacts [webId=" + webId + ", endPoint=" + endPoint + "]";
	}

	
}
