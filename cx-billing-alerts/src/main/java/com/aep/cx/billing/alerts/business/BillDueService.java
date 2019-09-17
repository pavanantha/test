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

public class BillDueService {
	
	static CustomerPreferences prefs = null;
	static BillingPreferencesDAO dao = null;
	static String bucketKey = null;

	public static String BuildBillDueContent (ArrayList<BillDue> billDueList,String key) {
		bucketKey = key;
		try {
		dao = new BillingPreferencesDAO();
		BillDueService ps = new BillDueService();
		for (BillDue billDue : billDueList) {
			prefs = dao.getCustomerPreferences(billDue.getAccountNumber(), PreferencesTypes.BILLINGPAYMENT.toString());
		
			System.out.println("prefereces="+prefs);
			if (null != prefs && prefs.getCustomerContacts().size() > 0) {
				HashMap<String, ArrayList<String>> emailContent = ps.BuildEmail(billDue,prefs);
				HashMap<String, ArrayList<String>> smsContent = ps.BuildSMS(billDue,prefs);
			}
		}
		return "Successfully processed : build content and stored into per account bucket="+key;
		} catch (Exception e) {
			return "Failed processing : content build for payment="+key;
		}
	}
	
	public HashMap<String,ArrayList<String>> BuildEmail (BillDue billDue,CustomerPreferences prefs) {

		HashMap<String, String> emailTemplate = GetEmailTemplate(billDue);
		HashMap<String,ArrayList<String>> emailDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> emailList = new ArrayList<String>(); 
		ArrayList<String> historyList = new ArrayList<String>();
		
        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(billDue.getAccountNumber().substring(0,2));
        
		EmailDeliveryHeader emailHeader = new EmailDeliveryHeader();
		
		emailHeader.setAccountNumber(billDue.getAccountNumber());
		emailHeader.setAccountNickname("");
		emailHeader.setChannelMemberID(opcoDetails.getChannelID());
		emailHeader.setCity(prefs.getCustomerInfo().getCity());
		emailHeader.setStreetAddress1(prefs.getCustomerInfo().getStreetAddress()+"***");
		emailHeader.setESID(billDue.getEzid());
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
				emailList.add(billDue.getExternalID()+emailHeader.toString()+","+billDue.getEmailContent());
				
				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(billDue.getAccountNumber());
				bm.setAlertName(billDue.getAlertType());
				bm.setEndPoint(cc.getEndPoint());
				bm.setWebID(cc.getWebId());
				bm.setOutageNumber(0);
				bm.setEmailTemplate(emailHeader.getTemplate());
				bm.setEmailSubject(emailHeader.getSubject());
				bm.setMessageTrackId(emailHeader.getHashLink());
				bm.setEmailMetaData("Template:"+bm.getEmailTemplate()+billDue.getMacssEmailContent());
				historyList.add(bm.getMacssBuild());			
			}
		}
		if (emailList.size() > 0) {
			String bucketKey = "email_"+billDue.getAccountNumber()+"_"+DateTime.now().toString("yyyyMMddHHmmss");
			emailDetails.put("xat", emailList);
			emailDetails.put("history", historyList);
			LoadData2S3 load2S3 = new LoadData2S3();
			load2S3.loadData(System.getenv("NOTIFICATION_EMAIL_BUCKET"), bucketKey, emailList);
			load2S3.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), bucketKey, historyList);
		}
		return emailDetails;
	}
	
	public HashMap<String,ArrayList<String>> BuildSMS (BillDue billDue,CustomerPreferences prefs) {

		HashMap<String,ArrayList<String>> smsDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> smsList = new ArrayList<String>(); 
		ArrayList<String> historyList = new ArrayList<String>();
		
        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(billDue.getAccountNumber().substring(0,2));
		System.out.println("starting the content");
		HashMap<String, String> smsTemplate = GetSMSTemplate(billDue);
        //String currency = formatter.format(billDue.getPaymentAmount());
        String smsText = smsTemplate.get("SMSText")
        		.replaceAll("\\*OPCOSN", opcoDetails.getAbbreviatedName())
        		//.replaceAll("\\*PAYMENT", "\\"+df.format(billDue.getPaymentAmount()))
        		.replaceAll("\\*ACCT", billDue.getAccountNumber().substring(6, 6+5))
        		.replaceAll("\\*OPCOURL", opcoDetails.getOpcoSite());
        //EmailDeliveryHeader emailHeader = new EmailDeliveryHeader();
        
		for (CustomerContacts cc : prefs.getCustomerContacts()) {
			if(!cc.getEndPoint().contains("@")) {
				
		        smsText = smsText
		        		.replaceAll("\\*ADDRESS", prefs.getCustomerInfo().getStreetAddress()+"***");
				smsList.add("1"+cc.getEndPoint()+"||"+smsText+"~~");
				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(billDue.getAccountNumber());
				bm.setAlertName(billDue.getAlertType());
				bm.setEndPoint(cc.getEndPoint());
				bm.setWebID(cc.getWebId());
				bm.setOutageNumber(0);
				bm.setMessageTrackId(UUID.randomUUID().toString().replaceAll("-", ""));
				bm.setSmsText(smsText.replaceAll("%26", "&"));
				historyList.add(bm.getMacssBuild());			
			}
		}
        
		if (smsList.size() > 0) {
			String bucketKey = "sms_"+billDue.getAccountNumber()+"_"+DateTime.now().toString("yyyyMMddHHmmss");
			LoadData2S3 load2S3 = new LoadData2S3();
			load2S3.loadData(System.getenv("NOTIFICATION_SMS_BUCKET"), bucketKey, smsList);
			load2S3.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), bucketKey, historyList);
			smsDetails.put("sms", smsList);
			smsDetails.put("history",historyList);
		}
		return smsDetails;
	}
	 
	public HashMap<String,String> GetEmailTemplate(BillDue billDue) {

		System.out.println("starting the email content");
        HashMap<String,String> emailTemplate = new HashMap<String, String>();
        
        if (billDue.getCPPCustomer().toLowerCase().equals("y"))
        {
		    emailTemplate.put("Template", "CPPDueDateApproaching");
            emailTemplate.put("Subject", "Your *OPCOSN payment is scheduled");
        }
        else
        {
            emailTemplate.put("Template", "NonCPPDueDateApproaching");
            emailTemplate.put("Subject", "Due Date Approaching for Your *OPCOSN Electric Bill");
        }


		return emailTemplate;
	}
	
	public HashMap<String,String> GetSMSTemplate(BillDue billDue) {
        NumberFormat format = DecimalFormat.getCurrencyInstance();

        DateTimeFormatter fmtToString = DateTimeFormat.forPattern("M/d/YY");

        HashMap<String,String> smsTemplate = new HashMap<String, String>();

        StringBuilder sms = new StringBuilder("*OPCOSN: ");

        if (billDue.getCPPCustomer().toLowerCase().equals("y"))
        {
            sms.append("Your payment of ");
			sms.append(format.format(billDue.getBillAmount()));
			sms.append(" for *ADDRESS will be deducted on ");
            sms.append(fmtToString.print(billDue.getBillDueDate()));
            sms.append(". Total balance: ");
            sms.append(format.format(billDue.getTotalAmount()));
            sms.append(". *OPCOURL/account");
            
        }
        else
        {
            double adjustedDue = billDue.getTotalAmount() - billDue.getPendingPaymentAmount();
            double adjustedPastDue = billDue.getPastDueAmount() - billDue.getPendingPaymentAmount();

            
            if (billDue.getPastDueAmount() > billDue.getPendingPaymentAmount())
            {
				//adjustedDue += adjustedPastDue;

                sms.append("Remaining amount due is ");

                sms.append(format.format(adjustedDue));
                sms.append(" of which ");
                sms.append(format.format(adjustedPastDue));
                sms.append(" is past due for service at *ADDRESS. *OPCOURL/pay");
                
            }
            else
            {
                sms.append("Your balance of ");
                sms.append(format.format(adjustedDue));
                sms.append(" is due on ");
                sms.append(fmtToString.print(billDue.getBillDueDate()));
                sms.append(" for service at *ADDRESS. *OPCOURL/pay");
            }
        }

		smsTemplate.put("SMSText", sms.toString());

		return smsTemplate;
	}
}
