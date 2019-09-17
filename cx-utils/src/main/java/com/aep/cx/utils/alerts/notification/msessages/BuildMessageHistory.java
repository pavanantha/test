package com.aep.cx.utils.alerts.notification.msessages;

public class BuildMessageHistory {

	private String accountNumber;
	private Integer outageNumber;
	private String alertName;
	private String emailTemplate;
	private String emailSubject;
	private String webID;
	private String endPoint;
	private String messageTrackId;
	private String smsText;
	private String emailMetaData;
	private String batchFile;

	public BuildMessageHistory(BuildEmail email) {

	}

	public BuildMessageHistory(BuildSMS sms) {

	}

	public String getEmailMetaData() {
		return emailMetaData;
	}

	public void setEmailMetaData(String emailMetaData) {
		this.emailMetaData = emailMetaData;
	}

	public String getSmsText() {
		return smsText;
	}

	public void setSmsText(String smsText) {
		this.smsText = smsText;
	}

	public BuildMessageHistory() {
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public Integer getOutageNumber() {
		return outageNumber;
	}

	public void setOutageNumber(Integer outageNumber) {
		this.outageNumber = outageNumber;
	}

	public String getAlertName() {
		return alertName;
	}

	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}

	public String getEmailTemplate() {
		return emailTemplate;
	}

	public void setEmailTemplate(String emailTemplate) {
		this.emailTemplate = emailTemplate;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public String getWebID() {
		return webID;
	}

	public void setWebID(String webID) {
		this.webID = webID;
	}

	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}
	
	public String getMessageTrackId() {
		return messageTrackId;
	}

	public void setMessageTrackId(String messageTrackId) {
		this.messageTrackId = messageTrackId;
	}

	
	public String getBatchFile() {
		return batchFile;
	}

	public void setBatchFile(String batchFile) {
		this.batchFile = batchFile;
	}

	@Override
	public String toString() {
		return "BuildMessageHistory [accountNumber=" + accountNumber + ", outageNumber=" + outageNumber + ", alertName="
				+ alertName + ", emailTemplate=" + emailTemplate + ", emailSubject=" + emailSubject + ", webID=" + webID
				+ ", endPoint=" + endPoint + ", smsText=" + smsText + ", emailMetaData=" + emailMetaData + "]";
	}

	public String getMacssBuild() {
		String content=null;
		if (null != this.getSmsText()) {
			content= webID + "|" + endPoint + "|" + accountNumber + "|" + alertName + "|" + outageNumber.toString() + "|" + messageTrackId +"|" + smsText.trim() + "|" + batchFile;
		}
		if (null != this.getEmailMetaData())
		{
			content= webID + "|" + endPoint + "|" + accountNumber + "|" + alertName + "|" + outageNumber.toString() + "|" + messageTrackId +"|" + emailMetaData.trim()+ "|" + batchFile;
		}
		System.out.println(content);
		return content;
	}

}