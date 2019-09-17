package com.aep.cx.outage.alerts.domains;

import org.joda.time.DateTime;

import com.aep.cx.outage.alerts.enums.ValueAddProcessingReasonType;
import com.aep.cx.utils.time.CustomStdDateTimeDeserializer;
import com.aep.cx.utils.time.CustomStdDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class AlertsNotificationData {

	private String accountNumber;
	private String premiseNumber;
	private String premisePowerStatus;
	private String outageStatus;
	private Integer outageNumber;
	private String alertName;
	private String outageSimpleCause;
	private String etrType;
	private int outageMaxCount;
	private int outageCurrentCount;
	@JsonIgnore
	private ValueAddProcessingReasonType valueAddProcessingReasonType;
	@JsonDeserialize (using = CustomStdDateTimeDeserializer.class)
	@JsonSerialize (using = CustomStdDateTimeSerializer.class)
	private DateTime outageEtrTime;	
	@JsonDeserialize (using = CustomStdDateTimeDeserializer.class)
	@JsonSerialize (using = CustomStdDateTimeSerializer.class)
	private DateTime outageCreationTime;
	@JsonDeserialize (using = CustomStdDateTimeDeserializer.class)
	@JsonSerialize (using = CustomStdDateTimeSerializer.class)
	private DateTime outageRestorationTime;
	/*
	@JsonIgnore
	@JsonDeserialize (using = CustomStdDateTimeDeserializer.class)
	@JsonSerialize (using = CustomStdDateTimeSerializer.class)
	private DateTime outageAsOfTime;
	@JsonIgnore
	private String outageType;
	@JsonIgnore
	private String outageETRType;
	*/

	public AlertsNotificationData() {
		// TODO Auto-generated constructor stub
	}

	/*
	public String getOutageETRType() {
        return this.outageETRType;
    }

    public void setOutageETRType(String outageETRType) {
        this.outageETRType = outageETRType;
	}

	public String getOutageType() {
        return this.outageType;
    }

    public void setOutageType(String outageType) {
        this.outageType = outageType;
	}

    public void setOutageAsOfTime(DateTime outageAsOfTime) {
        this.outageAsOfTime = outageAsOfTime;
    }

    public DateTime getOutageAsOfTime() {
        return outageAsOfTime;
	}
	*/
    public ValueAddProcessingReasonType getValueAddProcessingReasonType() {
        return valueAddProcessingReasonType;
    }

    public void setValueAddProcessingReasonType(ValueAddProcessingReasonType valueAddProcessingReasonType) {
        this.valueAddProcessingReasonType = valueAddProcessingReasonType;
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

	public String getPremisePowerStatus() {
		return premisePowerStatus;
	}

	public void setPremisePowerStatus(String premisePowerStatus) {
		this.premisePowerStatus = premisePowerStatus;
	}

	public String getOutageStatus() {
		return outageStatus;
	}

	public void setOutageStatus(String outageStatus) {
		this.outageStatus = outageStatus;
	}

	public String getAlertName() {
		return alertName;
	}

	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}

	public String getOutageSimpleCause() {
		return outageSimpleCause;
	}

	public void setOutageSimpleCause(String outageSimpleCause) {
		this.outageSimpleCause = outageSimpleCause;
	}

	public String getEtrType() {
		return etrType;
	}

	public void setEtrType(String etrType) {
		this.etrType = etrType;
	}

	public DateTime getOutageEtrTime() {
		return outageEtrTime;
	}

	public void setOutageEtrTime(DateTime outageEtrTime) {
		this.outageEtrTime = outageEtrTime;
	}

	public DateTime getOutageCreationTime() {
		return outageCreationTime;
	}

	public void setOutageCreationTime(DateTime outageCreationTime) {
		this.outageCreationTime = outageCreationTime;
	}

	public DateTime getOutageRestorationTime() {
		return outageRestorationTime;
	}

	public void setOutageRestorationTime(DateTime outageRestorationTime) {
		this.outageRestorationTime = outageRestorationTime;
	}

	public Integer getOutageNumber() {
		return outageNumber;
	}

	public void setOutageNumber(Integer outageNumber) {
		this.outageNumber = outageNumber;
	}

	public int getOutageMaxCount() {
		return outageMaxCount;
	}

	public void setOutageMaxCount(int outageMaxCount) {
		this.outageMaxCount = outageMaxCount;
	}

	public int getOutageCurrentCount() {
		return outageCurrentCount;
	}

	public void setOutageCurrentCount(int outageCurrentCount) {
		this.outageCurrentCount = outageCurrentCount;
	}

}
