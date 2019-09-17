package com.aep.cx.utils.alerts.notification.msessages;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.aep.cx.preferences.dao.CustomerContacts;
import com.aep.cx.preferences.dao.CustomerInfo;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.utils.alerts.common.data.AlertsNotificationData;
import com.aep.cx.utils.enums.EtrType;
import com.aep.cx.utils.enums.NotificationTemplateType;
import com.aep.cx.utils.opco.OperatingCompany;

public class BuildSMS {

	private String smsText;
	private String address;
	private List<CustomerContacts> contacts;
	private OperatingCompany opcoDetails;

	private List<String> smsPayload;
	private ArrayList<String> macssload;
	private BuildMessageHistory bm;
	
	final Logger logger = LogManager.getLogger(BuildSMS.class);
	
	public BuildSMS(ArrayList<AlertsNotificationData> notificationsList,HashMap<String, ArrayList<CustomerPreferences>> preferences) {
		logger.info("*** Entering BuildSMS ***");
		smsPayload = new ArrayList<String>();
		macssload = new ArrayList<String>();
		
		for (AlertsNotificationData msgNotification : notificationsList) {
					
			opcoDetails = new OperatingCompany(msgNotification.getPremiseNumber().substring(0,2));

			EnumMap<NotificationTemplateType, String> smsTextTemplate = getSmsTemplates();
			
			switch (NotificationTemplateType.valueOf(msgNotification.getAlertName().toUpperCase())) {
			case PREDICTED:
				this.smsText = smsTextTemplate.get(NotificationTemplateType.PREDICTED);
				break;
			case PREDICTEDNOETR: 
					this.smsText = smsTextTemplate.get(NotificationTemplateType.PREDICTEDNOETR);
					break;
			case ETR:
				this.smsText = smsTextTemplate.get(NotificationTemplateType.ETR);
				break;
			case RESTORED:
				/*if (this.outageSimpleCause.trim().length() > 0)
					this.smsText = smsTextTemplate.get("restoredCause");
				else*/
					this.smsText = smsTextTemplate.get(NotificationTemplateType.RESTORED);
				break;
			case CANCELLED:
				this.smsText = smsTextTemplate.get(NotificationTemplateType.CANCELLED);
				break;
			}
			
			for (CustomerPreferences custPref : preferences.get(msgNotification.getPremiseNumber())) {

				CustomerInfo custInfo = custPref.getCustomerInfo();
				
				if (null != msgNotification.getOutageEtrTime()) {
				this.smsText = this.smsText.replaceAll("\\*OPCOSN", opcoDetails.getAbbreviatedName())
						.replaceAll("\\*ADDRESS", (custInfo.getStreetAddress()+"***"))
						.replaceAll("\\*OPCOURL", opcoDetails.getOpcoSite()).replaceAll("\\*CAUSE", msgNotification.getOutageSimpleCause())
						.replaceAll("\\*SMSETR", msgNotification.getOutageEtrTime().toString(DateTimeFormat.forPattern("h:mm a MM/dd/yyyy")));
				}
				else {
					this.smsText = this.smsText.replaceAll("\\*OPCOSN", opcoDetails.getAbbreviatedName())
							.replaceAll("\\*ADDRESS", (custInfo.getStreetAddress()+"***"))
							.replaceAll("\\*OPCOURL", opcoDetails.getOpcoSite()).replaceAll("\\*CAUSE", msgNotification.getOutageSimpleCause());
				}
				for (CustomerContacts contact : custPref.getCustomerContacts()) {
					if (!contact.getEndPoint().contains("@")) {
						smsPayload.add("1" + contact.getEndPoint() + "||" + this.smsText + "~~");
						logger.info("sms payload:" + smsPayload.toString());
						bm = new BuildMessageHistory();
						bm.setAccountNumber(custInfo.getAccountNumber());
						if (msgNotification.getAlertName().toLowerCase().contentEquals(NotificationTemplateType.PREDICTEDNOETR.toString().toLowerCase())) {
							bm.setAlertName(NotificationTemplateType.PREDICTED.toString().toLowerCase());
						}
						else {
							bm.setAlertName(msgNotification.getAlertName());
						}
						//bm.setAlertName(msgNotification.getAlertName());
						bm.setEndPoint(contact.getEndPoint());
						bm.setWebID(contact.getWebId());
						bm.setOutageNumber(msgNotification.getOutageNumber());
						bm.setSmsText(smsText);
						bm.setMessageTrackId(UUID.randomUUID().toString().replaceAll("-", ""));
						bm.setBatchFile(msgNotification.getBatchKey());
						macssload.add(bm.getMacssBuild());
					}
				}
			}
		}
	}


	private EnumMap<NotificationTemplateType, String> getSmsTemplates() {
		EnumMap<NotificationTemplateType, String> smsTextTemplate = new EnumMap<>(NotificationTemplateType.class);
		smsTextTemplate.put(NotificationTemplateType.PREDICTED,
				"*OPCOSN: Outage in area of *ADDRESS. Power estimated to be on by *SMSETR. Will update if time changes. View map at *OPCOURL/outagemap");
		smsTextTemplate.put(NotificationTemplateType.PREDICTEDNOETR,
				"*OPCOSN: Outage in area of *ADDRESS. Estimated time restoration will be updated when available. View map at *OPCOURL/outagemap");
		smsTextTemplate.put(NotificationTemplateType.ETR,
				"*OPCOSN Update: Estimate for power on is *SMSETR for area of *ADDRESS. Thank you for your patience. View map at *OPCOURL/outagemap");
		smsTextTemplate.put(NotificationTemplateType.RESTORED,
				"*OPCOSN Power is on in area of *ADDRESS. Thank you for your patience. Still out? Report it *OPCOURL/outages/report");
		smsTextTemplate.put(NotificationTemplateType.CANCELLED,
				"*OPCOSN Power is ON in area of *ADDRESS. We apologize for any incovenience. If power is out please report it at *OPCOURL/outages/report");
		return smsTextTemplate;
	}



	public BuildSMS() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "BuildSMS [ smsText=" + smsText 	+ ", address=" + address + ", contacts=" + contacts + ", opcoDetails=" + opcoDetails + "]";
	}


	public List<String> getSmsPayload() {
		return smsPayload;
	}


	public void setSmsPayload(List<String> smsPayload) {
		this.smsPayload = smsPayload;
	}


	public ArrayList<String> getMacssload() {
		return this.macssload;
	}


	public void setMacssload(ArrayList<String> macssload) {
		this.macssload = macssload;
	}
}

