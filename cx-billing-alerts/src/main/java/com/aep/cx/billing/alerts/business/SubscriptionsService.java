package com.aep.cx.billing.alerts.business;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.aep.cx.billing.alerts.enrollment.events.EnrollmentAlerts;
import com.aep.cx.billing.events.Payment;
import com.aep.cx.preferences.dao.BillingPreferencesDAO;
import com.aep.cx.preferences.dao.CustomerContacts;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.aep.cx.utils.alerts.notification.msessages.BuildMessageHistory;
import com.aep.cx.utils.alerts.notification.msessages.EmailDeliveryHeader;
import com.aep.cx.utils.enums.NotificationTemplateType;
import com.aep.cx.utils.enums.OperatingCompanyType;
import com.aep.cx.utils.enums.PreferencesTypes;
import com.aep.cx.utils.opco.OperatingCompanyManager;
import com.aep.cx.utils.opco.OperatingCompanyV2;
import com.amazonaws.services.s3.model.RequestPaymentConfiguration.Payer;

public class SubscriptionsService {

	static String bucketKey = null;

	public String BuildProfileContent (ArrayList<EnrollmentAlerts> enrollmentAlerts,String key) {
		bucketKey = key;
		try {
			//SubscriptionsService ps = new SubscriptionsService();
			for (EnrollmentAlerts enrollment : enrollmentAlerts) {
					HashMap<String, ArrayList<String>> emailContent = this.BuildEmail(enrollment);
					HashMap<String, ArrayList<String>> smsContent = this.BuildSMS(enrollment);
			}
			return "Successfully processed : Subscrition build content and stored into per account bucket="+key;
		} catch (Exception e) {
			return "Failed processing : content build for Subscrition Service="+key;
		}
	}
	
	public HashMap<String,ArrayList<String>> BuildEmail (EnrollmentAlerts enrollment) {

		HashMap<String, String> emailTemplate = GetEmailTemplate(enrollment);
		HashMap<String,ArrayList<String>> emailDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> emailList = new ArrayList<String>(); 
		ArrayList<String> historyList = new ArrayList<String>();
		
        if (null != emailTemplate.get("Subject") && null != emailTemplate.get("Template") ) {
			OperatingCompanyManager cm = new OperatingCompanyManager();
			Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
			OperatingCompanyV2 opcoDetails = opcoBuild.get(enrollment.getAccountNumber().substring(0, 2));
			EmailDeliveryHeader emailHeader = new EmailDeliveryHeader();
			emailHeader.setAccountNumber(enrollment.getAccountNumber());
			emailHeader.setAccountNickname("");
			emailHeader.setChannelMemberID(opcoDetails.getChannelID());
			emailHeader.setCity(enrollment.getCustomerInfo().getCity());
			emailHeader.setStreetAddress1(enrollment.getCustomerInfo().getStreetAddress());
			emailHeader.setESID(enrollment.getEzid());
			emailHeader.setFirstName(enrollment.getCustomerInfo().getName());
			emailHeader.setLearnMoreLink(opcoDetails.getOpcoSite() + opcoDetails.getLearnMoreLinkUrl());
			emailHeader.setPreheader(emailTemplate.get("Subject")
					.replaceAll("\\*OPCOSN", opcoDetails.getAbbreviatedName()).replaceAll("%26", "&"));
			emailHeader.setState(enrollment.getCustomerInfo().getState());
			emailHeader.setStreetAddress2("");
			emailHeader.setSubject(emailHeader.getPreheader());
			emailHeader.setTemplate(emailTemplate.get("Template"));
			emailHeader.setZipCode(enrollment.getCustomerInfo().getZipCode());
			if (enrollment.getEndPoint().contains("@")) {
				emailHeader.setHashLink(UUID.randomUUID().toString().replaceAll("-", ""));
				emailHeader.setSubscriberKey(emailHeader.getHashLink());
				emailHeader.setEmailAddress(enrollment.getEndPoint());
				emailList.add(enrollment.getExternalID() + emailHeader.toString() + "," + enrollment.getEmailContent());

				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(enrollment.getAccountNumber());
				bm.setAlertName(enrollment.getAlertType());
				bm.setEndPoint(enrollment.getEndPoint());
				bm.setWebID(enrollment.getWebID());
				bm.setOutageNumber(0);
				bm.setEmailTemplate(emailHeader.getTemplate());
				bm.setEmailSubject(emailHeader.getSubject());
				bm.setMessageTrackId(emailHeader.getHashLink());
				bm.setEmailMetaData("Template:" + bm.getEmailTemplate() + enrollment.getMacssEmailContent());
				historyList.add(bm.getMacssBuild());
			} 
		}
		if (emailList.size() > 0) {
			String bucketKey = "email_"+enrollment.getAccountNumber()+"_"+enrollment.getAlertType()+"_"+DateTime.now().toString("yyyyMMddHHmmss");
			emailDetails.put("xat", emailList);
			emailDetails.put("history", historyList);
			LoadData2S3 load2S3 = new LoadData2S3();
			load2S3.loadData(System.getenv("NOTIFICATION_EMAIL_BUCKET"), bucketKey, emailList);
			load2S3.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), bucketKey, historyList);
		}
		return emailDetails;
	}
	
	public HashMap<String,ArrayList<String>> BuildSMS (EnrollmentAlerts enrollment) {

		HashMap<String,ArrayList<String>> smsDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> smsList = new ArrayList<String>(); 
		ArrayList<String> historyList = new ArrayList<String>();
		HashMap<String, String> smsTemplate = GetSMSTemplate(enrollment);
		String smsText = smsTemplate.get("SMSText");
		if (null != smsText) {
			DecimalFormat df = new DecimalFormat("$0.00##");   
			NumberFormat formatter = NumberFormat.getCurrencyInstance();
	        OperatingCompanyManager cm = new OperatingCompanyManager();
	        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
	        OperatingCompanyV2 opcoDetails = opcoBuild.get(enrollment.getAccountNumber().substring(0,2));
			smsText = smsTemplate.get("SMSText").replaceAll("\\*OPCOSN", opcoDetails.getAbbreviatedName())
					.replaceAll("\\*ACCT", enrollment.getAccountNumber().substring(6, 6 + 5))
					.replaceAll("\\*OPCOURL", opcoDetails.getOpcoSite());
			if (!enrollment.getEndPoint().contains("@")) {

				smsText = smsText.replaceAll("\\*ADDRESS", enrollment.getCustomerInfo().getStreetAddress());
				smsList.add("1" + enrollment.getEndPoint() + "||" + smsText + "~~");
				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(enrollment.getAccountNumber());
				bm.setAlertName(enrollment.getAlertType());
				bm.setEndPoint(enrollment.getEndPoint());
				bm.setWebID(enrollment.getWebID());
				bm.setOutageNumber(0);
				bm.setMessageTrackId(UUID.randomUUID().toString().replaceAll("-", ""));
				bm.setSmsText(smsText.replaceAll("%26", "&"));
				historyList.add(bm.getMacssBuild());

			} 
		}
		if (smsList.size() > 0) {
			String bucketKey = "sms_"+enrollment.getAccountNumber()+"_"+enrollment.getAlertType()+"_"+DateTime.now().toString("yyyyMMddHHmmss");
			LoadData2S3 load2S3 = new LoadData2S3();
			load2S3.loadData(System.getenv("NOTIFICATION_SMS_BUCKET"), bucketKey, smsList);
			load2S3.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), bucketKey, historyList);
			smsDetails.put("sms", smsList);
			smsDetails.put("history",historyList);
		}
		return smsDetails;
	}
	 
	public HashMap<String,String> GetEmailTemplate (EnrollmentAlerts enrollment) {
		
		OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(enrollment.getAccountNumber().substring(0,2));
        
		HashMap<String,String> emailTemplate = new HashMap<String, String>();
		switch (enrollment.getAlertType().toLowerCase()) {
		case "welcome-email":
			
			if (opcoDetails.getOperatingCompanyType().equals(OperatingCompanyType.IndianaMichiganPower.toString())) {
				emailTemplate.put("Subject", "Welcome to I&M ON THE GO - Mobile Alerts");
			}
			else {
				emailTemplate.put("Subject", "Welcome to *OPCOSN Alerts.");
			}
			
			emailTemplate.put("Template", "SubscriptionWelcome");
			break;
		case "confirm-email":
			if (opcoDetails.getOperatingCompanyType().equals(OperatingCompanyType.IndianaMichiganPower.toString())) {
				emailTemplate.put("Subject", "Update to Your I&M ON THE GO - Mobile Alerts");
			}
			else {
				emailTemplate.put("Subject", "Update to Your *OPCOSN Alerts.");
			}
			emailTemplate.put("Template", "SubscriptionUpdate");			
			break;
		case "auto-welcome":
			emailTemplate.put("Template", "AutoEnrollWelcome");
			emailTemplate.put("Subject", "Welcome to Alerts.");
			
			break;
		case "auto-registered":
			emailTemplate.put("Template", "AutoEnrollRegistered");
			emailTemplate.put("Subject", "Welcome to Alerts.");
			
			break;

		default:
			break;
		}
		return emailTemplate;
	}
	
	public HashMap<String,String> GetSMSTemplate (EnrollmentAlerts enrollment) {

		HashMap<String,String> smsTemplate = new HashMap<String, String>();
		//smsTemplate.put("SMSText", "*OPCOSN has received your payment of *PAYMENT for your account ending in *ACCT at *ADDRESS.  Visit: *OPCOURL/account.~~");
		
		OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(enrollment.getAccountNumber().substring(0,2));
       
		switch (enrollment.getAlertType().trim().toUpperCase()) {
		case "DOPT-TEXT":
			
			if (opcoDetails.getOperatingCompanyType().equals(OperatingCompanyType.IndianaMichiganPower.toString())) {
				smsTemplate.put("SMSText", "*OPCOSN received a request for *OPCOSN ON THE GO - Mobile Alerts be sent to this number. \" + \"Reply YES to confirm and activate alerts. HELP for help. STOP to stop.");
			}
			else {
				smsTemplate.put("SMSText","*OPCOSN received a request for alerts to be sent to this number. Reply YES to confirm this request and activate alerts. HELP for help. STOP to stop.");
			}
			break;
		case "DOPT-CONFIRM":
			if (opcoDetails.getOperatingCompanyType().equals(OperatingCompanyType.IndianaMichiganPower.toString())) {
				smsTemplate.put("SMSText", "Thanks for confirming. You are now enrolled in *OPCOSN ON THE GO - Mobile text alerts.");
			}
			else {
				smsTemplate.put("SMSText", "Thanks for confirming. You are now enrolled in *OPCOSN's text alerts.");
			}
			break;
		case "WELCOME-TEXT":
			smsTemplate.put("SMSText", "You enrolled in *OPCOSN's text alerts program for *ADDRESS. Number of texts per month will vary. HELP for help. STOP to quit. Msg%26Data rates may apply.");
			if (opcoDetails.getOperatingCompanyType().equals(OperatingCompanyType.IndianaMichiganPower.toString())) {
				smsTemplate.put("SMSText", "You are enrolled in *OPCOSN ON THE GO - Mobile Alerts for *ADDRESS. HELP for help. STOP to quit. Message %26 Data rates may apply.");
			}
			
			break;
		case "CONFIRM-TEXT":
			smsTemplate.put("SMSText", "Your *OPCOSN text alerts for *ADDRESS have been updated. Not sure what changed? Log in at *OPCOURL/alerts to view your alerts.");
			if (opcoDetails.getOperatingCompanyType().equals(OperatingCompanyType.IndianaMichiganPower.toString())) {
				smsTemplate.put("SMSText", "*OPCOSN ON THE GO - Mobile Alerts for *ADDRESS have been updated. Not sure what changed? Log in at *OPCOURL/alerts to view your alerts.");
			}
			
			break;
		case "UNSUB-TEXT":
			smsTemplate.put("SMSText", "You have unsubscribed from *OPCOSN's text alerts.");
			if (opcoDetails.getOperatingCompanyType().equals(OperatingCompanyType.IndianaMichiganPower.toString())) {
				smsTemplate.put("SMSText", "You have unsubscribed from *OPCOSN ON THE GO text alerts program.");
			}
			
			break;
		case "UNSUB-TEXT-PPAY":
			smsTemplate.put("SMSText", "You have unsubscribed from *OPCOSN's text alerts including PowerPay.");			
			break;

		default:
			break;
		}

		return smsTemplate;
	}
}

