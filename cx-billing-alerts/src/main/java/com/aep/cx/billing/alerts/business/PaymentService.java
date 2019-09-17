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

public class PaymentService {
	
	static CustomerPreferences prefs = null;
	static BillingPreferencesDAO dao = null;
	static String bucketKey = null;
	
	final Logger logger = LogManager.getLogger(PaymentService.class);

	public static String BuildPaymentContent (ArrayList<Payment> paymentList,String key) {
		bucketKey = key;
		try {
		dao = new BillingPreferencesDAO();
		PaymentService ps = new PaymentService();
		for (Payment payment : paymentList) {
			prefs = dao.getCustomerPreferences(payment.getAccountNumber(), PreferencesTypes.BILLINGPAYMENT.toString());
		
			System.out.println("prefereces="+prefs);
			if (null != prefs && null != prefs.getCustomerContacts() && prefs.getCustomerContacts().size() > 0) {
				ps.BuildEmail(payment,prefs);
				ps.BuildSMS(payment,prefs);
			}
		}
		return "Successfully processed : build content and stored into per account bucket = " + key;
		} catch (Exception e) {
			return "Failed processing : content build for payment = " + key;
		}
	}
	
	public HashMap<String,ArrayList<String>> BuildEmail (Payment payment,CustomerPreferences prefs) {

		HashMap<String, String> emailTemplate = GetEmailTemplate(payment);
		HashMap<String,ArrayList<String>> emailDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> emailList = new ArrayList<String>(); 
		ArrayList<String> historyList = new ArrayList<String>();
		
        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(payment.getAccountNumber().substring(0,2));
        
		EmailDeliveryHeader emailHeader = new EmailDeliveryHeader();
		
		emailHeader.setAccountNumber(payment.getAccountNumber());
		emailHeader.setAccountNickname("");
		emailHeader.setChannelMemberID(opcoDetails.getChannelID());
		emailHeader.setCity(prefs.getCustomerInfo().getCity());
		emailHeader.setStreetAddress1(prefs.getCustomerInfo().getStreetAddress()+"***");
		emailHeader.setESID(payment.getEzid());
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
				emailList.add(payment.getExternalID()+emailHeader.toString()+","+payment.getEmailContent());
				
				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(payment.getAccountNumber());
				bm.setAlertName(payment.getAlertType());
				bm.setEndPoint(cc.getEndPoint());
				bm.setWebID(cc.getWebId());
				bm.setOutageNumber(0);
				bm.setEmailTemplate(emailHeader.getTemplate());
				bm.setEmailSubject(emailHeader.getSubject());
				bm.setMessageTrackId(emailHeader.getHashLink());
				bm.setEmailMetaData("Template:"+bm.getEmailTemplate()+payment.getMacssEmailContent());
				historyList.add(bm.getMacssBuild());			
			}
		}
		if (emailList.size() > 0) {
			String bucketKey = "email_"+payment.getAccountNumber()+"_"+DateTime.now().toString("yyyyMMddHHmmss");
			emailDetails.put("xat", emailList);
			emailDetails.put("history", historyList);
			LoadData2S3 load2S3 = new LoadData2S3();
			load2S3.loadData(System.getenv("NOTIFICATION_EMAIL_BUCKET"), bucketKey, emailList);
			load2S3.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), bucketKey, historyList);
		}
		return emailDetails;
	}
	
	public HashMap<String,ArrayList<String>> BuildSMS (Payment payment,CustomerPreferences prefs) {

		HashMap<String,ArrayList<String>> smsDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> smsList = new ArrayList<String>(); 
		ArrayList<String> historyList = new ArrayList<String>();
		
		DecimalFormat df = new DecimalFormat("$0.00##");
		//Locale locale = new Locale("en", "US");      
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		//String result = df.format(34.4959);
        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(payment.getAccountNumber().substring(0,2));
        HashMap<String, String> smsTemplate = GetSMSTemplate(payment);
        String currency = formatter.format(payment.getPaymentAmount());
        String smsText = smsTemplate.get("SMSText")
        		.replaceAll("\\*OPCOSN", opcoDetails.getAbbreviatedName())
        		.replaceAll("\\*PAYMENT", "\\"+df.format(payment.getPaymentAmount()))
        		.replaceAll("\\*ACCT", payment.getAccountNumber().substring(6, 6+5))
        		.replaceAll("\\*OPCOURL", opcoDetails.getOpcoSite());
        //EmailDeliveryHeader emailHeader = new EmailDeliveryHeader();
        
		for (CustomerContacts cc : prefs.getCustomerContacts()) {
			if(!cc.getEndPoint().contains("@")) {
				
		        smsText = smsText
		        		.replaceAll("\\*ADDRESS", prefs.getCustomerInfo().getStreetAddress()+"***");
				smsList.add("1"+cc.getEndPoint()+"||"+smsText+"~~");
				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(payment.getAccountNumber());
				bm.setAlertName(payment.getAlertType());
				bm.setEndPoint(cc.getEndPoint());
				bm.setWebID(cc.getWebId());
				bm.setOutageNumber(0);
				bm.setMessageTrackId(UUID.randomUUID().toString().replaceAll("-", ""));
				bm.setSmsText(smsText.replaceAll("%26", "&"));
				historyList.add(bm.getMacssBuild());			
			}
		}
        
		if (smsList.size() > 0) {
			String bucketKey = "sms_"+payment.getAccountNumber()+"_"+DateTime.now().toString("yyyyMMddHHmmss");
			LoadData2S3 load2S3 = new LoadData2S3();
			load2S3.loadData(System.getenv("NOTIFICATION_SMS_BUCKET"), bucketKey, smsList);
			load2S3.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), bucketKey, historyList);
			smsDetails.put("sms", smsList);
			smsDetails.put("history",historyList);
		}
		return smsDetails;
	}
	 
	public HashMap<String,String> GetEmailTemplate (Payment payment) {

		HashMap<String,String> emailTemplate = new HashMap<String, String>();
		emailTemplate.put("Template", "PaymentReceived");
		emailTemplate.put("Subject", "*OPCOSN has Received Your Payment.");
		return emailTemplate;
	}
	
	public HashMap<String,String> GetSMSTemplate (Payment payment) {

		HashMap<String,String> smsTemplate = new HashMap<String, String>();
		smsTemplate.put("SMSText", "*OPCOSN has received your payment of *PAYMENT for your account ending in *ACCT at *ADDRESS.  Visit: *OPCOURL/account.");                          

		return smsTemplate;
	}
}
