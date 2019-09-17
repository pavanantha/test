package com.aep.cx.billing.alerts.business;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
//import java.time.format.*;

import org.joda.time.DateTime;
import org.joda.time.format.*;

import com.aep.cx.billing.events.BillDue;
import com.aep.cx.billing.events.ReturnCheck;
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

public class ReturnCheckService {
	
	static CustomerPreferences prefs = null;
	static BillingPreferencesDAO dao = null;
	static String bucketKey = null;

	public static String BuildReturnCheckContent (ArrayList<ReturnCheck> returnCheckList,String key) {
		bucketKey = key;
		try {
		dao = new BillingPreferencesDAO();
		ReturnCheckService ps = new ReturnCheckService();
		for (ReturnCheck returnCheck : returnCheckList) {
			prefs = dao.getCustomerPreferences(returnCheck.getAccountNumber(), PreferencesTypes.BILLINGPAYMENT.toString());
		
			System.out.println("prefereces="+prefs);
			if (null != prefs && prefs.getCustomerContacts().size() > 0) {
				HashMap<String, ArrayList<String>> emailContent = ps.BuildEmail(returnCheck,prefs);
				HashMap<String, ArrayList<String>> smsContent = ps.BuildSMS(returnCheck,prefs);
			}
		}
		return "Successfully processed : build content and stored into per account bucket="+key;
		} catch (Exception e) {
			return "Failed processing : content build for return check="+key;
		}
	}
	
	public HashMap<String,ArrayList<String>> BuildEmail (ReturnCheck returnCheck,CustomerPreferences prefs) {

		HashMap<String, String> emailTemplate = getEmailTemplate(returnCheck);
		HashMap<String,ArrayList<String>> emailDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> emailList = new ArrayList<String>(); 
		ArrayList<String> historyList = new ArrayList<String>();
		
        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(returnCheck.getAccountNumber().substring(0,2));
        
		EmailDeliveryHeader emailHeader = new EmailDeliveryHeader();
		
		emailHeader.setAccountNumber(returnCheck.getAccountNumber());
		emailHeader.setAccountNickname("");
		emailHeader.setChannelMemberID(opcoDetails.getChannelID());
		emailHeader.setCity(prefs.getCustomerInfo().getCity());
		emailHeader.setStreetAddress1(prefs.getCustomerInfo().getStreetAddress()+"***");
		emailHeader.setESID(returnCheck.getEzid());
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
				emailList.add(returnCheck.getExternalID()+emailHeader.toString()+","+returnCheck.getEmailContent());
				
				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(returnCheck.getAccountNumber());
				bm.setAlertName(returnCheck.getAlertType());
				bm.setEndPoint(cc.getEndPoint());
				bm.setWebID(cc.getWebId());
				bm.setOutageNumber(0);
				bm.setEmailTemplate(emailHeader.getTemplate());
				bm.setEmailSubject(emailHeader.getSubject());
				bm.setMessageTrackId(emailHeader.getHashLink());
				bm.setEmailMetaData("Template:"+bm.getEmailTemplate()+returnCheck.getMacssEmailContent());
				historyList.add(bm.getMacssBuild());			
			}
		}
		if (emailList.size() > 0) {
			String bucketKey = "email_"+returnCheck.getAccountNumber()+"_"+DateTime.now().toString("yyyyMMddHHmmss");
			emailDetails.put("xat", emailList);
			emailDetails.put("history", historyList);
			LoadData2S3 load2S3 = new LoadData2S3();
			load2S3.loadData(System.getenv("NOTIFICATION_EMAIL_BUCKET"), bucketKey, emailList);
			load2S3.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), bucketKey, historyList);
		}
		return emailDetails;
	}
	
	public HashMap<String,ArrayList<String>> BuildSMS(ReturnCheck returnCheck,CustomerPreferences prefs) {

		HashMap<String,ArrayList<String>> smsDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> smsList = new ArrayList<String>(); 
		ArrayList<String> historyList = new ArrayList<String>();
		
        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(returnCheck.getAccountNumber().substring(0,2));
        HashMap<String, String> smsTemplate = getSMSTemplate(returnCheck, opcoDetails);
        //String currency = formatter.format(billDue.getPaymentAmount());
        String smsText = smsTemplate.get("SMSText")
        		.replaceAll("\\*OPCOSN", opcoDetails.getAbbreviatedName())
        		//.replaceAll("\\*PAYMENT", "\\"+df.format(billDue.getPaymentAmount()))
        		.replaceAll("\\*ACCT", returnCheck.getAccountNumber().substring(6, 6+5))
        		.replaceAll("\\*OPCOURL", opcoDetails.getOpcoSite());
        //EmailDeliveryHeader emailHeader = new EmailDeliveryHeader();
        
		for (CustomerContacts cc : prefs.getCustomerContacts()) {
			if(!cc.getEndPoint().contains("@")) {
				
		        smsText = smsText
		        		.replaceAll("\\*ADDRESS", prefs.getCustomerInfo().getStreetAddress()+"***");
				smsList.add("1"+cc.getEndPoint()+"||"+smsText+"~~");
				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(returnCheck.getAccountNumber());
				bm.setAlertName(returnCheck.getAlertType());
				bm.setEndPoint(cc.getEndPoint());
				bm.setWebID(cc.getWebId());
				bm.setOutageNumber(0);
				bm.setMessageTrackId(UUID.randomUUID().toString().replaceAll("-", ""));
				bm.setSmsText(smsText.replaceAll("%26", "&"));
				historyList.add(bm.getMacssBuild());			
			}
		}
        
		if (smsList.size() > 0) {
			String bucketKey = "sms_"+returnCheck.getAccountNumber()+"_"+DateTime.now().toString("yyyyMMddHHmmss");
			LoadData2S3 load2S3 = new LoadData2S3();
			load2S3.loadData(System.getenv("NOTIFICATION_SMS_BUCKET"), bucketKey, smsList);
			load2S3.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), bucketKey, historyList);
			smsDetails.put("sms", smsList);
			smsDetails.put("history",historyList);
		}
		return smsDetails;
	}
	 
	public HashMap<String,String> getEmailTemplate(ReturnCheck returnCheck) {

        HashMap<String,String> emailTemplate = new HashMap<String, String>();
        
		emailTemplate.put("Template", "ReturnPayment");
		emailTemplate.put("Subject", "*OPCOSN Payment Returned");


		return emailTemplate;
	}
	
	public HashMap<String,String> getSMSTemplate(ReturnCheck returnCheck, OperatingCompanyV2 opcoDetails) {

        NumberFormat format = DecimalFormat.getCurrencyInstance();

        //DateTimeFormatter fmtToString = DateTimeFormat.forPattern("M-D-YY");

        HashMap<String,String> smsTemplate = new HashMap<String, String>();

        StringBuilder sms = new StringBuilder("*OPCOSN: Payment for *ADDRESS was returned. Please immediately pay ");
		sms.append(format.format(returnCheck.getPaymentAmount()));
		sms.append(" in cash, certified check or money order. More info: ");
		sms.append(opcoDetails.getOpcoSite());
		sms.append(opcoDetails.getBillAccountURL());
		sms.append(".");
		smsTemplate.put("SMSText", sms.toString());

		return smsTemplate;
	}
}
