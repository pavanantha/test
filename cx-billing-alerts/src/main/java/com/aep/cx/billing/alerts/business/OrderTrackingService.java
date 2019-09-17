package com.aep.cx.billing.alerts.business;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.aep.cx.billing.alerts.lambda.handlers.LoadBillingAlertsByType;
import com.aep.cx.billing.events.OrderTracking;
import com.aep.cx.billing.events.Payment;
import com.aep.cx.preferences.dao.BillingPreferencesDAO;
import com.aep.cx.preferences.dao.CustomerContacts;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.aep.cx.utils.alerts.notification.msessages.BuildMessageHistory;
import com.aep.cx.utils.alerts.notification.msessages.EmailDeliveryHeader;
import com.aep.cx.utils.enums.NotificationTemplateType;
import com.aep.cx.utils.enums.PreferencesTypes;
import com.aep.cx.utils.opco.OperatingCompanyManager;
import com.aep.cx.utils.opco.OperatingCompanyV2;
import com.amazonaws.services.s3.model.RequestPaymentConfiguration.Payer;

public class OrderTrackingService {
	
	static CustomerPreferences prefs = null;
	static BillingPreferencesDAO dao = null;
	static String bucketKey = null;
	
	final Logger logger = LogManager.getLogger(PaymentService.class);

	public String BuildOrderTrackingContent (ArrayList<OrderTracking> orderTrackingList,String key) {
		bucketKey = key;
		try {
		dao = new BillingPreferencesDAO();
		for (OrderTracking order : orderTrackingList) {
			prefs = dao.getCustomerPreferences(order.getAccountNumber(), PreferencesTypes.ORDER.toString());
		
			System.out.println("prefereces="+prefs);
			if (null != prefs && null != prefs.getCustomerContacts() && prefs.getCustomerContacts().size() > 0) {
				BuildEmail(order,prefs);
				BuildSMS(order,prefs);
			}
		}
		return "Successfully processed : build content and stored into per account bucket = " + key;
		} catch (Exception e) {
			return "Failed processing : content build for payment = " + key;
		}
	}
	
	public HashMap<String,ArrayList<String>> BuildEmail (OrderTracking orderTracking,CustomerPreferences prefs) {

		HashMap<String, String> emailTemplate = GetEmailTemplate(orderTracking);
		HashMap<String,ArrayList<String>> emailDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> emailList = new ArrayList<String>(); 
		ArrayList<String> historyList = new ArrayList<String>();
		
        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(orderTracking.getAccountNumber().substring(0,2));
        
		EmailDeliveryHeader emailHeader = new EmailDeliveryHeader();
		
		emailHeader.setAccountNumber(orderTracking.getAccountNumber());
		emailHeader.setAccountNickname("");
		emailHeader.setChannelMemberID(opcoDetails.getChannelID());
		emailHeader.setCity(prefs.getCustomerInfo().getCity());
		emailHeader.setStreetAddress1(prefs.getCustomerInfo().getStreetAddress()+"***");
		emailHeader.setESID(orderTracking.getEzid());
		emailHeader.setFirstName(prefs.getCustomerInfo().getName());
		emailHeader.setLearnMoreLink(opcoDetails.getOpcoSite()+opcoDetails.getLearnMoreLinkUrl());
		emailHeader.setPreheader(emailTemplate.get("Subject")
				.replaceAll("\\*OPCOSN", opcoDetails.getAbbreviatedName())
				.replaceAll("%26", "&"));
		emailHeader.setState(prefs.getCustomerInfo().getState());
		emailHeader.setStreetAddress2("");
		emailHeader.setSubject(emailHeader.getPreheader());
		emailHeader.setTemplate(emailTemplate.get("Template"));
		emailHeader.setZipCode(prefs.getCustomerInfo().getZipCode());
		
		for (CustomerContacts cc : prefs.getCustomerContacts()) {
			if(cc.getEndPoint().contains("@")) {
				emailHeader.setHashLink(UUID.randomUUID().toString().replaceAll("-", ""));
				emailHeader.setSubscriberKey(emailHeader.getHashLink());
				emailHeader.setEmailAddress(cc.getEndPoint());
				emailList.add(orderTracking.getExternalID()+emailHeader.toString()+","+orderTracking.getEmailContent());
				
				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(orderTracking.getAccountNumber());
				bm.setAlertName(orderTracking.getAlertType());
				bm.setEndPoint(cc.getEndPoint());
				bm.setWebID(cc.getWebId());
				bm.setOutageNumber(0);
				bm.setEmailTemplate(emailHeader.getTemplate());
				bm.setEmailSubject(emailHeader.getSubject());
				bm.setMessageTrackId(emailHeader.getHashLink());
				bm.setEmailMetaData("Template:"+bm.getEmailTemplate()+orderTracking.getMACSSEmailContent());
				historyList.add(bm.getMacssBuild());			
			}
		}
		if (emailList.size() > 0) {
			String bucketKey = "email_"+orderTracking.getAccountNumber()+"_"+DateTime.now().toString("yyyyMMddHHmmss");
			emailDetails.put("xat", emailList);
			emailDetails.put("history", historyList);
			LoadData2S3 load2S3 = new LoadData2S3();
			load2S3.loadData(System.getenv("NOTIFICATION_EMAIL_BUCKET"), bucketKey, emailList);
			load2S3.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), bucketKey, historyList);
		}
		return emailDetails;
	}
	
	public HashMap<String,ArrayList<String>> BuildSMS (OrderTracking orderTracking,CustomerPreferences prefs) {

		HashMap<String,ArrayList<String>> smsDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> smsList = new ArrayList<String>(); 
		ArrayList<String> historyList = new ArrayList<String>();
		
        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(orderTracking.getAccountNumber().substring(0,2));
        HashMap<String, String> smsTemplate = GetSMSTemplate(orderTracking);
        
        String smsText = smsTemplate.get("SMSText")
        		.replaceAll("\\*OPCOSN", opcoDetails.getAbbreviatedName())
        		.replaceAll("\\*OPCOURL", opcoDetails.getOpcoSite());
        
		for (CustomerContacts cc : prefs.getCustomerContacts()) {
			if(!cc.getEndPoint().contains("@")) {
				
		        smsText = smsText
		        		.replaceAll("\\*ADDRESS", prefs.getCustomerInfo().getStreetAddress()+"***");
				smsList.add("1"+cc.getEndPoint()+"||"+smsText+"~~");
				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(orderTracking.getAccountNumber());
				bm.setAlertName(orderTracking.getAlertType());
				bm.setEndPoint(cc.getEndPoint());
				bm.setWebID(cc.getWebId());
				bm.setOutageNumber(0);
				bm.setMessageTrackId(UUID.randomUUID().toString().replaceAll("-", ""));
				bm.setSmsText(smsText.replaceAll("%26", "&"));
				historyList.add(bm.getMacssBuild());			
			}
		}
        
		if (smsList.size() > 0) {
			String bucketKey = "sms_"+orderTracking.getAccountNumber()+"_"+DateTime.now().toString("yyyyMMddHHmmss");
			LoadData2S3 load2S3 = new LoadData2S3();
			load2S3.loadData(System.getenv("NOTIFICATION_SMS_BUCKET"), bucketKey, smsList);
			load2S3.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), bucketKey, historyList);
			smsDetails.put("sms", smsList);
			smsDetails.put("history",historyList);
		}
		return smsDetails;
	}
	 
	public HashMap<String,String> GetEmailTemplate (OrderTracking orderTracking) {

		HashMap<String,String> emailTemplate = new HashMap<String, String>();
		
		if (orderTracking.getAlertType().toLowerCase().contentEquals("ordr-complete")) {
			emailTemplate.put("Template", "OrderCompleted");
			emailTemplate.put("Subject", "Your order is completed");
		}
		
		if (orderTracking.getAlertType().toLowerCase().contentEquals("insp-complete")) {
			emailTemplate.put("Template", "OrderStatus");
			emailTemplate.put("Subject", "Order Status");
		}

		return emailTemplate;
	}
	
	public HashMap<String,String> GetSMSTemplate (OrderTracking orderTracking) {

		HashMap<String,String> smsTemplate = new HashMap<String, String>();
		
		if (orderTracking.getAlertType().toLowerCase().contentEquals("ordr-complete")) {
			smsTemplate.put("SMSText", "*OPCOSN: Service work at *ADDRESS is complete. Please visit https://aeptexas.com/builders/orders/default.aspx for further details regarding your order.");
		}
		
		if (orderTracking.getAlertType().toLowerCase().contentEquals("insp-complete")) {
			smsTemplate.put("SMSText", "*OPCOSN: A requirement for your order at *ADDRESS is updated. Please visit https://aeptexas.com/builders/orders/default.aspx for details.");
		}

		return smsTemplate;
	}
}

