package com.aep.cx.billing.alerts.lambda.handlers;

import java.io.IOException;

import java.io.*;
import java.text.*;
import org.junit.Assert;
import org.junit.Test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.billing.alerts.business.*;

import com.aep.cx.billing.events.BillingAlerts;

import org.joda.time.DateTime;
import org.joda.time.base.AbstractDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.aep.cx.billing.events.Payment;
import com.aep.cx.load.billing.alerts.service.Load2S3BillingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.aep.cx.utils.opco.*;
/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class EndToEndBillingAlertsTest {


    private DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");
    private DateTimeFormatter fmtToString = DateTimeFormat.forPattern("yyyy-MM-dd");
   
    static final Logger logger = LogManager.getLogger(EndToEndBillingAlertsTest.class);

    @Test
    public void testPayment()
    {
        logger.info("Test payment...");

        BillingAlerts payment = new BillingAlerts("MBxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxPAYPAL01PAYMENT         04014530234040145302xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                      .00 000000000.00                                                2019-05-28        10.00CKPY2019-05-28-00034                             .002001-01-012001-01-01          .002001-01-01          .002001-01-01            .00          .002019-06-12-11.17.11.333882");
        Assert.assertEquals("04014530234", payment.payment.getAccountNumber());
        
        //Assert.assertEquals("040145302", payment.payment.getPremiseNumber());
        Assert.assertEquals("04014530234", payment.payment.getAccountNumber());
        Assert.assertEquals("payment", payment.payment.getAlertType());
        Assert.assertTrue(payment.payment.getPaymentSource().contains("CKPY"));
        //Assert.assertEquals("MB", payment.payment.getRegion());
        //Assert.assertTrue(payment.payment.getTdat().contains("PAYPAL01"));
        Assert.assertEquals(10, payment.payment.getPaymentAmount(), 0.01); // need to check the xat string text to truely validate this
        //Assert.assertEquals("0401453023", payment.payment.get());
        
        //writeAFile(paymentHeader.getEmailContent());

        BillingAlerts pendingPayment = new BillingAlerts("MBxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxKEVIN01 PENDING-PAYMENT 04014530234040145302xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx NNNN    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx  NNNN    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx      NNNN            .00 000000000.00xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx2019-06-11       100.00MMIP                            xx               .002001-01-012001-01-01          .002001-01-01          .002001-01-01            .00          .002019-06-11-15.04.17.138239");


        Assert.assertEquals("04014530234", pendingPayment.getAccountNumber());
        //Assert.assertEquals("040145302", pendingPayment.payment.getPremiseNumber());
        Assert.assertEquals("payment", pendingPayment.getAlertType());
        Assert.assertTrue(pendingPayment.payment.getPaymentSource().contains("MMIP"));
        //Assert.assertEquals("MB", pendingPaymentHeader.getRegion());
        //Assert.assertTrue(pendingPaymentHeader.getTdat().contains("KEVIN01"));
        Assert.assertEquals(100, pendingPayment.payment.getPaymentAmount(), 0.01); // need to check the xat string text to truely validate this
        //Assert.assertEquals("0401453023", pendingPayment.payment.get());
        
        PaymentService paymentService = new PaymentService();

        HashMap<String,String> emailInfo = paymentService.GetEmailTemplate(payment.payment);
        Assert.assertEquals(emailInfo.get("Template"), "PaymentReceived");
        Assert.assertEquals(emailInfo.get("Subject"), "*OPCOSN has Received Your Payment.");

        // OperatingCompanyManager cm = new OperatingCompanyManager();
        // Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        //OperatingCompanyV2 opcoDetails = opcoBuild.get(payment.payment.getAccountNumber().substring(0,2));
        
        HashMap<String, String> smsInfo = paymentService.GetSMSTemplate(payment.payment);
        String textMessage = smsInfo.get("SMSText");

        Assert.assertEquals("*OPCOSN has received your payment of *PAYMENT for your account ending in *ACCT at *ADDRESS.  Visit: *OPCOURL/account.", textMessage);
    }

    @Test
    public void testBillDue()
    {
        logger.info("Test bill due...");

        BillingAlerts billDue = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTBILL-DUE        10944671303109446713xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx            .00 000000000.00xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx2001-01-01          .00xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx            .002001-01-012019-08-08       429.692019-09-04xxxxxxxxxx.002001-01-01   000000000.00 000000529.692019-08-08-14.27.55.719324xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

        BillDueService billDueService = new BillDueService();

        HashMap<String,String> emailInfo = billDueService.GetEmailTemplate(billDue.billDue);
        Assert.assertEquals(emailInfo.get("Template"), "NonCPPDueDateApproaching");
        Assert.assertEquals(emailInfo.get("Subject"), "Due Date Approaching for Your *OPCOSN Electric Bill");

        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(billDue.billDue.getAccountNumber().substring(0,2));
        
        HashMap<String, String> smsInfo = billDueService.GetSMSTemplate(billDue.billDue);
        String textMessage = smsInfo.get("SMSText");
        
        Assert.assertEquals("*OPCOSN: Your balance of $529.69 is due on 9/4/19 for service at *ADDRESS. *OPCOURL/pay", textMessage);
        
        BillingAlerts cppBillDue = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTBILL-DUE        95200121242952001212xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx           Y           .00           xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx     2001-01-01          .00  xxxxxxxxxxxxxxxxxxxxxxxxxx                     .002001-01-012018-03-07       176.002018-03-29          .002001-01-01            .00       176.002018-04-05-12.50.39.468988");

        HashMap<String,String> emailInfo2 = billDueService.GetEmailTemplate(cppBillDue.billDue);
        Assert.assertEquals(emailInfo2.get("Template"), "CPPDueDateApproaching");
        Assert.assertEquals(emailInfo2.get("Subject"), "Your *OPCOSN payment is scheduled");

        opcoDetails = opcoBuild.get(cppBillDue.billDue.getAccountNumber().substring(0,2));
        
        HashMap<String, String> smsInfo2 = billDueService.GetSMSTemplate(cppBillDue.billDue);
        String textMessage2 = smsInfo2.get("SMSText");

        Assert.assertEquals("*OPCOSN: Your payment of $176.00 for *ADDRESS will be deducted on 3/29/18. Total balance: $176.00. *OPCOURL/account", textMessage2);
        
        BillingAlerts billDue2 = new BillingAlerts("MS                                            EXPALERTBILL-DUE        03604047708036040477                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 .00 000000000.00                                                2001-01-01          .00                                                 .002001-01-010001-01-01       219.562019-08-29          .002001-01-01   000000209.39 000000428.952019-08-29-11.04.50.433666 ");
        HashMap<String,String> emailInfo3 = billDueService.GetEmailTemplate(billDue2.billDue);
        // Assert.assertEquals(emailInfo3.get("Template"), "CPPDueDateApproaching");
        // Assert.assertEquals(emailInfo3.get("Subject"), "Your *OPCOSN payment is scheduled");

        opcoDetails = opcoBuild.get(billDue2.billDue.getAccountNumber().substring(0,2));
        
        HashMap<String, String> smsInfo3 = billDueService.GetSMSTemplate(billDue2.billDue);
        String textMessage3 = smsInfo3.get("SMSText");

    }

    @Test
    public void testDisconnectNotice()
    {
        logger.info("Test disconnect notice...");

        BillingAlerts discNotice = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTDISC-NOTICE     96042205508964548095    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx        565.61      xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx       2001-01-01          .00          xxxxxxxxxxxxxxxxxxxxxxxxxxx           .002001-01-010001-01-01       220.062018-03-28       345.552018-03-28         345.55       565.612018-04-05-12.50.40.058097");


        Assert.assertEquals("96042205508", discNotice.getAccountNumber());
        //Assert.assertEquals("040145302", pendingPayment.payment.getPremiseNumber());
        Assert.assertEquals("disc-notice", discNotice.getAlertType());
        //Assert.assertEquals("MMIP", pendingPayment.payment.getPaymentSource());
        //Assert.assertEquals("MA", discNoticeHeader.getRegion());
        //Assert.assertTrue(discNoticeHeader.getTdat().contains("EXPALERT"));
        //Assert.assertEquals(100, billDue.billDue.getPaymentAmount(), 0.01); // need to check the xat string text to truely validate this
        //Assert.assertEquals("0401453023", pendingPayment.payment.get());

    }

    
    // @Test
    // public void testFile() {
    //     DateTime dateTime  = DateTime.now();
	// 	String headerName  ="profile";
    //     // com.aep.cx.load.billing.alerts.service.Load2S3BillingService e = new
    //     // com.aep.cx.load.billing.alerts.service.Load2S3BillingService();
    //     Load2S3BillingService.Load2s3byAlertHeader(new ArrayList<com.aep.cx.macss.customer.subscriptions.MACSSIntegrationWrapper>(), "profile");
    //     //e.Load2s3byAlertHeaderd(new ArrayList<com.aep.cx.macss.customer.subscriptions.MACSSIntegrationWrapper>(), "profile");
    //     String d =  dateTime.toString("yyyyMMdd")+ "/" + headerName + "_" +UUID.randomUUID().toString().replaceAll("-", "")+"_"+ dateTime.toString("HHmmssSSS");
    //     writeAFile(d);
    
    // }

    @Test
    public void testReturnCheck()
    {
        logger.info("Test return check...");

        BillingAlerts returnCheck = new BillingAlerts("MB                                            PAYPAL01RETURN-CHECK    10944671303109446713                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 .00 000000009.00account not found                               2001-01-01        10.10                                                 .002001-01-012001-01-01          .002001-01-01          .002001-01-01            .00          .002019-06-04-14.05.35.980724                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ");

        Assert.assertEquals("return-check", returnCheck.returnCheck.getAlertType());
        Assert.assertEquals(returnCheck.returnCheck.getPaymentAmount(), 10.10, 0.01);
        Assert.assertEquals(returnCheck.returnCheck.getProcessFee(), 9, 0.01);

        ReturnCheckService returnService = new ReturnCheckService();

        HashMap<String,String> emailInfo = returnService.getEmailTemplate(returnCheck.returnCheck);
        Assert.assertEquals(emailInfo.get("Template"), "ReturnPayment");

        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(returnCheck.returnCheck.getAccountNumber().substring(0,2));
        
        HashMap<String, String> smsInfo = returnService.getSMSTemplate(returnCheck.returnCheck, opcoDetails);
        String textMessage = smsInfo.get("SMSText");

        Assert.assertEquals("*OPCOSN: Payment for *ADDRESS was returned. Please immediately pay $10.10 in cash, certified check or money order. More info: http://aepohio.com/account/bills/.", textMessage);

    }

    @Test
    public void testDisconnected()
    {
        logger.info("Test disconnected...");

        BillingAlerts disconnected = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTDISCONNECTED    02040096097020400960    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx           000000901.712018-11-  2018-11-21-23.00.00.074051 000000901.71  xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                   .00 000000000.00    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx  2001-01-01          .00      xxxxxxxxxxxxxxxxxxxxxxxxxxx                .002001-01-012001-01-01          .002001-01-01          .002001-01-01            .00          .002018-11-29-12.28.40.622977");


        Assert.assertEquals("02040096097", disconnected.getAccountNumber());
        Assert.assertEquals("disconnected", disconnected.getAlertType());
    }

    @Test
    public void testReconnected()
    {
        logger.info("Test reconnected...");

        BillingAlerts reconnected = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTRECONNECTED     10271881632102718813    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx           000000000.002018-09-12-23.00.00.177924                         xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx    NNNN     xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx              NNNN       xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx       NNNN            .00 000000000.00    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx  2001-01-01          .00      xxxxxxxxxxxxxxxxxxxxxxxxxxx                .002001-01-012001-01-01          .002001-01-01          .002001-01-01            .00          .002018-11-07-10.12.48.715871");


        Assert.assertEquals("10271881632", reconnected.getAccountNumber());
        Assert.assertEquals("reconnected", reconnected.getAlertType());
    }

    @Test
    public void testReconnectedCreated()
    {
        logger.info("Test reconnect order create...");

        BillingAlerts reconnectCreated = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTRECON-CREATED   10271881632102718813    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx           000000000.002018-08-06-08.00.00.293900                         xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx    NNNN     xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx              NNNN       xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx       NNNN         130.00 000000000.00    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx  2001-01-01          .00      xxxxxxxxxxxxxxxxxxxxxxxxxxx                .002001-01-012001-01-01          .002001-01-01          .002001-01-01            .00          .002018-08-06-11.44.35.771241");


        Assert.assertEquals("10271881632", reconnectCreated.getAccountNumber());
        Assert.assertEquals("recon-created", reconnectCreated.getAlertType());
    }

    @Test
    public void testInHouseWelcome()
    {
        logger.info("Test paperless subscription...");

        BillingAlerts inHouse = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTINHOUSE-WELCOME 02071081000020710810    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                                                                          xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx             xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                         xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                       .00 000000000.00    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                      .00      xxxxxxxxxxxxxxxxxxxxxxxxxxx                .00          2019-08-21          .00                    .00             000000000.00 000000000.002019-07-24-12.36.40.700111");


        Assert.assertEquals("02071081000", inHouse.getAccountNumber());
        Assert.assertEquals("inhouse-welcome", inHouse.getAlertType());
    }

    @Test
    public void testNewBill()
    {
        logger.info("Test new bill available...");

        BillingAlerts newBill = new BillingAlerts("MXxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxDARNER  NEW-BILL        01007724030010077240    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                                                                          xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx    NNNN     xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx              NNNN       xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx       NNNNY           .00 000000000.00    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                      .00      xxxxxxxxxxxxxxxxxxxxxxxxxxx                .00          2019-05-28        70.652019-06-18          .00             000000000.00 000000070.652019-05-29-17.28.49.046241");


        Assert.assertEquals("01007724030", newBill.getAccountNumber());
        Assert.assertEquals("new-bill", newBill.getAlertType());
    }

    @Test
    public void testCPPEnrollment()
    {
        logger.info("Test CPP Enrollment...");

        BillingAlerts currentBill = new BillingAlerts("MBxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxPAYPALDPCPP-ENROLLMENT  |10944671303|2019-07-12|  000001369.91|          |P|C|9275");

        Assert.assertEquals("10944671303", currentBill.getAccountNumber());
        Assert.assertEquals("cpp-enrollment", currentBill.getAlertType());
        
        Assert.assertEquals("9275", currentBill.cpp.getBankNumber());
        Assert.assertEquals("2019-07-12", fmtToString.print(currentBill.cpp.getDueDate()));
        Assert.assertEquals(1369.91, currentBill.cpp.getRemainingAmount(), 0.01);
        Assert.assertEquals("P", currentBill.cpp.getPrenoteStatusCode());
        Assert.assertEquals("C", currentBill.cpp.getToBeCurrNext());
        Assert.assertEquals("2015-01-01", fmtToString.print(currentBill.cpp.getNextBillDate()));

        CPPService cppService = new CPPService();

        HashMap<String,String> emailInfo = cppService.getEmailTemplate(currentBill.cpp);
        Assert.assertEquals(emailInfo.get("Template"), "AutoPayCurrentBill");
        Assert.assertEquals(emailInfo.get("Subject"), "Welcome to AEP AutoPay");

        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(currentBill.cpp.getAccountNumber().substring(0,2));
        
        HashMap<String, String> smsInfo = cppService.getSMSTemplate(currentBill.cpp, opcoDetails);
        String textMessage = smsInfo.get("SMSText");
        
        Assert.assertTrue(textMessage.contains("Welcome to AEP AutoPay. Bills for *ADDRESS will be paid automatically. AutoPay will withdraw $1,369.91 on 7/12 to pay your bill due on that day."));
        
        BillingAlerts currentBillWithNoDueAmount = new BillingAlerts("MDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxPAYPALDPCPP-ENROLLMENT  |10944671302|2019-07-13|  000000000.00|          |P|C|9276");

        Assert.assertEquals("10944671302", currentBillWithNoDueAmount.getAccountNumber());
        Assert.assertEquals("cpp-enrollment", currentBillWithNoDueAmount.getAlertType());
        
        Assert.assertEquals("9276", currentBillWithNoDueAmount.cpp.getBankNumber());
        Assert.assertEquals("2019-07-13", fmtToString.print(currentBillWithNoDueAmount.cpp.getDueDate()));
        Assert.assertEquals(0, currentBillWithNoDueAmount.cpp.getRemainingAmount(), 0.01);
        Assert.assertEquals("P", currentBillWithNoDueAmount.cpp.getPrenoteStatusCode());
        Assert.assertEquals("C", currentBillWithNoDueAmount.cpp.getToBeCurrNext());
        Assert.assertEquals("2015-01-01", fmtToString.print(currentBillWithNoDueAmount.cpp.getNextBillDate()));

        HashMap<String,String> emailInfo2 = cppService.getEmailTemplate(currentBillWithNoDueAmount.cpp);
        Assert.assertEquals(emailInfo2.get("Template"), "AutoPayCurrentBill");
        Assert.assertEquals(emailInfo2.get("Subject"), "Welcome to AEP AutoPay");

        opcoDetails = opcoBuild.get(currentBillWithNoDueAmount.cpp.getAccountNumber().substring(0,2));
        
        HashMap<String, String> smsInfo2 = cppService.getSMSTemplate(currentBillWithNoDueAmount.cpp, opcoDetails);
        String textMessage2 = smsInfo2.get("SMSText");

        Assert.assertTrue(textMessage2.contains("Welcome to AEP AutoPay. Bills for *ADDRESS will be paid automatically. AutoPay will withdraw $0.00 on 7/13 to pay your bill due on that day."));
           
        BillingAlerts nextBillNoAmountDue = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxPAYSEC01CPP-ENROLLMENT  |04024843122|          |  000000000.00|2019-07-29|P|N|2145");

        Assert.assertEquals("04024843122", nextBillNoAmountDue.getAccountNumber());
        Assert.assertEquals("cpp-enrollment", nextBillNoAmountDue.getAlertType());
        
        Assert.assertEquals("2145", nextBillNoAmountDue.cpp.getBankNumber());
        Assert.assertEquals("2015-01-01", fmtToString.print(nextBillNoAmountDue.cpp.getDueDate()));
        Assert.assertEquals(0, nextBillNoAmountDue.cpp.getRemainingAmount(), 0.01);
        Assert.assertEquals("P", nextBillNoAmountDue.cpp.getPrenoteStatusCode());
        Assert.assertEquals("N", nextBillNoAmountDue.cpp.getToBeCurrNext());
        Assert.assertEquals("2019-07-29", fmtToString.print(nextBillNoAmountDue.cpp.getNextBillDate()));

        HashMap<String,String> emailInfo3 = cppService.getEmailTemplate(nextBillNoAmountDue.cpp);
        Assert.assertEquals(emailInfo3.get("Template"), "AutoPayNextBill");
        Assert.assertEquals(emailInfo3.get("Subject"), "Welcome to AEP AutoPay");

        opcoDetails = opcoBuild.get(nextBillNoAmountDue.cpp.getAccountNumber().substring(0,2));
        
        HashMap<String, String> smsInfo3 = cppService.getSMSTemplate(nextBillNoAmountDue.cpp, opcoDetails);
        String textMessage3 = smsInfo3.get("SMSText");

        Assert.assertTrue(textMessage3.contains("Welcome to AEP AutoPay. Your bills for *ADDRESS will be paid automatically. See bill after meter reading on 7/29 for first withdrawal date and amount."));

        BillingAlerts nextBillWithRemainingAmount = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxPAYSEC01CPP-ENROLLMENT  |04024843122|          |  000000037.38|2019-07-29|P|N|2145");

        Assert.assertEquals("04024843122", nextBillWithRemainingAmount.getAccountNumber());
        Assert.assertEquals("cpp-enrollment", nextBillWithRemainingAmount.getAlertType());
        
        Assert.assertEquals("2145", nextBillWithRemainingAmount.cpp.getBankNumber());
        Assert.assertEquals("2015-01-01", fmtToString.print(nextBillWithRemainingAmount.cpp.getDueDate()));
        Assert.assertEquals(37.38, nextBillWithRemainingAmount.cpp.getRemainingAmount(), 0.01);
        Assert.assertEquals("P", nextBillWithRemainingAmount.cpp.getPrenoteStatusCode());
        Assert.assertEquals("N", nextBillWithRemainingAmount.cpp.getToBeCurrNext());
        Assert.assertEquals("2019-07-29", fmtToString.print(nextBillWithRemainingAmount.cpp.getNextBillDate()));
    
        HashMap<String,String> emailInfo4 = cppService.getEmailTemplate(nextBillWithRemainingAmount.cpp);
        Assert.assertEquals(emailInfo4.get("Template"), "AutoPayNextBillWithDues");
        Assert.assertEquals(emailInfo4.get("Subject"), "Welcome to AEP AutoPay");

        opcoDetails = opcoBuild.get(nextBillWithRemainingAmount.cpp.getAccountNumber().substring(0,2));
        
        HashMap<String, String> smsInfo4 = cppService.getSMSTemplate(nextBillWithRemainingAmount.cpp, opcoDetails);
        String textMessage4 = smsInfo4.get("SMSText");

        Assert.assertTrue(textMessage4.contains("Welcome to AEP AutoPay. AutoPay for *ADDRESS begins after 7/29 read. You must pay your current bill before 1/1. http://iandmpwr.com/pay"));
    }

    @Test
    public void testCPPUpdate()
    {
        logger.info("Test CPP Update...");

        BillingAlerts b = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxPAYSEC01CPP-UPDATE      |02362712032|          |  000000000.00|          | | |1111");


        Assert.assertEquals("02362712032", b.getAccountNumber());
        Assert.assertEquals("cpp-update", b.getAlertType());
        //Assert.assertEquals("MA", header.getRegion());
        //Assert.assertTrue(header.getTdat().contains("PAYSEC01"));

        Assert.assertEquals("1111", b.cpp.getBankNumber());

        CPPService cppService = new CPPService();

        HashMap<String,String> emailInfo = cppService.getEmailTemplate(b.cpp);
        Assert.assertEquals(emailInfo.get("Template"), "AutoPayUpdated");
        Assert.assertEquals(emailInfo.get("Subject"), "AutoPay information was updated.");

        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(b.cpp.getAccountNumber().substring(0,2));
        
        HashMap<String, String> smsInfo = cppService.getSMSTemplate(b.cpp, opcoDetails);
        String textMessage = smsInfo.get("SMSText");


        Assert.assertTrue(textMessage.contains("AutoPay bank account information updated for *ADDRESS and will be used to pay all current and future bills."));
    }

    @Test
    public void testCPPCancel()
    {
        logger.info("Test CPP Cancel...");

        BillingAlerts b = new BillingAlerts("MBxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxPAYPAL01CPP-CANCEL      |96061496137|          |  000000000.00|          | | |    ");

        
        Assert.assertEquals("96061496137", b.getAccountNumber());
        Assert.assertEquals("cpp-cancel", b.getAlertType());
        //Assert.assertEquals("MB", header.getRegion());
        //Assert.assertTrue(header.getTdat().contains("PAYPAL01"));
        Assert.assertEquals("", b.cpp.getBankNumber().trim());

        CPPService cppService = new CPPService();

        HashMap<String,String> emailInfo = cppService.getEmailTemplate(b.cpp);
        Assert.assertEquals(emailInfo.get("Template"), "AutoPayUnenroll");
        Assert.assertEquals(emailInfo.get("Subject"), "Unenrolled from AEP AutoPay");

        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(b.cpp.getAccountNumber().substring(0,2));
        
        HashMap<String, String> smsInfo = cppService.getSMSTemplate(b.cpp, opcoDetails);
        String textMessage = smsInfo.get("SMSText");
        

        Assert.assertTrue(textMessage.contains("*ADDRESS has been unenrolled from AutoPay. You are responsible for paying any current or future bills by the due date."));
                                                
    }

    @Test
    public void testCPPAcknowledgement()
    {
        logger.info("Test CPP Acknowledgement...");

        BillingAlerts b = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxPAYSEC01CPP-ACKNOWLEDGE |07340616718|          |  000000000.00|          |F| |9054");

        Assert.assertEquals("07340616718", b.getAccountNumber());
        Assert.assertEquals("cpp-acknowledge", b.getAlertType());
        //Assert.assertEquals("MA", header.getRegion());
        //Assert.assertTrue(header.getTdat().contains("PAYSEC01"));

        Assert.assertEquals("9054", b.cpp.getBankNumber());
        Assert.assertEquals("F", b.cpp.getPrenoteStatusCode());

        CPPService cppService = new CPPService();

        HashMap<String,String> emailInfo = cppService.getEmailTemplate(b.cpp);
        Assert.assertEquals(emailInfo.get("Template"), "AutoPayActivationFail");
        //Assert.assertEquals(emailInfo.get("Subject"), "Welcome to AEP AutoPay");

        OperatingCompanyManager cm = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
        OperatingCompanyV2 opcoDetails = opcoBuild.get(b.cpp.getAccountNumber().substring(0,2));
        
        HashMap<String, String> smsInfo = cppService.getSMSTemplate(b.cpp, opcoDetails);
        String textMessage = smsInfo.get("SMSText");

        Assert.assertTrue(textMessage.contains("Unsuccessful validation of AutoPay banking information for *ADDRESS. You can clarify with your bank and sign up for AutoPay again."));

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
