package com.aep.cx.billing.alerts.lambda.handlers;

import com.aep.cx.billing.alerts.business.*;
import java.io.IOException;
import java.util.ArrayList;
import com.aep.cx.utils.opco.*;

import java.io.*;
import java.text.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.junit.Assert;
// import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.billing.events.BillingAlerts;
import com.aep.cx.billing.events.CPPAlert;
import org.joda.time.DateTime;
import com.aep.cx.billing.events.Payment;
import com.aep.cx.billing.events.ReturnCheck;


/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class BuildingAlertContentTest {


    static final Logger logger = LogManager.getLogger(BuildingAlertContentTest.class);


 //   @Test
    public void testReturnCheckContentCreation() {
        ReturnCheckService returnCheckService = new ReturnCheckService();

        ReturnCheck returnCheck = new ReturnCheck();
        returnCheck.setAccountNumber("10000000123");
        returnCheck.setAlertType("RETURN-CHECK");
        returnCheck.setEndPoint("aerickson@aep.com");
        returnCheck.setPaymentAmount(23.23);
        returnCheck.setProcessFee(34.34);
        returnCheck.setReason("Wrong information for check");
        returnCheck.setExternalID("XATPYM");
        returnCheck.setMacssID("MCSPYM");


        HashMap<String,String> returnCheckInfo = returnCheckService.getEmailTemplate(returnCheck);
        Assert.assertEquals(returnCheckInfo.get("Template"), "ReturnPayment");
        Assert.assertEquals(returnCheckInfo.get("Subject"), "*OPCOSN Payment Returned");

        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(returnCheck.getAccountNumber().substring(0,2));
        
        HashMap<String, String> smsInfo = returnCheckService.getSMSTemplate(returnCheck, opcoDetails);
        String textMessage = smsInfo.get("SMSText");
        Assert.assertTrue(textMessage.contains(" in cash, certified check or money order. More info: "));
        Assert.assertTrue(textMessage.contains("$23.23"));
        Assert.assertTrue(textMessage.contains("/account/bills/"));


        //import com.aep.cx.preferences.dao.*
        com.aep.cx.preferences.dao.CustomerContacts contactEmail = new com.aep.cx.preferences.dao.CustomerContacts();
        contactEmail.setEndPoint("aerickson@aep.com");
        contactEmail.setWebId("aaron1");

        com.aep.cx.preferences.dao.CustomerContacts contactText = new com.aep.cx.preferences.dao.CustomerContacts();
        contactText.setEndPoint("5672220987");
        contactText.setWebId("aaron1");

        com.aep.cx.preferences.dao.CustomerInfo custInfo = new com.aep.cx.preferences.dao.CustomerInfo();
        custInfo.setAccountNumber(returnCheck.getAccountNumber());
        custInfo.setCity("Some City");
        custInfo.setName("Aaron");
        custInfo.setPremiseNumber(returnCheck.getPremiseNumber());
        custInfo.setState("Ohio");
        custInfo.setStreetAddress("123 Beeler");
        custInfo.setZipCode("43040");
        
        List<com.aep.cx.preferences.dao.CustomerContacts> cl = new ArrayList<>();
        cl.add(contactText);
        cl.add(contactEmail);
        com.aep.cx.preferences.dao.CustomerPreferences prefs = new com.aep.cx.preferences.dao.CustomerPreferences();
        prefs.setCustomerContacts(cl);
        prefs.setCustomerInfo(custInfo);


        HashMap<String,ArrayList<String>> sms = returnCheckService.BuildSMS(returnCheck, prefs);
        ArrayList<String> finalTextMessage = sms.get("sms");

        HashMap<String,ArrayList<String>> email = returnCheckService.BuildEmail(returnCheck, prefs);
        ArrayList<String> finalEmail = email.get("xat");

        //Assert.assertEquals(true, finalTextMessage.contains("Ohio"));


    }

    @Test
    public void testCPPContentCreation() {
        CPPService cppService = new CPPService();

        CPPAlert cppEnrollmentCurrentBill = new CPPAlert();
        cppEnrollmentCurrentBill.setAccountNumber("10000000123");
        cppEnrollmentCurrentBill.setAlertType("CPP-ENROLLMENT");
        cppEnrollmentCurrentBill.setEndPoint("aerickson@aep.com");
        cppEnrollmentCurrentBill.setBankNumber("9887");
        cppEnrollmentCurrentBill.setToBeCurrNext("C");
        cppEnrollmentCurrentBill.setDueDate(DateTime.now().plusDays(2));
        cppEnrollmentCurrentBill.setNextBillDate(DateTime.now().plusDays(31));
        cppEnrollmentCurrentBill.setRemainingAmount(33.33);
        cppEnrollmentCurrentBill.setPrenoteStatusCode("");
        
        //cppAlert.getNextBillDate(DateTime

        HashMap<String,String> emailInfo = cppService.getEmailTemplate(cppEnrollmentCurrentBill);
        Assert.assertEquals(emailInfo.get("Template"), "AutoPayCurrentBill");
        Assert.assertEquals(emailInfo.get("Subject"), "Welcome to AEP AutoPay");

        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(cppEnrollmentCurrentBill.getAccountNumber().substring(0,2));
        
        HashMap<String, String> smsInfo = cppService.getSMSTemplate(cppEnrollmentCurrentBill, opcoDetails);
        String textMessage = smsInfo.get("SMSText");


        Assert.assertTrue(textMessage.contains("Welcome to AEP AutoPay. Bills for *ADDRESS will be paid automatically. AutoPay will withdraw "));
        Assert.assertTrue(textMessage.contains(" to pay your bill due on that day."));
                                                
        CPPAlert cppEnrollmentNextBillWithDues = new CPPAlert();
        cppEnrollmentNextBillWithDues.setAccountNumber("10000000123");
        cppEnrollmentNextBillWithDues.setAlertType("CPP-ENROLLMENT");
        cppEnrollmentNextBillWithDues.setEndPoint("aerickson@aep.com");
        cppEnrollmentNextBillWithDues.setBankNumber("9887");
        cppEnrollmentNextBillWithDues.setToBeCurrNext("N");
        cppEnrollmentNextBillWithDues.setDueDate(DateTime.now().plusDays(2));
        cppEnrollmentNextBillWithDues.setNextBillDate(DateTime.now().plusDays(31));
        cppEnrollmentNextBillWithDues.setRemainingAmount(33.33);
        cppEnrollmentNextBillWithDues.setPrenoteStatusCode("");

        HashMap<String,String> emailInfo4 = cppService.getEmailTemplate(cppEnrollmentNextBillWithDues);
        Assert.assertEquals(emailInfo4.get("Template"), "AutoPayNextBillWithDues");
        Assert.assertEquals(emailInfo4.get("Subject"), "Welcome to AEP AutoPay");

        HashMap<String, String> smsInfo4 = cppService.getSMSTemplate(cppEnrollmentNextBillWithDues, opcoDetails);
        textMessage = smsInfo4.get("SMSText");
        Assert.assertTrue(textMessage.contains("Welcome to AEP AutoPay. AutoPay for *ADDRESS begins after "));
        Assert.assertTrue(textMessage.contains(" read. You must pay your current bill before "));


        CPPAlert cppEnrollmentNextBill = new CPPAlert();
        cppEnrollmentNextBill.setAccountNumber("10000000123");
        cppEnrollmentNextBill.setAlertType("CPP-ENROLLMENT");
        cppEnrollmentNextBill.setEndPoint("aerickson@aep.com");
        cppEnrollmentNextBill.setBankNumber("9887");
        cppEnrollmentNextBill.setToBeCurrNext("N");
        cppEnrollmentNextBill.setDueDate(DateTime.now().plusDays(2));
        cppEnrollmentNextBill.setNextBillDate(DateTime.now().plusDays(31));
        cppEnrollmentNextBill.setRemainingAmount(0);
        cppEnrollmentNextBill.setPrenoteStatusCode("");

        HashMap<String,String> emailInfo5 = cppService.getEmailTemplate(cppEnrollmentNextBill);
        Assert.assertEquals(emailInfo5.get("Template"), "AutoPayNextBill");
        Assert.assertEquals(emailInfo5.get("Subject"), "Welcome to AEP AutoPay");

        HashMap<String, String> smsInfo5 = cppService.getSMSTemplate(cppEnrollmentNextBill, opcoDetails);
        textMessage = smsInfo5.get("SMSText");
        Assert.assertTrue(textMessage.contains("Welcome to AEP AutoPay. Your bills for *ADDRESS will be paid automatically. See bill after meter reading on "));
        Assert.assertTrue(textMessage.contains(" for first withdrawal date and amount."));


        
        CPPAlert cppAcknowledge = new CPPAlert();
        cppAcknowledge.setAccountNumber("10000000123");
        cppAcknowledge.setAlertType("CPP-ACKNOWLEDGE");
        cppAcknowledge.setEndPoint("aerickson@aep.com");
        cppAcknowledge.setBankNumber("9227");
        cppAcknowledge.setToBeCurrNext("N");
        cppAcknowledge.setDueDate(DateTime.now().plusDays(2));
        cppAcknowledge.setNextBillDate(DateTime.now().plusDays(31));
        cppAcknowledge.setRemainingAmount(33.33);
        cppAcknowledge.setPrenoteStatusCode("F");

        HashMap<String,String> emailInfo2 = cppService.getEmailTemplate(cppAcknowledge);
        Assert.assertEquals(emailInfo2.get("Template"), "AutoPayActivationFail");
        Assert.assertEquals(emailInfo2.get("Subject"), "Trouble enrolling in AEP Autopay");

        
        HashMap<String, String> smsInfo2 = cppService.getSMSTemplate(cppAcknowledge, opcoDetails);
        textMessage = smsInfo2.get("SMSText");
        Assert.assertTrue(textMessage.contains("Unsuccessful validation of AutoPay banking information for *ADDRESS. You can clarify with your bank and sign up for AutoPay again."));

        CPPAlert cppUpdate = new CPPAlert();
        cppUpdate.setAccountNumber("10000000123");
        cppUpdate.setAlertType("CPP-UPDATE");
        cppUpdate.setEndPoint("aerickson@aep.com");
        cppUpdate.setBankNumber("9287");
        //cppUpdate.setToBeCurrNext("N");
        //cppUpdate.setDueDate(DateTime.now().plusDays(2));
        //cppUpdate.setNextBillDate(DateTime.now().plusDays(31));
        //cppUpdate.setRemainingAmount(33.33);
        cppUpdate.setPrenoteStatusCode("");

        HashMap<String,String> emailInfo3 = cppService.getEmailTemplate(cppUpdate);
        Assert.assertEquals(emailInfo3.get("Template"), "AutoPayUpdated");
        Assert.assertEquals(emailInfo3.get("Subject"), "AutoPay information was updated.");

        
        HashMap<String, String> smsInfo3 = cppService.getSMSTemplate(cppUpdate, opcoDetails);
        textMessage = smsInfo3.get("SMSText");
        Assert.assertTrue(textMessage.contains("AutoPay bank account information updated for *ADDRESS and will be used to pay all current and future bills."));


        CPPAlert cppCancel = new CPPAlert();
        cppCancel.setAccountNumber("10000000123");
        cppCancel.setAlertType("CPP-CANCEL");
        cppCancel.setEndPoint("aerickson@aep.com");
        cppCancel.setBankNumber("9887");
        cppCancel.setToBeCurrNext("N");
        cppCancel.setDueDate(DateTime.now().plusDays(2));
        cppCancel.setNextBillDate(DateTime.now().plusDays(31));
        cppCancel.setRemainingAmount(33.33);
        cppCancel.setPrenoteStatusCode("");

        HashMap<String,String> emailInfo6 = cppService.getEmailTemplate(cppCancel);
        Assert.assertEquals(emailInfo6.get("Template"), "AutoPayUnenroll");
        Assert.assertEquals(emailInfo6.get("Subject"), "Unenrolled from AEP AutoPay");

        
        HashMap<String, String> smsInfo7 = cppService.getSMSTemplate(cppCancel, opcoDetails);
        textMessage = smsInfo7.get("SMSText");
        Assert.assertTrue(textMessage.contains("*ADDRESS has been unenrolled from AutoPay. You are responsible for paying any current or future bills by the due date."));

        //writeAFile(cppEnrollmentCurrentBill.getEmailContent());
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
