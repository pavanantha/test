package com.aep.cx.billing.alerts.lambda.handlers;

import java.io.IOException;
import java.util.ArrayList;

import java.io.*;
import java.text.*;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.internal.inject.Custom;

import com.aep.cx.billing.alerts.business.SubscriptionsService;
import com.aep.cx.billing.alerts.enrollment.events.EnrollmentAlerts;
import com.aep.cx.billing.alerts.lambda.handlers.BillingAlertsLanding;
import com.aep.cx.billing.events.BillingAlerts;
import com.aep.cx.preferences.dao.CustomerInfo;
import com.amazonaws.services.lambda.runtime.Context;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class EndToEndProcessSubscriptionTest {


    @Test
    public void test()
    {
        SubscriptionsService service = new SubscriptionsService();



        // "alertType":"auto-registered","accountNumber":"01000527620","premiseNumber":"010005276","ezid":"","macssEmailContent":"&MaxCustOut:0&Cause:unknown&ETR:2001-01-01T00:00:00.0000000&Duration:unknown&OutageChannel:ENNN&CreditChannel:NNNN","emailContent":",8/26/2019 7:01:49 PM,,,,ENNN",
        //"macssID":"MCSXAT","externalID":"XATFTP","webID":"awsuatuser02","endPoint":"0100052762@aep.com","alertDetails":"ENNN",
        //"customerInfo":{"premiseNumber":null,"accountNumber":null,"name":"CHARLES ADDISON","streetAddress":"F***","city":"FALL BRANCH","state":"TN",
        //"zipCode":"37656"},"opcoAbbreviatedName":null}


        
        EnrollmentAlerts enrollmentAlerts = new EnrollmentAlerts("MS                                            EXPALERTSUBS-ALL        |0100165670 |awsuatuser08                                                                                                                                                                                                                                                                                                                    |010016567|A|    232|0100165670@aep.com                                                                                                                                                                                                                                                                                                              |          |B|ROGERSVILLE         |TN|37857-5902|JEFFERY                            |GOAD                               |OUTAGE          E~BILLINGPAYMENT  N~PSOVPP          N~POWERPAY        N~ORDER           N~                 ~                 ~                 ~                 ~                 ~");

        enrollmentAlerts.setAccountNumber("01000527620");
        enrollmentAlerts.setPremiseNumber("010005276");
        enrollmentAlerts.setEzid("");
        enrollmentAlerts.setAlertType("auto-welcome");
        enrollmentAlerts.setMacssEmailContent("&MaxCustOut:0&Cause:unknown&ETR:2001-01-01T00:00:00.0000000&Duration:unknown&OutageChannel:ENNN&CreditChannel:NNNN");
        enrollmentAlerts.setEmailContent(",8/26/2019 7:01:49 PM,,,,ENNN");
        enrollmentAlerts.setEndPoint("dedarner@aep.com");
        enrollmentAlerts.setAlertDetails("ENNN");

        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setAccountNumber(enrollmentAlerts.getAlertType());
        customerInfo.setCity("Test");
        customerInfo.setName("Name");
        customerInfo.setState("OH");
        customerInfo.setStreetAddress("1234 **");
        customerInfo.setZipCode("43004");

        enrollmentAlerts.setCustomerInfo(customerInfo);

        HashMap<String, ArrayList<String>> testmap = service.BuildEmail(enrollmentAlerts);
        int test = 0;

    }


}

