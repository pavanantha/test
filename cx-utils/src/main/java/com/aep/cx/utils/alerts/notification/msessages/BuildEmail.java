package com.aep.cx.utils.alerts.notification.msessages;

import java.awt.TrayIcon.MessageType;
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
import com.aep.cx.utils.enums.NotificationTemplateType;
import com.aep.cx.utils.opco.OperatingCompany;

public class BuildEmail {
	private OutageEmail2ExactTarget outageXat;
	private String emailTemplate;
	private String emailSubject;
	private List<CustomerContacts> contacts;
	private OperatingCompany opcoDetails;
	//private ArrayList<CustomerPreferences> cpList;
	
	private List<String> xatPayload;
	//private List<BuildMessageHistory> macssload;
	private ArrayList<String> macssload;
	
	final Logger logger = LogManager.getLogger(BuildEmail.class);
	
	public BuildEmail() {
		// TODO Auto-generated constructor stub
	}
	
	public BuildEmail(ArrayList<AlertsNotificationData> notificationsList, HashMap<String, ArrayList<CustomerPreferences>> preferences) {
		logger.info("*** Entering BuildEmail ***");
		xatPayload = new ArrayList<String>();
		macssload = new ArrayList<String>();

		for (AlertsNotificationData msgNotification : notificationsList) {
			
			if (null == msgNotification.getOutageEtrTime()) {
				DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmm");
				DateTime dt = formatter.parseDateTime("200101010101");
				msgNotification.setOutageEtrTime(dt);
			}

			outageXat = new OutageEmail2ExactTarget();
			opcoDetails = new OperatingCompany(msgNotification.getPremiseNumber().substring(0,2));
			
			EnumMap<NotificationTemplateType, String> emailSubjectMap = getEmailSubjectMap();
			
			EnumMap<NotificationTemplateType, String> emailTemplateMap = getEmailTemplateMap();

			NotificationTemplateType templateType = NotificationTemplateType.valueOf(msgNotification.getAlertName().toUpperCase());
			
			if (null != msgNotification.getOutageEtrTime()) {

				this.emailSubject = emailSubjectMap.get(templateType)
						.replaceAll("\\*OPCOSN", opcoDetails.getAbbreviatedName().replace("%26", "&"))
						// .replaceAll("\\*ADDRESS", (outage.getHouseNbrAddress() + "
						// "+outage.getStreetNameAddress()).trim())
						.replaceAll("\\*ETR", msgNotification.getOutageEtrTime()
								.toString(DateTimeFormat.forPattern("h:mm a MM/dd/yyyy")));
			} 
			else {
				this.emailSubject = emailSubjectMap.get(templateType)
						.replaceAll("\\*OPCOSN", opcoDetails.getAbbreviatedName().replace("%26", "&"));

			}

			this.emailTemplate = emailTemplateMap.get(templateType);

			outageXat.setPreheader(this.emailSubject.trim());
			outageXat.setSubject(this.emailSubject.trim());
			outageXat.setChannelMemberID(opcoDetails.getChannelID());
			outageXat.setTemplate(this.emailTemplate.trim());

			for (CustomerPreferences custPref : preferences.get(msgNotification.getPremiseNumber())) {

				
				CustomerInfo custInfo = custPref.getCustomerInfo();
				outageXat.setAccountNumber(custInfo.getAccountNumber().trim());
				outageXat.setCity(custInfo.getCity());
				outageXat.setState(custInfo.getState());
				outageXat.setZipCode(custInfo.getZipCode());
				outageXat.setStreetAddress1(custInfo.getStreetAddress()+"***");
				outageXat.setFirstName(custInfo.getName());
				outageXat.setLearnMoreLink(opcoDetails.getOpcoSite()+opcoDetails.getLearnMoreLinkUrl());
				outageXat.setCause(msgNotification.getOutageSimpleCause());
				outageXat.setTimeStamp(DateTime.now().toString(DateTimeFormat.forPattern("M/d/yyyy h:mm:ss a")));
				outageXat.setDuration("unknown");
				outageXat.setAccountNickname("unknown");
				outageXat.setStreetAddress2("");
				outageXat.setOutage("NNNN");
				outageXat.setETR(msgNotification.getOutageEtrTime().toString(DateTimeFormat.forPattern("M/d/yyyy h:mm:ss a")));
				outageXat.setMaxCustOut(String.valueOf(msgNotification.getOutageMaxCount()));
				
				StringBuilder sb = new StringBuilder();
		        if (msgNotification.getPremisePowerStatus().equalsIgnoreCase("on") &&
		        	msgNotification.getAlertName().toLowerCase().contentEquals(NotificationTemplateType.RESTORED.toString().toLowerCase()) &&
		        	null != msgNotification.getOutageRestorationTime() && 
		        	msgNotification.getOutageRestorationTime().getMillis() > msgNotification.getOutageCreationTime().getMillis())
		        {
		        	logger.debug("Outage Type is restored/Cancelled " + msgNotification.getPremiseNumber() + msgNotification.getBatchKey());
		            DateTime rdate = (msgNotification.getOutageRestorationTime().minus(msgNotification.getOutageCreationTime().getMillis()));
		            long days = rdate.getMillis()/(1000 * 60*60*24);
		            //System.out.println("duration days :" + days);
		            
		            long hours = rdate.minusDays((int) days).getMillis()/(1000 * 60 * 60);
		            //System.out.println("duration hours :" + hours);
		            
		            long minutes = rdate.minusDays((int) days).minusHours((int) hours).getMillis()/(1000 * 60);
		            //System.out.println("duration hours :" + minutes);
		            if (days > 0)
		            {
		                sb.append(days == 1 ? "1 Day " : days + " Days ");
		            }
		            
		            if (hours > 0)
		            {
		            	sb.append(hours == 1 ? "1 Hour " : hours + " Hours ");
		            }
		            
		            if (minutes > 0)
		            {
		            	sb.append(minutes == 1 ? "1 Minute " : minutes + " Minutes ");
		            }
		            
		            //System.out.println("duration is :" + sb.toString());
		        }
		        else {
		        	//System.out.println("Outage Type is not restored");
		        	sb.append("unknown");
		        }
				
				for (CustomerContacts contact : custPref.getCustomerContacts()) {
					if (contact.getEndPoint().contains("@")) {
						outageXat.setHashLink(UUID.randomUUID().toString().replaceAll("-", ""));
						outageXat.setSubscriberKey(outageXat.getHashLink());
						outageXat.setEmailAddress(contact.getEndPoint());
						outageXat.setESID("");
						outageXat.setDuration(sb.toString());
						xatPayload.add(outageXat.toString());
						logger.debug("******** " + outageXat.toString() + "\n" + xatPayload.get(xatPayload.size()-1));
						BuildMessageHistory bm = new BuildMessageHistory();
						bm.setAccountNumber(custInfo.getAccountNumber());
						if (msgNotification.getAlertName().toLowerCase().contentEquals(NotificationTemplateType.PREDICTEDNOETR.toString().toLowerCase())) {
							bm.setAlertName(NotificationTemplateType.PREDICTED.toString().toLowerCase());
						}
						else {
							bm.setAlertName(msgNotification.getAlertName());
						}
						bm.setEndPoint(contact.getEndPoint());
						bm.setWebID(contact.getWebId());
						bm.setOutageNumber(msgNotification.getOutageNumber());
						bm.setEmailTemplate(emailTemplate);
						bm.setEmailSubject(emailSubject);
						bm.setMessageTrackId(outageXat.getHashLink());
						bm.setBatchFile(msgNotification.getBatchKey());
						bm.setEmailMetaData("Template:"+emailTemplate+"&MaxCustOut:"+msgNotification.getOutageMaxCount()+"&Cause:"+msgNotification.getOutageSimpleCause()+"&ETR:" + msgNotification.getOutageEtrTime().toString(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss"))+".0000000"+ "&Duration:"+ sb.toString());
						macssload.add(bm.getMacssBuild());
					}
				}
			}
		}
		
	}

	public EnumMap<NotificationTemplateType, String> getEmailTemplateMap() {
		EnumMap<NotificationTemplateType, String> emailTemplateMap = new EnumMap<NotificationTemplateType, String>(NotificationTemplateType.class);
		emailTemplateMap.put(NotificationTemplateType.PREDICTED, "OutagePredicted");
		emailTemplateMap.put(NotificationTemplateType.PREDICTEDNOETR, "OutagePredicted");
		emailTemplateMap.put(NotificationTemplateType.ETR, "OutageETR");
		emailTemplateMap.put(NotificationTemplateType.RESTORED, "Restoration");
		emailTemplateMap.put(NotificationTemplateType.CANCELLED, "OutageCancelled");
		return emailTemplateMap;
	}

	private EnumMap<NotificationTemplateType, String> getEmailSubjectMap() {
		EnumMap<NotificationTemplateType, String> emailSubjectMap = new EnumMap<>(NotificationTemplateType.class);
		emailSubjectMap.put(NotificationTemplateType.PREDICTED, "*OPCOSN : Outage Reported In Your Area");
		emailSubjectMap.put(NotificationTemplateType.PREDICTEDNOETR, "*OPCOSN : Outage Reported In Your Area");
		emailSubjectMap.put(NotificationTemplateType.ETR, "*OPCOSN : Updated Estimated Restoration Time");
		emailSubjectMap.put(NotificationTemplateType.RESTORED, "*OPCOSN : Power Has Been Restored");
		emailSubjectMap.put(NotificationTemplateType.CANCELLED, "*OPCOSN : Investigation of Outage Completed");
		return emailSubjectMap;
	}

	@Override
	public String toString() {
		return "BuildEmail [ emailTemplate =" + emailTemplate + ", emailSubject =" + emailSubject + ", contacts =" + contacts + "]";
	}

	public List<String> getXatPayload() {
		return xatPayload;
	}

	public void setXatPayload(List<String> xatPayload) {
		this.xatPayload = xatPayload;
	}

	public ArrayList<String> getMacssload() {
		return this.macssload;
	}

	public void setMacssload(ArrayList<String> macssload) {
		this.macssload = macssload;
	}
}
