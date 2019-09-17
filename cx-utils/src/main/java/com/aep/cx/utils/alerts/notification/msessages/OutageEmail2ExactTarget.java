package com.aep.cx.utils.alerts.notification.msessages;

public class OutageEmail2ExactTarget extends EmailDeliveryHeader {

	private String MaxCustOut;
	private String TimeStamp;
	private String Cause;
	private String ETR;
	private String Duration;
	private String Outage;
	
	public String getMaxCustOut() {
		return MaxCustOut;
	}

	public void setMaxCustOut(String maxCustOut) {
		MaxCustOut = maxCustOut;
	}

	public String getTimeStamp() {
		return TimeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		TimeStamp = timeStamp;
	}

	public String getCause() {
		return Cause;
	}

	public void setCause(String cause) {
		Cause = cause;
	}

	public String getETR() {
		return ETR;
	}

	public void setETR(String eTR) {
		ETR = eTR;
	}

	public String getDuration() {
		return Duration;
	}

	public void setDuration(String duration) {
		Duration = duration;
	}

	public String getOutage() {
		return Outage;
	}

	public void setOutage(String outage) {
		Outage = outage;
	}

	@Override
	public String toString() {

		return super.toString() + "," + MaxCustOut + "," + TimeStamp + "," + Cause + "," + ETR + "," + Duration + ","
				+ Outage;
	}

}
