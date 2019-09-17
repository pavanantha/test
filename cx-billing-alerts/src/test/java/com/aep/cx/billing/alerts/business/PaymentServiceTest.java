package com.aep.cx.billing.alerts.business;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import org.apache.logging.log4j.LogManager;
import java.io.*;
import org.apache.logging.log4j.Logger;

import com.aep.cx.billing.alerts.business.BillDueService;
import com.aep.cx.billing.alerts.business.PaymentService;
import com.aep.cx.billing.events.BillDue;
import com.aep.cx.billing.events.Payment;
import com.aep.cx.preferences.dao.CustomerPreference;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aep.cx.preferences.dao.*;

class PaymentServiceTest {

    static final Logger logger = LogManager.getLogger(PaymentServiceTest.class);


	@Test
	void test() {
		try {
			String prefix = "20190803";
			String key = "payment_102108";
			ObjectMapper mapper = new ObjectMapper();
			ProfileCredentialsProvider profile = new ProfileCredentialsProvider("customer_dev");
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(profile).build();
			InputStream is = LoadData2S3.getObject("dev-alerts-billing-payment-e1", prefix, key, s3Client);
			ArrayList<Payment> payList = mapper.readValue(is,new TypeReference<ArrayList<Payment>>() {});
			String result = PaymentService.BuildPaymentContent(payList,prefix+"/"+key);
			System.out.println("payment processed ="+result);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SdkClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void testBillDue()
	{
		BillDue billDue = new BillDue();
		billDue.setCPPCustomer("y");
		billDue.setBillAmount(100);
		billDue.setBillDueDate(DateTime.now());
		billDue.setBudgetBillAniversary("b");
		billDue.setPendingPaymentAmount(65);
		billDue.setPastDueAmount(10);
		billDue.setTotalAmount(80);

		HashMap<String,String> emailTemplate = new HashMap<String, String>();
		HashMap<String,String> smsTemplate = new HashMap<String, String>();

		BillDueService billdueservice = new BillDueService();
		emailTemplate = billdueservice.GetEmailTemplate(billDue);

		smsTemplate = billdueservice.GetSMSTemplate(billDue);

		//CustomerContacts contacts = new CustomerContacts();
		//contacts.setEndPoint("aerickson@aep.com");
		//contacts

		//CustomerPreferences prefs = new CustomerPreferences();
		//prefs.setCustomerContacts(new List<CustomerContacts>() {});

		//billdueservice.BuildSMS(billDue, prefs);

		//writeAFile("EmailTemplate: " + emailTemplate + "/n" + "SMSTemplate: " + smsTemplate);

// for (CustomerContacts cc : prefs.getCustomerContacts()) {
// 			if(!cc.getEndPoint().contains("@")) {
				
// 		        smsText = smsText
// 		        		.replaceAll("\\*ADDRESS", prefs.getCustomerInfo().getStreetAddress()+"***");
// 				smsList.add("1"+cc.getEndPoint()+"||"+smsText+"~~");
// 				BuildMessageHistory bm = new BuildMessageHistory();
// 				bm.setAccountNumber(billDue.getAccountNumber());
// 				bm.setAlertName(billDue.getAlertType());
// 				bm.setEndPoint(cc.getEndPoint());
// 				bm.setWebID(cc.getWebId());
// 				bm.setOutageNumber(0);
// 				bm.setMessageTrackId(UUID.randomUUID().toString().replaceAll("-", ""));
// 				bm.setSmsText(smsText.replaceAll("%26", "&"));
// 				historyList.add(bm.getMacssBuild());			
// 			}
// 		}
		
	}

	public void writeAFile(String content)
    {
        logger.info("Writing file for debugging purposes MyFile.txt...");

        FileWriter writer = null;
        logger.info("Writing content to MyFile.txt...");

        try {
        writer = new FileWriter("MyFile.txt", true);
        

        writer.write(content);
        } catch (Exception e)
        {
            logger.info("Error writing file: + " + e.getMessage());
        }
        finally
        {
            try {
        writer.close();
            } catch (Exception e)
            {
                
            }
        }
    }

}
