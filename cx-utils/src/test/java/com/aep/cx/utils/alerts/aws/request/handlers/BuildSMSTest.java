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
import com.aep.cx.utils.alerts.notification.msessages.BuildSMS;

/*
public class BuildSMSTest {

	private ArrayList<AlertsNotificationData> alertsData1 = new ArrayList<>();
	AlertsNotificationData ad1;
	private ArrayList<AlertsNotificationData> alertsData2 = new ArrayList<>();
	AlertsNotificationData ad2;
	private ArrayList<AlertsNotificationData> alertsData3 = new ArrayList<>();
	AlertsNotificationData ad3;
	private ArrayList<AlertsNotificationData> alertsData4 = new ArrayList<>();
	AlertsNotificationData ad4;
	private ArrayList<AlertsNotificationData> alertsData5 = new ArrayList<>();
	AlertsNotificationData ad5;

	private HashMap<String, ArrayList<CustomerPreferences>> cpList;

	ObjectMapper mapper = new ObjectMapper();
	TypeReference<Map<String, ArrayList<CustomerPreferences>>> cpListType = new TypeReference<Map<String, ArrayList<CustomerPreferences>>>() {
	};

	HashMap<String, ArrayList<CustomerPreferences>> loadPreferencesTestData(String resourceName) throws IOException {
		return mapper.readValue(IOUtils.resourceToString(resourceName, Charset.defaultCharset()), cpListType);

	}

	static final Logger logger = LogManager.getLogger(BuildSMSTest.class);

	@Before
	public void setUp() throws Exception {
		// Email Alert Type Predicted
		ad1 = new AlertsNotificationData();
		ad1.setPremiseNumber("953456666");
		ad1.setAccountNumber("890-fgh");
		ad1.setAlertName("predicted");
		ad1.setOutageSimpleCause("unknown");
		ad1.setEtrType("g");
		ad1.setOutageEtrTime(DateTime.now().plusDays(3));
		ad1.setOutageNumber(02306505);
		alertsData1.add(ad1);

		// Email Alert Type Predicted NO ETR
		ad5 = new AlertsNotificationData();
		ad5.setPremiseNumber("953456666");
		ad5.setAccountNumber("890-fgh");
		ad5.setAlertName("confirmed");
		ad5.setOutageSimpleCause("unknown");
		ad5.setEtrType("u");
		ad5.setOutageEtrTime(DateTime.now().plusDays(3));
		ad5.setOutageNumber(02306505);
		alertsData5.add(ad5);

		// Email Alert Type Restored
		ad2 = new AlertsNotificationData();
		ad2.setPremiseNumber("019999998");
		ad2.setAlertName("restored");
		ad2.setOutageSimpleCause("unknown");
		ad2.setEtrType("g");
		ad2.setOutageEtrTime(DateTime.now().plusDays(3));
		ad2.setOutageNumber(02306505);
		alertsData2.add(ad2);

		// Email Alert Type ETR
		ad3 = new AlertsNotificationData();
		ad3.setPremiseNumber("019999997");
		ad3.setAlertName("etr");
		ad3.setOutageSimpleCause("unknown");
		ad3.setEtrType("g");
		ad3.setOutageEtrTime(DateTime.now().plusDays(3));
		// ad3.setOutageNumber(02306505);
		alertsData3.add(ad3);

		// Email Alert Type Cancelled
		ad4 = new AlertsNotificationData();
		ad4.setPremiseNumber("019999997");
		ad4.setAlertName("cancelled");
		ad4.setOutageSimpleCause("unknown");
		ad4.setEtrType("g");
		ad4.setOutageEtrTime(DateTime.now().plusDays(3));
		ad4.setOutageNumber(02306505);
		alertsData4.add(ad4);

		// Get Customer Prefernces From a Json File Dedicated To Test Customers
		cpList = loadPreferencesTestData("/testpreferences.json");
	}

	@Test
	public void test_Customer_SMS_Notification_Data_PREDICTED() {

		BuildSMS sms = new BuildSMS(alertsData1, cpList);
		assertTrue(sms.getSmsPayload().toString().contains("6788871526"));
		assertTrue(sms.getSmsPayload().toString().contains("PSO"));
		assertTrue(sms.getSmsPayload().toString().contains("AWS Test: PSO: Outage in area of"));
		assertTrue(sms.getSmsPayload().toString().contains("Power estimated to be on by"));
		assertTrue(sms.getSmsPayload().toString().contains("Will update if time changes"));

	}

	@Test
	public void test_Customer_SMS_Notification_Data_PREDICTEDNOETR() {

		BuildSMS sms = new BuildSMS(alertsData5, cpList);
		assertTrue(sms.getSmsPayload().toString().contains("6788871526"));
		assertTrue(sms.getSmsPayload().toString().contains("PSO"));
		assertTrue(sms.getSmsPayload().toString().contains("AWS Test: PSO: Outage in area of"));
		assertTrue(
				sms.getSmsPayload().toString().contains("Estimated time restoration will be updated when available"));

	}

	@Test
	public void test_Customer_SMS_Notification_Data_ETR() {

		BuildSMS sms = new BuildSMS(alertsData3, cpList);
		assertTrue(sms.getSmsPayload().toString().contains("16148051126"));
		assertTrue(sms.getSmsPayload().toString().contains("APCo"));
		assertTrue(sms.getSmsPayload().toString().contains("AWS Test: APCo Update: Estimate for power on is"));
		assertTrue(sms.getSmsPayload().toString().contains("Thank you for your patience"));

	}

	@Test
	public void test_Customer_SMS_Notification_Data_RESTORED() {

		BuildSMS sms = new BuildSMS(alertsData2, cpList);
		assertTrue(sms.getSmsPayload().toString().contains("16149499232"));
		assertTrue(sms.getSmsPayload().toString().contains("APCo"));
		assertTrue(sms.getSmsPayload().toString().contains("AWS Test: APCo Power is on in area of"));
		assertTrue(sms.getSmsPayload().toString().contains("Thank you for your patience"));
		// We need to change the template to Power is ON in area of
	}

	@Test
	public void test_Customer_SMS_Notification_Data_CANCELLED() {

		BuildSMS sms = new BuildSMS(alertsData4, cpList);
		assertTrue(sms.getSmsPayload().toString().contains("16148051126"));
		assertTrue(sms.getSmsPayload().toString().contains("APCo"));
		assertTrue(sms.getSmsPayload().toString().contains("AWS Test: APCo Power is ON in area of"));
		assertTrue(sms.getSmsPayload().toString().contains("We apologize for any incovenience"));

	}
}
*/