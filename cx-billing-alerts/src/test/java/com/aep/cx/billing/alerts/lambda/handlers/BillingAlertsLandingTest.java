package com.aep.cx.billing.alerts.lambda.handlers;

import java.io.IOException;
import java.util.ArrayList;

import java.io.*;
import java.text.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.billing.alerts.lambda.handlers.BillingAlertsLanding;
import com.aep.cx.billing.events.BillingAlerts;
import com.amazonaws.services.lambda.runtime.Context;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class BillingAlertsLandingTest {


    // private DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");
    private DateTimeFormatter fmtToString = DateTimeFormat.forPattern("yyyy-MM-dd");
    // private DateTime defaultDateTime = formatter.parseDateTime("2015-01-01-01.01.01.000001");
    // private DateTimeFormatter shortFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");


    static final Logger logger = LogManager.getLogger(BillingAlertsLandingTest.class);

    private static ArrayList<Object> input;

    @BeforeClass
    public static void createInput() throws IOException {
        // TODO: set up your sample input object here.
        input = null;
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testBillingAlertsLanding() {

        logger.info("Testing billing alerts landing...");
        BillingAlertsLanding handler = new BillingAlertsLanding();
        Context ctx = createContext();
    
        //String output = handler.handleRequest(input, ctx);

        // TODO: validate output here if needed.
        //Assert.assertEquals("Hello from Lambda!", output);
    }

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
        
        
    }

    @Test
    public void testBillDue()
    {
        logger.info("Test bill due...");

        BillingAlerts billDue = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTBILL-DUE        95200121242952001212xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx           Y           .00           xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx     2001-01-01          .00  xxxxxxxxxxxxxxxxxxxxxxxxxx                     .002001-01-012018-03-07       176.002018-03-29          .002001-01-01            .00       176.002018-04-05-12.50.39.468988");
        
        //writeAFile("HashLink,AccountNo,Template,StreetAddress1,StreetAddress2,City,State,ZipCode,FirstName,AccountNickname,Preheader,Subject,SubscriberKey,EmailAddress,ChannelMemberID,ESID,LearnMoreLink,PaymentAmount,DueDate,TotalAmount,PastDueAmount,PaymentReceived,PayMyBillLink,AssistAgencyLink,PendingPaymentAmount,CPPCustomer,BudgetBillAnniversary,ProcessFee,Reason\r\n" 
        //+ "ae0599694ad84d628e96ca3f36545ead,02859119501,NonCPPDueDateApproaching,\"C***\",,FRIES,VA,24330,\"\",,APCo: test,APCo: test,ae0510694ad84d628e96ca3f36545ead,aerickson@aep.com,150036,00007330225702272,http://psoklahoma.com/alerts,176,2018-03-29,176,0,2001-01-01," +
        ///billDueHeader.getEmailContent());

        Assert.assertEquals("95200121242", billDue.getAccountNumber());
        //Assert.assertEquals("040145302", pendingPayment.payment.getPremiseNumber());
        Assert.assertEquals("bill-due", billDue.getAlertType());
        //Assert.assertEquals("MMIP", pendingPayment.payment.getPaymentSource());
        //Assert.assertEquals("MA", billDueHeader.getRegion());
        //Assert.assertTrue(billDueHeader.getTdat().contains("EXPALERT"));
        Assert.assertEquals(0, billDue.billDue.getPastDueAmount(), 0.01);
        Assert.assertEquals(176, billDue.billDue.getBillAmount(), 0.01);
        Assert.assertEquals(0, billDue.billDue.getPendingPaymentAmount(), 0.01);
        Assert.assertEquals(176, billDue.billDue.getTotalAmount(), 0.01);
        Assert.assertEquals("y", billDue.billDue.getCPPCustomer().toLowerCase());
        Assert.assertEquals("a", billDue.billDue.getBudgetBillAniversary().toLowerCase());
        //Assert.assertEquals(100, billDue.billDue.getPaymentAmount(), 0.01); // need to check the xat string text to truely validate this
        //Assert.assertEquals("0401453023", pendingPayment.payment.get());

        // com.aep.cx.utils.alerts.notification.msessages.EmailDeliveryHeader emailHeader = new com.aep.cx.utils.alerts.notification.msessages.EmailDeliveryHeader();
        // emailHeader.setHashLink("d418d7d3e5bd45c18a6513d5b02af8ad");
        // emailHeader.setAccountNumber(billDue.getAccountNumber());
		// emailHeader.setAccountNickname("");
		// emailHeader.setChannelMemberID("150040");
		// emailHeader.setCity("Elida");
		// emailHeader.setStreetAddress1("R***");
		// emailHeader.setESID("");
		// emailHeader.setFirstName("Aaron");
		// emailHeader.setLearnMoreLink("www.learnmorelink.com");
		// emailHeader.setPreheader("test preheader");
		// emailHeader.setState("OH");
		// emailHeader.setStreetAddress2("");
		// emailHeader.setSubject(emailHeader.getPreheader());
		// emailHeader.setTemplate("testEmailtemplate");
        // emailHeader.setZipCode("45807");
        // emailHeader.setEmailAddress("aerickson@aep.com");
        // emailHeader.setSubscriberKey(emailHeader.getHashLink());
        
        // writeAFile(emailHeader.toString() + "," + billDue.billDue.getEmailContent());

        BillingAlerts billDue2 = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTBILL-DUE        95200121242952001212xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx           Y           .00           xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx     2001-01-01          .00  xxxxxxxxxxxxxxxxxxxxxxxxxx                     .002001-01-012018-03-07       176.002018-03-29          .002001-01-01            .00       176.002018-04-05-12.50.39.468988");
        

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

    @Test
    public void testReturnCheck()
    {
        logger.info("Test return check...");

        BillingAlerts returnCheck = new BillingAlerts("MBxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxPAYPAL01RETURN-CHECK    10014675010100146750   xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                     .00 000000009.00account not found                               2001-01-01        10.10                                                 .002001-01-012001-01-01          .002001-01-01          .002001-01-01            .00          .002019-06-04-14.05.35.980724");


        Assert.assertEquals("10014675010", returnCheck.returnCheck.getAccountNumber());
        //Assert.assertEquals("040145302", pendingPayment.payment.getPremiseNumber());
        Assert.assertEquals("return-check", returnCheck.returnCheck.getAlertType());
        Assert.assertEquals(returnCheck.returnCheck.getPaymentAmount(), 10.10, 0.01);
        Assert.assertEquals(returnCheck.returnCheck.getProcessFee(), 9, 0.01);

        //returnCheck.returnCheck
        //Assert.assertEquals("MMIP", pendingPayment.payment.getPaymentSource());
        //Assert.assertEquals("MB", returnCheckHeader.getRegion());
        //Assert.assertTrue(returnCheckHeader.getTdat().contains("PAYPAL01"));
        //Assert.assertEquals(100, billDue.billDue.getPaymentAmount(), 0.01); // need to check the xat string text to truely validate this
        //Assert.assertEquals("0401453023", pendingPayment.payment.get());

        // com.aep.cx.utils.alerts.notification.msessages.EmailDeliveryHeader emailHeader = new com.aep.cx.utils.alerts.notification.msessages.EmailDeliveryHeader();
        // emailHeader.setHashLink("d418d7d3e5bd45c18a6513d5b02af8ad");
        // emailHeader.setAccountNumber(returnCheck.getAccountNumber());
		// emailHeader.setAccountNickname("");
		// emailHeader.setChannelMemberID("150040");
		// emailHeader.setCity("Elida");
		// emailHeader.setStreetAddress1("R***");
		// emailHeader.setESID("");
		// emailHeader.setFirstName("Aaron");
		// emailHeader.setLearnMoreLink("www.learnmorelink.com");
		// emailHeader.setPreheader("test preheader");
		// emailHeader.setState("OH");
		// emailHeader.setStreetAddress2("");
		// emailHeader.setSubject(emailHeader.getPreheader());
		// emailHeader.setTemplate("testEmailtemplate");
        // emailHeader.setZipCode("45807");
        // emailHeader.setEmailAddress("aerickson@aep.com");
        // emailHeader.setSubscriberKey(emailHeader.getHashLink());
        
        // writeAFile(emailHeader.toString() + "," + returnCheck.returnCheck.getEmailContent());


    }

    @Test
    public void testDisconnected()
    {
        logger.info("Test disconnected...");

        BillingAlerts disconnected = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTDISCONNECTED    02040096097020400960    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx           000000901.712018-11-  2018-11-21-23.00.00.074051 000000901.71  xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                   .00 000000000.00    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx  2001-01-01          .00      xxxxxxxxxxxxxxxxxxxxxxxxxxx                .002001-01-012001-01-01          .002001-01-01          .002001-01-01            .00          .002018-11-29-12.28.40.622977");


        Assert.assertEquals("02040096097", disconnected.getAccountNumber());
        Assert.assertEquals("disconnected", disconnected.getAlertType());
        //Assert.assertEquals("MA", header.getRegion());
        //Assert.assertTrue(header.getTdat().contains("EXPALERT"));
    }

    @Test
    public void testReconnected()
    {
        logger.info("Test reconnected...");

        BillingAlerts reconnected = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTRECONNECTED     10271881632102718813    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx           000000000.002018-09-12-23.00.00.177924                         xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx    NNNN     xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx              NNNN       xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx       NNNN            .00 000000000.00    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx  2001-01-01          .00      xxxxxxxxxxxxxxxxxxxxxxxxxxx                .002001-01-012001-01-01          .002001-01-01          .002001-01-01            .00          .002018-11-07-10.12.48.715871");


        Assert.assertEquals("10271881632", reconnected.getAccountNumber());
        Assert.assertEquals("reconnected", reconnected.getAlertType());
        //Assert.assertEquals("MA", header.getRegion());
        //Assert.assertTrue(header.getTdat().contains("EXPALERT"));
    }

    @Test
    public void testReconnectedCreated()
    {
        logger.info("Test reconnect order create...");

        BillingAlerts reconnectCreated = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTRECON-CREATED   10271881632102718813    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx           000000000.002018-08-06-08.00.00.293900                         xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx    NNNN     xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx              NNNN       xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx       NNNN         130.00 000000000.00    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx  2001-01-01          .00      xxxxxxxxxxxxxxxxxxxxxxxxxxx                .002001-01-012001-01-01          .002001-01-01          .002001-01-01            .00          .002018-08-06-11.44.35.771241");


        Assert.assertEquals("10271881632", reconnectCreated.getAccountNumber());
        Assert.assertEquals("recon-created", reconnectCreated.getAlertType());
        //Assert.assertEquals("MA", header.getRegion());
        //Assert.assertTrue(header.getTdat().contains("EXPALERT"));
    }

    @Test
    public void testInHouseWelcome()
    {
        logger.info("Test paperless subscription...");

        BillingAlerts inHouse = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTINHOUSE-WELCOME 02071081000020710810    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                                                                          xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx             xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                         xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                       .00 000000000.00    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                      .00      xxxxxxxxxxxxxxxxxxxxxxxxxxx                .00          2019-08-21          .00                    .00             000000000.00 000000000.002019-07-24-12.36.40.700111");


        Assert.assertEquals("02071081000", inHouse.getAccountNumber());
        Assert.assertEquals("inhouse-welcome", inHouse.getAlertType());
        //Assert.assertEquals("MA", header.getRegion());
        //Assert.assertTrue(header.getTdat().contains("EXPALERT"));
    }

    @Test
    public void testNewBill()
    {
        logger.info("Test new bill available...");

        BillingAlerts newBill = new BillingAlerts("MXxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxDARNER  NEW-BILL        01007724030010077240    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                                                                          xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx    NNNN     xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx              NNNN       xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx       NNNNY           .00 000000000.00    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                      .00      xxxxxxxxxxxxxxxxxxxxxxxxxxx                .00          2019-05-28        70.652019-06-18          .00             000000000.00 000000070.652019-05-29-17.28.49.046241");


        Assert.assertEquals("01007724030", newBill.getAccountNumber());
        Assert.assertEquals("new-bill", newBill.getAlertType());
        //Assert.assertEquals("MX", header.getRegion());
        //Assert.assertTrue(header.getTdat().contains("DARNER"));
    }

    @Test
    public void testCPPEnrollment()
    {
        logger.info("Test CPP Enrollment...");

        //BillingAlerts test = new BillingAlerts("MB   x x x x x x x x x x x x x x x x x x x x  PAYPALDPCPP-ENROLLMENT  |10944671303|2019-07-12|  000001369.91|          |P|C|9275");

        BillingAlerts currentBillWithDueAmount = new BillingAlerts("MBxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxPAYPALDPCPP-ENROLLMENT  |10944671303|2019-07-12|  000001369.91|          |P|C|9275");



        Assert.assertEquals("10944671303", currentBillWithDueAmount.getAccountNumber());
        Assert.assertEquals("cpp-enrollment", currentBillWithDueAmount.getAlertType());
        //Assert.assertEquals("MB", header.getRegion());
        //Assert.assertTrue(header.getTdat().contains("PAYPALDP"));
        
        
        Assert.assertEquals("9275", currentBillWithDueAmount.cpp.getBankNumber());
        Assert.assertEquals("2019-07-12", fmtToString.print(currentBillWithDueAmount.cpp.getDueDate()));
        Assert.assertEquals(1369.91, currentBillWithDueAmount.cpp.getRemainingAmount(), 0.01);
        Assert.assertEquals("P", currentBillWithDueAmount.cpp.getPrenoteStatusCode());
        Assert.assertEquals("C", currentBillWithDueAmount.cpp.getToBeCurrNext());
        Assert.assertEquals("2015-01-01", fmtToString.print(currentBillWithDueAmount.cpp.getNextBillDate()));
        
        
        BillingAlerts currentBillWithNoDueAmount = new BillingAlerts("MDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxPAYPALDPCPP-ENROLLMENT  |10944671302|2019-07-13|  000000000.00|          |P|C|9276");



        Assert.assertEquals("10944671302", currentBillWithNoDueAmount.getAccountNumber());
        Assert.assertEquals("cpp-enrollment", currentBillWithNoDueAmount.getAlertType());
        //Assert.assertEquals("MD", currentBillWithNoDueAmountHeader.getRegion());
        //Assert.assertTrue(currentBillWithNoDueAmountHeader.getTdat().contains("PAYPALDP"));
        
        
        Assert.assertEquals("9276", currentBillWithNoDueAmount.cpp.getBankNumber());
        Assert.assertEquals("2019-07-13", fmtToString.print(currentBillWithNoDueAmount.cpp.getDueDate()));
        Assert.assertEquals(0, currentBillWithNoDueAmount.cpp.getRemainingAmount(), 0.01);
        Assert.assertEquals("P", currentBillWithNoDueAmount.cpp.getPrenoteStatusCode());
        Assert.assertEquals("C", currentBillWithNoDueAmount.cpp.getToBeCurrNext());
        Assert.assertEquals("2015-01-01", fmtToString.print(currentBillWithNoDueAmount.cpp.getNextBillDate()));




        BillingAlerts nextBillNoAmountDue = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxPAYSEC01CPP-ENROLLMENT  |04024843122|          |  000000000.00|2019-07-29|P|N|2145");



        Assert.assertEquals("04024843122", nextBillNoAmountDue.getAccountNumber());
        Assert.assertEquals("cpp-enrollment", nextBillNoAmountDue.getAlertType());
        //Assert.assertEquals("MA", nextBillNoAmountDueHeader.getRegion());
        //Assert.assertTrue(nextBillNoAmountDueHeader.getTdat().contains("PAYSEC01"));
        
        
        Assert.assertEquals("2145", nextBillNoAmountDue.cpp.getBankNumber());
        Assert.assertEquals("2015-01-01", fmtToString.print(nextBillNoAmountDue.cpp.getDueDate()));
        Assert.assertEquals(0, nextBillNoAmountDue.cpp.getRemainingAmount(), 0.01);
        Assert.assertEquals("P", nextBillNoAmountDue.cpp.getPrenoteStatusCode());
        Assert.assertEquals("N", nextBillNoAmountDue.cpp.getToBeCurrNext());
        Assert.assertEquals("2019-07-29", fmtToString.print(nextBillNoAmountDue.cpp.getNextBillDate()));

        BillingAlerts nextBillWithRemainingAmount = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxPAYSEC01CPP-ENROLLMENT  |04024843122|          |  000000037.38|2019-07-29|P|N|2145");



        Assert.assertEquals("04024843122", nextBillNoAmountDue.getAccountNumber());
        Assert.assertEquals("cpp-enrollment", nextBillNoAmountDue.getAlertType());
        //Assert.assertEquals("MA", nextBillWithRemainingAmountHeader.getRegion());
        //Assert.assertTrue(nextBillWithRemainingAmountHeader.getTdat().contains("PAYSEC01"));
        
        
        Assert.assertEquals("2145", nextBillWithRemainingAmount.cpp.getBankNumber());
        Assert.assertEquals("2015-01-01", fmtToString.print(nextBillWithRemainingAmount.cpp.getDueDate()));
        Assert.assertEquals(37.38, nextBillWithRemainingAmount.cpp.getRemainingAmount(), 0.01);
        Assert.assertEquals("P", nextBillWithRemainingAmount.cpp.getPrenoteStatusCode());
        Assert.assertEquals("N", nextBillWithRemainingAmount.cpp.getToBeCurrNext());
        Assert.assertEquals("2019-07-29", fmtToString.print(nextBillWithRemainingAmount.cpp.getNextBillDate()));
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

        // com.aep.cx.utils.alerts.notification.msessages.EmailDeliveryHeader emailHeader = new com.aep.cx.utils.alerts.notification.msessages.EmailDeliveryHeader();
        // emailHeader.setHashLink("d418d7d3e5bd45c18a6513d5b02af8ad");
        // emailHeader.setAccountNumber(b.cpp.getAccountNumber());
		// emailHeader.setAccountNickname("");
		// emailHeader.setChannelMemberID("150040");
		// emailHeader.setCity("Elida");
		// emailHeader.setStreetAddress1("R***");
		// emailHeader.setESID("");
		// emailHeader.setFirstName("Aaron");
		// emailHeader.setLearnMoreLink("www.learnmorelink.com");
		// emailHeader.setPreheader("test preheader");
		// emailHeader.setState("OH");
		// emailHeader.setStreetAddress2("");
		// emailHeader.setSubject(emailHeader.getPreheader());
		// emailHeader.setTemplate("AutoPayActivationFail");
        // emailHeader.setZipCode("45807");
        // emailHeader.setEmailAddress("aerickson@aep.com");
        // emailHeader.setSubscriberKey(emailHeader.getHashLink());
        
        // writeAFile(emailHeader.toString() + "," + b.cpp.getEmailContent());

    }
    
    @Test
    public void testOrderTracking()
    {
        logger.info("Test Order Tracking...");

        BillingAlerts b = new BillingAlerts("MXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX0XXXXXXXXXXXXXEXPALERTINSP-COMPLETE   |01234567890|123456789|NI01|2019-08-02|EDIR2019-08-102019-08-15S007063XXXXXXXXX~CURR2019-08-102019-08-15S007063XXXXXXXXX~~~~|ISTA2019-08-102019-08-15S007063XXXXXXXXX~ICNT2019-08-102019-08-15S007063XXXXXXXXX~~~~");

        Assert.assertEquals("01234567890", b.getAccountNumber());
        Assert.assertEquals("insp-complete", b.getAlertType());

        //Assert.assertEquals("9054", b.cpp.getBankNumber());
        //Assert.assertEquals("F", b.cpp.getPrenoteStatusCode());
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
