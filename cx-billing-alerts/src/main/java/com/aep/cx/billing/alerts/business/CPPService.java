package com.aep.cx.billing.alerts.business;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
//import java.time.format.*;

import org.joda.time.DateTime;
import org.joda.time.format.*;

import com.aep.cx.billing.events.CPPAlert;
import com.aep.cx.preferences.dao.BillingPreferencesDAO;
import com.aep.cx.preferences.dao.CustomerContacts;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.aep.cx.utils.alerts.notification.msessages.BuildMessageHistory;
import com.aep.cx.utils.alerts.notification.msessages.EmailDeliveryHeader;
import com.aep.cx.utils.enums.PreferencesTypes;
import com.aep.cx.utils.opco.OperatingCompanyManager;
import com.aep.cx.utils.opco.OperatingCompanyV2;

public class CPPService {
	
	CustomerPreferences prefs = null;
	BillingPreferencesDAO dao = null;
	String bucketKey = null;

	public String buildCPPContent (ArrayList<CPPAlert> cppList,String key) {
		bucketKey = key;
		try {
		dao = new BillingPreferencesDAO();
		
		for (CPPAlert cpp : cppList) {
			prefs = dao.getCustomerPreferences(cpp.getAccountNumber(), PreferencesTypes.BILLINGPAYMENT.toString());
		
			System.out.println("preferences="+prefs);
			//System.out.println()
			if (null != prefs && prefs.getCustomerContacts().size() > 0) {
				buildEmail(cpp,prefs);
				buildSMS(cpp,prefs);
			}
		}
		return "Successfully processed : build content and stored into per account bucket="+key;
		} catch (Exception e) {
			return "Failed processing : content build for payment="+key;
		}
	}
	
	public void buildEmail (CPPAlert cpp, CustomerPreferences prefs) {

		HashMap<String, String> emailTemplate = getEmailTemplate(cpp);
		HashMap<String,ArrayList<String>> emailDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> emailList = new ArrayList<String>(); 
		ArrayList<String> historyList = new ArrayList<String>();
		
        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(cpp.getAccountNumber().substring(0,2));
        
		EmailDeliveryHeader emailHeader = new EmailDeliveryHeader();
		
		emailHeader.setAccountNumber(cpp.getAccountNumber());
		emailHeader.setAccountNickname("");
		emailHeader.setChannelMemberID(opcoDetails.getChannelID());
		emailHeader.setCity(prefs.getCustomerInfo().getCity());
		emailHeader.setStreetAddress1(prefs.getCustomerInfo().getStreetAddress()+"***");
		emailHeader.setESID(cpp.getEzid());
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
				emailList.add(cpp.getExternalID()+emailHeader.toString()+","+cpp.getEmailContent());
				
				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(cpp.getAccountNumber());
				bm.setAlertName(cpp.getAlertType());
				bm.setEndPoint(cc.getEndPoint());
				bm.setWebID(cc.getWebId());
				bm.setOutageNumber(0);
				bm.setEmailTemplate(emailHeader.getTemplate());
				bm.setEmailSubject(emailHeader.getSubject());
				bm.setMessageTrackId(emailHeader.getHashLink());
				bm.setEmailMetaData("Template:"+bm.getEmailTemplate()+cpp.getMacssEmailContent());
				historyList.add(bm.getMacssBuild());			
			}
		}
		if (emailList.size() > 0) {
			String bucketKey = "email_"+cpp.getAccountNumber()+"_"+DateTime.now().toString("yyyyMMddHHmmss");
			emailDetails.put("xat", emailList);
			emailDetails.put("history", historyList);
			LoadData2S3 load2S3 = new LoadData2S3();
			System.out.println("result from CPPService for email: " + load2S3.loadData(System.getenv("NOTIFICATION_EMAIL_BUCKET"), bucketKey, emailList));
			load2S3.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), bucketKey, historyList);
		}
		
	}
	
	public void buildSMS(CPPAlert cpp, CustomerPreferences prefs) {

		HashMap<String,ArrayList<String>> smsDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> smsList = new ArrayList<String>(); 
		ArrayList<String> historyList = new ArrayList<String>();
		
		//Locale locale = new Locale("en", "US");      
		//String result = df.format(34.4959);
        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(cpp.getAccountNumber().substring(0,2));
        
        HashMap<String, String> smsTemplate = getSMSTemplate(cpp, opcoDetails);
        //String currency = formatter.format(billDue.getPaymentAmount());
        String smsText = smsTemplate.get("SMSText")
        		.replaceAll("\\*OPCOSN", opcoDetails.getAbbreviatedName())
        		//.replaceAll("\\*PAYMENT", "\\"+df.format(billDue.getPaymentAmount()))
        		.replaceAll("\\*ACCT", cpp.getAccountNumber().substring(6, 6+5))
        		.replaceAll("\\*OPCOURL", opcoDetails.getOpcoSite());
        //EmailDeliveryHeader emailHeader = new EmailDeliveryHeader();
        
		for (CustomerContacts cc : prefs.getCustomerContacts()) {
			if(!cc.getEndPoint().contains("@")) {
				
		        smsText = smsText
		        		.replaceAll("\\*ADDRESS", prefs.getCustomerInfo().getStreetAddress()+"***");
				smsList.add("1"+cc.getEndPoint()+"||"+smsText+"~~");
				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(cpp.getAccountNumber());
				bm.setAlertName(cpp.getAlertType());
				bm.setEndPoint(cc.getEndPoint());
				bm.setWebID(cc.getWebId());
				bm.setOutageNumber(0);
				bm.setMessageTrackId(UUID.randomUUID().toString().replaceAll("-", ""));
				bm.setSmsText(smsText.replaceAll("%26", "&"));
				historyList.add(bm.getMacssBuild());			
			}
		}
        
		if (smsList.size() > 0) {
			String bucketKey = "sms_"+cpp.getAccountNumber()+"_"+DateTime.now().toString("yyyyMMddHHmmss");
			LoadData2S3 load2S3 = new LoadData2S3();
			System.out.println("result from CPPService for text: " + load2S3.loadData(System.getenv("NOTIFICATION_SMS_BUCKET"), bucketKey, smsList));
			load2S3.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), bucketKey, historyList);
			smsDetails.put("sms", smsList);
			smsDetails.put("history",historyList);
		}
		
	}
	 
	public HashMap<String,String> getEmailTemplate(CPPAlert cpp) {

        HashMap<String,String> emailTemplate = new HashMap<String, String>();
        
		OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(cpp.getAccountNumber().substring(0,2));
		
		if (cpp.getAlertType().toLowerCase().equals("cpp-cancel"))
		{
			emailTemplate.put("Template", "AutoPayUnenroll");
			emailTemplate.put("Subject", "Unenrolled from AEP AutoPay");
		}
		else if (cpp.getAlertType().toLowerCase().equals("cpp-update"))
		{
			emailTemplate.put("Template", "AutoPayUpdated");
			emailTemplate.put("Subject", "AutoPay information was updated.");
		}
		else if (cpp.getAlertType().toLowerCase().equals("cpp-enrollment") || cpp.getAlertType().toLowerCase().equals("cpp-acknowledge"))
		{

			if (cpp.getPrenoteStatusCode().toLowerCase().equals("f"))
        	{
		    	emailTemplate.put("Template", "AutoPayActivationFail");
            	emailTemplate.put("Subject", "Trouble enrolling in AEP Autopay");
        	}
        	else if (cpp.getToBeCurrNext().toLowerCase().equals("c"))
        	{
            	emailTemplate.put("Template", "AutoPayCurrentBill");
            	emailTemplate.put("Subject", "Welcome to AEP AutoPay");
        	}
        	else if (cpp.getToBeCurrNext().toLowerCase().equals("n") && cpp.getRemainingAmount() > 0)
        	{
            	emailTemplate.put("Template", "AutoPayNextBillWithDues");
           		emailTemplate.put("Subject", "Welcome to AEP AutoPay");
        	}
        	else
        	{
            emailTemplate.put("Template", "AutoPayNextBill");
            emailTemplate.put("Subject", "Welcome to AEP AutoPay");
			}
		}
		else
		{
			System.out.println("Not a valid CPP alert");
			throw new IllegalArgumentException("Not a valid CPP alert");
		}

		return emailTemplate;
	}
        
	/*
	 * if (cpp.getToBeCurrNext().toLowerCase().equals("f")) {
	 * emailTemplate.put("Template", "AutoPayActivationFail");
	 * emailTemplate.put("Subject", "Trouble enrolling in AEP Autopay"); } else if
	 * (cpp.getToBeCurrNext().toLowerCase().equals("c")) {
	 * emailTemplate.put("Template", "AutoPayCurrentBill");
	 * emailTemplate.put("Subject", "Welcome to AEP AutoPay"); } else if
	 * (cpp.getToBeCurrNext().toLowerCase().equals("n") &&
	 * cpp.getRemainingAmount() > 0) { emailTemplate.put("Template",
	 * "AutoPayNextBillWithDues"); emailTemplate.put("Subject",
	 * "Welcome to AEP AutoPay"); } else { emailTemplate.put("Template",
	 * "AutoPayNextBill"); emailTemplate.put("Subject", "Welcome to AEP AutoPay"); }
	 * 
	 * return emailTemplate;
	 *
	}*/
	
	public HashMap<String,String> getSMSTemplate(CPPAlert cpp, OperatingCompanyV2 opcoDetails) {
		System.out.println("CPPService creating sms");
		System.out.println("Alert type is " + cpp.getAlertType());
        NumberFormat format = NumberFormat.getCurrencyInstance();

        DateTimeFormatter fmtToString = DateTimeFormat.forPattern("M/d");

        HashMap<String,String> smsTemplate = new HashMap<String, String>();

        StringBuilder sms = new StringBuilder("");
//AutoPay bank account information updated for *ADDRESS and will be used to pay all current and future bills.");


		if (cpp.getAlertType().toLowerCase().equals("cpp-update"))
		{
			sms.append("AutoPay bank account information updated for *ADDRESS and will be used to pay all current and future bills.");
		}
		else if (cpp.getAlertType().toLowerCase().equals("cpp-cancel"))
		{
			sms.append("*ADDRESS has been unenrolled from AutoPay. You are responsible for paying any current or future bills by the due date.");
		}
        else if (cpp.getPrenoteStatusCode().toLowerCase().equals("f"))
        {
		    sms.append("Unsuccessful validation of AutoPay banking information for *ADDRESS. You can clarify with your bank and sign up for AutoPay again.");
        }
        else if (cpp.getToBeCurrNext().toLowerCase().equals("c"))
        {
            sms.append("Welcome to AEP AutoPay. Bills for *ADDRESS will be paid automatically. AutoPay will withdraw ");
            sms.append(format.format(cpp.getRemainingAmount()));
            sms.append(" on ");
			sms.append(fmtToString.print(cpp.getDueDate()));
			sms.append(" to pay your bill due on that day.");

        }
        else if (cpp.getToBeCurrNext().toLowerCase().equals("n") && cpp.getRemainingAmount() > 0)
        {
            sms.append("Welcome to AEP AutoPay. AutoPay for *ADDRESS begins after ");
            sms.append(fmtToString.print(cpp.getNextBillDate()));
            sms.append(" read. You must pay your current bill before ");
            sms.append(fmtToString.print(cpp.getDueDate()));
			sms.append(". ");
			sms.append(opcoDetails.getOpcoSite());
            sms.append(opcoDetails.getPayMyBillUrl());

        }
        else
        {
            sms.append("Welcome to AEP AutoPay. Your bills for *ADDRESS will be paid automatically. See bill after meter reading on ");
            sms.append(fmtToString.print(cpp.getNextBillDate()));
            sms.append(" for first withdrawal date and amount.");
        }

		smsTemplate.put("SMSText", sms.toString());

		return smsTemplate;
	}
}
