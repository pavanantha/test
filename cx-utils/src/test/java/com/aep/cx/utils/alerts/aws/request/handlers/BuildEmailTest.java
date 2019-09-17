package com.aep.cx.utils.alerts.aws.request.handlers;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Console;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.aep.cx.preferences.dao.CustomerContacts;
import com.aep.cx.preferences.dao.CustomerInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.utils.alerts.common.data.AlertsNotificationData;
import com.aep.cx.utils.alerts.notification.msessages.BuildEmail;

/*
public class BuildEmailTest {

	private ArrayList<AlertsNotificationData> alertsData1 = new ArrayList<>();
	AlertsNotificationData ad1;
	private ArrayList<AlertsNotificationData> alertsData2 = new ArrayList<>();
	AlertsNotificationData ad2;
	private ArrayList<AlertsNotificationData> alertsData3 = new ArrayList<>();
	AlertsNotificationData ad3;
	private ArrayList<AlertsNotificationData> alertsData4 = new ArrayList<>();
	AlertsNotificationData ad4;

	private HashMap<String, ArrayList<CustomerPreferences>> cpList;

	ObjectMapper mapper = new ObjectMapper();
	TypeReference<Map<String, ArrayList<CustomerPreferences>>> cpListType = new TypeReference<Map<String, ArrayList<CustomerPreferences>>>() {
	};

	HashMap<String, ArrayList<CustomerPreferences>> loadPreferencesTestData(String resourceName) throws IOException {
		return mapper.readValue(IOUtils.resourceToString(resourceName, Charset.defaultCharset()), cpListType);

	}

	static final Logger logger = LogManager.getLogger(BuildEmailTest.class);

	@Before
	public void setUp() throws Exception {
		// Email Alert Type Predicted
		ad1 = new AlertsNotificationData();
		ad1.setPremiseNumber("019999999");
		ad1.setAlertName("predicted");
		ad1.setOutageSimpleCause("unknown");
		ad1.setEtrType("g");
		ad1.setOutageEtrTime(DateTime.now().plusDays(3));
		alertsData1.add(ad1);

		// Email Alert Type Restored
		ad2 = new AlertsNotificationData();
		ad2.setPremiseNumber("019999998");
		ad2.setAlertName("restored");
		ad2.setOutageSimpleCause("unknown");
		ad2.setEtrType("g");
		ad2.setOutageEtrTime(DateTime.now().plusDays(3));
		alertsData2.add(ad2);

		// Email Alert Type ETR
		ad3 = new AlertsNotificationData();
		ad3.setPremiseNumber("019999997");
		ad3.setAlertName("etr");
		ad3.setOutageSimpleCause("unknown");
		ad3.setEtrType("g");
		ad3.setOutageEtrTime(DateTime.now().plusDays(3));
		alertsData3.add(ad3);

		// Email Alert Type Cancelled
		ad4 = new AlertsNotificationData();
		ad4.setPremiseNumber("019999997");
		ad4.setAlertName("cancelled");
		ad4.setOutageSimpleCause("unknown");
		ad4.setEtrType("g");
		ad4.setOutageEtrTime(DateTime.now().plusDays(3));
		alertsData4.add(ad4);

		// Get Customer Prefernces From a Json File Dedicated To Test Customers
		cpList = loadPreferencesTestData("/testpreferences.json");
	}

	@Test
	public void test_Customer_Email_Notification_Data_PREDICTED() {

		BuildEmail email = new BuildEmail(alertsData1, cpList);

		logger.info("Results of Test Case Customer Email Notification Data PREDICTED Values Being Passed:\n"
				+ email.getOutageXat().getEmailAddress() + " " + email.getOutageXat().getAccountNumber() + " "
				+ email.getOutageXat().getSubject());

		// Exact Target Needed Values To Build Email Template
		// HashLink,AccountNo,Template,StreetAddress1,StreetAddress2,City,State,ZipCode,FirstName,AccountNickname,Preheader,
		// Subject,SubscriberKey,EmailAddress,ChannelMemberID,ESID,LearnMoreLink,MaxCustOut,TimeStamp,Cause,ETR,Duration,Outage

		assertNotEquals("", email.getOutageXat().getHashLink());
		assertEquals("567-dbe", email.getOutageXat().getAccountNumber());
		assertEquals("OutagePredicted", email.getOutageXat().getTemplate());
		assertNotEquals("1", email.getOutageXat().getStreetAddress1());
		assertNotEquals("1", email.getOutageXat().getStreetAddress2());
		assertNotEquals("1", email.getOutageXat().getCity());
		assertNotEquals("1", email.getOutageXat().getState());
		assertNotEquals("1", email.getOutageXat().getZipCode());
		assertNotEquals("1", email.getOutageXat().getFirstName());
		assertNotEquals("1", email.getOutageXat().getAccountNickname());
		assertNotEquals("", email.getOutageXat().getPreheader());
		assertEquals("APCo : Outage Reported In Your Area", email.getOutageXat().getSubject());
		assertNotEquals("1", email.getOutageXat().getSubscriberKey());
		assertEquals("whiterabbit@wonderland.com", email.getOutageXat().getEmailAddress());
		assertNotEquals("1", email.getOutageXat().getChannelMemberID());
		assertNotEquals("1", email.getOutageXat().getESID());
		assertNotEquals("1", email.getOutageXat().getLearnMoreLink());
		assertNotEquals("1", email.getOutageXat().getMaxCustOut());
		assertNotEquals("1", email.getOutageXat().getTimeStamp());
		assertNotEquals("1", email.getOutageXat().getCause());
		assertNotEquals("1", email.getOutageXat().getETR());
		assertNotEquals("1", email.getOutageXat().getDuration());
		assertNotEquals("1", email.getOutageXat().getOutage());

	}

	@Test
	public void test_Customer_Email_Notification_Data_RESTORED() {

		BuildEmail email = new BuildEmail(alertsData2, cpList);

		logger.info("Results of Test Case Customer Email Notification Data RESTORED Values Being Passed:\n"
				+ email.getOutageXat().getEmailAddress() + " " + email.getOutageXat().getAccountNumber() + " "
				+ email.getOutageXat().getSubject());

		assertNotEquals("", email.getOutageXat().getHashLink());
		assertEquals("123-abc", email.getOutageXat().getAccountNumber());
		assertEquals("OutageRestored", email.getOutageXat().getTemplate());
		assertNotEquals("1", email.getOutageXat().getStreetAddress1());
		assertNotEquals("1", email.getOutageXat().getStreetAddress2());
		assertNotEquals("1", email.getOutageXat().getCity());
		assertNotEquals("1", email.getOutageXat().getState());
		assertNotEquals("1", email.getOutageXat().getZipCode());
		assertNotEquals("1", email.getOutageXat().getFirstName());
		assertNotEquals("1", email.getOutageXat().getAccountNickname());
		assertNotEquals("", email.getOutageXat().getPreheader());
		assertEquals("APCo : Power Has Been Restored", email.getOutageXat().getSubject());
		assertNotEquals("1", email.getOutageXat().getSubscriberKey());
		assertEquals("madhatter@wonderland.com", email.getOutageXat().getEmailAddress());
		assertNotEquals("1", email.getOutageXat().getChannelMemberID());
		assertNotEquals("1", email.getOutageXat().getESID());
		assertNotEquals("1", email.getOutageXat().getLearnMoreLink());
		assertNotEquals("1", email.getOutageXat().getMaxCustOut());
		assertNotEquals("1", email.getOutageXat().getTimeStamp());
		assertNotEquals("1", email.getOutageXat().getCause());
		assertNotEquals("1", email.getOutageXat().getETR());
		assertNotEquals("1", email.getOutageXat().getDuration());
		assertNotEquals("1", email.getOutageXat().getOutage());

	}

	@Test
	public void test_Customer_Email_Notification_Data_ETR() {

		BuildEmail email = new BuildEmail(alertsData3, cpList);

		logger.info("Results of Test Case Customer Email Notification Data ETR Values Being Passed:\n"
				+ email.getOutageXat().getEmailAddress() + " " + email.getOutageXat().getAccountNumber() + " "
				+ email.getOutageXat().getSubject());

		assertNotEquals("", email.getOutageXat().getHashLink());
		assertEquals("890-fgh", email.getOutageXat().getAccountNumber());
		assertEquals("OutageETR", email.getOutageXat().getTemplate());
		assertNotEquals("1", email.getOutageXat().getStreetAddress1());
		assertNotEquals("1", email.getOutageXat().getStreetAddress2());
		assertNotEquals("1", email.getOutageXat().getCity());
		assertNotEquals("1", email.getOutageXat().getState());
		assertNotEquals("1", email.getOutageXat().getZipCode());
		assertNotEquals("1", email.getOutageXat().getFirstName());
		assertNotEquals("1", email.getOutageXat().getAccountNickname());
		assertNotEquals("", email.getOutageXat().getPreheader());
		assertEquals("APCo : Updated Estimated Restoration Time", email.getOutageXat().getSubject());
		assertNotEquals("1", email.getOutageXat().getSubscriberKey());
		assertEquals("alice@wonderland.com", email.getOutageXat().getEmailAddress());
		assertNotEquals("1", email.getOutageXat().getChannelMemberID());
		assertNotEquals("1", email.getOutageXat().getESID());
		assertNotEquals("1", email.getOutageXat().getLearnMoreLink());
		assertNotEquals("1", email.getOutageXat().getMaxCustOut());
		assertNotEquals("1", email.getOutageXat().getTimeStamp());
		assertNotEquals("1", email.getOutageXat().getCause());
		assertNotEquals("1", email.getOutageXat().getETR());
		assertNotEquals("1", email.getOutageXat().getDuration());
		assertNotEquals("1", email.getOutageXat().getOutage());

	}

	@Test
	public void test_Customer_Email_Notification_Data_CANCELLED() {

		BuildEmail email = new BuildEmail(alertsData4, cpList);

		logger.info("Results of Test Case Customer Email Notification Data Cancelled Values Being Passed:\n"
				+ email.getOutageXat().getEmailAddress() + " " + email.getOutageXat().getAccountNumber() + " "
				+ email.getOutageXat().getSubject());

		assertNotEquals("", email.getOutageXat().getHashLink());
		assertEquals("890-fgh", email.getOutageXat().getAccountNumber());
		assertEquals("OutageCancelled", email.getOutageXat().getTemplate());
		assertNotEquals("1", email.getOutageXat().getStreetAddress1());
		assertNotEquals("1", email.getOutageXat().getStreetAddress2());
		assertNotEquals("1", email.getOutageXat().getCity());
		assertNotEquals("1", email.getOutageXat().getState());
		assertNotEquals("1", email.getOutageXat().getZipCode());
		assertNotEquals("1", email.getOutageXat().getFirstName());
		assertNotEquals("1", email.getOutageXat().getAccountNickname());
		assertNotEquals("", email.getOutageXat().getPreheader());
		assertEquals("APCo : Investigation of Outage Completed", email.getOutageXat().getSubject());
		assertNotEquals("1", email.getOutageXat().getSubscriberKey());
		assertEquals("alice@wonderland.com", email.getOutageXat().getEmailAddress());
		assertNotEquals("1", email.getOutageXat().getChannelMemberID());
		assertNotEquals("1", email.getOutageXat().getESID());
		assertNotEquals("1", email.getOutageXat().getLearnMoreLink());
		assertNotEquals("1", email.getOutageXat().getMaxCustOut());
		assertNotEquals("1", email.getOutageXat().getTimeStamp());
		assertNotEquals("1", email.getOutageXat().getCause());
		assertNotEquals("1", email.getOutageXat().getETR());
		assertNotEquals("1", email.getOutageXat().getDuration());
		assertNotEquals("1", email.getOutageXat().getOutage());

	}
	/*
	 * @Test public void test_customer_sms_notifcation_data() { BuildSMS sms = new
	 * BuildSMS(alertsData, cpList);
	 * 
	 * //String smstexString = ""; //assertTrue(smstexString.contains("123") &&
	 * smstexString.contains("happy")); }
	 *
}
*/
