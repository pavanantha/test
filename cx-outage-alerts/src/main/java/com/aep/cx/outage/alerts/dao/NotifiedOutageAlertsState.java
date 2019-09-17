package com.aep.cx.outage.alerts.dao;

import java.util.ArrayList;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

//@DynamoDBTable(tableName="Test_Outage_Alerts_Current_State")
@DynamoDBTable(tableName="PREVIOUSLY_NOTIFIED_OUTAGE_ALERTS_INFO")
public class NotifiedOutageAlertsState {
	private String premiseNumber;
	
	private String asOfTime;
	private String notifiedETR;
	private String alertType;
	private String incomingFiles;
	private String incomingRequestID;



	@DynamoDBHashKey(attributeName="PremiseNumber")
	public String getPremiseNumber() {
		return premiseNumber;
	}

	public void setPremiseNumber(String premiseNumber) {
		this.premiseNumber = premiseNumber;
	}

	@DynamoDBAttribute(attributeName="AlertType")
	public String getAlertType() {
		return alertType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	@DynamoDBAttribute(attributeName="AsOfTime")
	public String getAsOfTime() {
		return asOfTime;
	}

	public void setAsOfTime(String asOfTime) {
		this.asOfTime = asOfTime;
	}

	@DynamoDBAttribute(attributeName="NotifedETR")
	public String getNotifiedETR() {
		return notifiedETR;
	}

	public void setNotifiedETR(String notifiedETR) {
		this.notifiedETR = notifiedETR;
	}
	
	@DynamoDBAttribute(attributeName="incomingFiles")
	public String getIncomingFiles() {
		return incomingFiles;
	}

	public void setIncomingFiles(String incomingFiles) {
		this.incomingFiles = incomingFiles;
	}

	@DynamoDBAttribute(attributeName="incomingRequestID")
	public String getIncomingRequestID() {
		return incomingRequestID;
	}

	public void setIncomingRequestID(String incomingRequestID) {
		this.incomingRequestID = incomingRequestID;
	}
}