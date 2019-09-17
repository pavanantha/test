package com.aep.cx.billing.alerts.business;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aep.cx.billing.events.BillingAlerts;
import com.aep.cx.billing.events.Disconnected;
import com.aep.cx.billing.events.DisconnectNotice;
import com.aep.cx.billing.events.Header;
import com.aep.cx.preferences.dao.BillingPreferencesDAO;
import com.aep.cx.preferences.dao.CustomerContacts;
import com.aep.cx.preferences.dao.CustomerInfo;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.utils.enums.PreferencesTypes;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class DisconnectNoticeServiceTest {
	@Mock
	AmazonS3 s3Client;

	@Mock
	ObjectMapper mapper;

	@InjectMocks
	BillingAlertsService service;
	
	BillingAlerts discNoticeAlert;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		discNoticeAlert = new BillingAlerts(
				"MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTDISC-NOTICE     96042205508964548095    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx       565.61      xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx       2001-01-01          .00          xxxxxxxxxxxxxxxxxxxxxxxxxxxx           .002001-01-010001-01-01       220.062018-03-28       345.552018-03-28         345.55       565.612018-04-05-12.50.40.058097");
	}

	@Test
	void testParseDisconnectNoticeData() throws JsonParseException, JsonMappingException, IOException {
		S3Event s3Event = mock(S3Event.class);
		S3ObjectInputStream s3ObjectStream = mock(S3ObjectInputStream.class);
		setupEventMocks(s3Client, s3Event, s3ObjectStream);

		DisconnectNotice discNotice = discNoticeAlert.getDiscNotice();
		doReturn(new ArrayList<>(Collections.singletonList(discNotice))).when(mapper).readValue(eq(s3ObjectStream), any(TypeReference.class));
		assertTrue(service.parseS3EventData(s3Event, new DisconnectNotice()).contains(discNotice));
	}

	@Test
	void testBuildDisconnectNoticeContent() {
		service = new BillingAlertsService() {
			@Override
			public <T extends Header> HashMap<String, ArrayList<String>> buildEmail(T disconnectedNotice, CustomerPreferences prefs){
				return null;
			}
			
			@Override
			public <T extends Header> HashMap<String, ArrayList<String>> buildSMS(T disconnectedNotice, CustomerPreferences prefs) {
				return null;
			}
		};
		
		DisconnectNotice discNotice = discNoticeAlert.getDiscNotice();
		service.dao = mock(BillingPreferencesDAO.class);
		doReturn(getCustomerPreferences()).when(service.dao).getCustomerPreferences(eq("02040096097"), eq(PreferencesTypes.BILLINGPAYMENT.toString()));
		
		assertTrue(service.buildAlertContent(new ArrayList<>(Collections.singletonList(discNotice))).contains("Successfully processed"));
	}

	@Test
	void testDisconnectNoticeBuildEmail() {
		service = new BillingAlertsService() {
			@Override
			public void loadNotificationAlerts2S3(String bucketName, String bucketKey, ArrayList<String> alertsList,
					String historyBucketName, ArrayList<String> historyList) {
				// do nothing
			}
		};
		DisconnectNotice discNotice = discNoticeAlert.getDiscNotice();
		HashMap<String, ArrayList<String>> result = service.buildEmail(discNotice, getCustomerPreferences());
		assertTrue(result.get("xat").get(0).contains("abc@abc.com"));
		assertTrue(result.get("xat").get(0).contains("DisconnectApproaching"));
	}

	@Test
	void testDisconnectNoticeBuildSMS() {
		service = new BillingAlertsService() {
			@Override
			public void loadNotificationAlerts2S3(String bucketName, String bucketKey, ArrayList<String> alertsList,
					String historyBucketName, ArrayList<String> historyList) {
				// do nothing
			}
		};
		DisconnectNotice discNotice = discNoticeAlert.getDiscNotice();
		HashMap<String, ArrayList<String>> result = service.buildSMS(discNotice, getCustomerPreferences());
		assertTrue(result.get("sms").get(0).contains("5508"));
		assertFalse(result.get("sms").get(0).contains("abc@abc.com"));
	}

	@Disabled("needs to implement")
	@Test
	void testGetEmailTemplate() {
		fail("Not yet implemented");
	}

	@Disabled("needs to implement")
	@Test
	void testGetSMSTemplate() {
		fail("Not yet implemented");
	}

	private static void setupEventMocks(AmazonS3 s3Client, S3Event s3Event, S3ObjectInputStream s3ObjectStream) {
		S3EventNotification.S3EventNotificationRecord record = mock(
				S3EventNotification.S3EventNotificationRecord.class);
		List<S3EventNotification.S3EventNotificationRecord> records = Collections.singletonList(record);
		doReturn(records).when(s3Event).getRecords();

		S3EventNotification.S3Entity entity = mock(S3EventNotification.S3Entity.class);
		doReturn(entity).when(record).getS3();

		S3EventNotification.S3BucketEntity bucket = mock(S3EventNotification.S3BucketEntity.class);
		doReturn("bucket-name").when(bucket).getName();
		doReturn(bucket).when(entity).getBucket();

		S3EventNotification.S3ObjectEntity objectEntity = mock(S3EventNotification.S3ObjectEntity.class);
		doReturn("key").when(objectEntity).getKey();
		doReturn(objectEntity).when(entity).getObject();

		S3Object object = mock(S3Object.class);
		doReturn(object).when(s3Client)
				.getObject(argThat(r -> r.getBucketName().equals("bucket-name") && r.getKey().equals("key")));

		doReturn(s3ObjectStream).when(object).getObjectContent();
	}
	
	public CustomerPreferences getCustomerPreferences() {
		CustomerPreferences pref = new CustomerPreferences();
		
		CustomerInfo custInfo = new CustomerInfo();
		custInfo.setAccountNumber("02040096097");
		custInfo.setPremiseNumber("020400960");
		custInfo.setName("Name");
		custInfo.setCity("City");
		custInfo.setState("OH");
		custInfo.setStreetAddress("Street");
		custInfo.setZipCode("43016");
		 
		pref.setCustomerInfo(custInfo);
		
		CustomerContacts contact = new CustomerContacts();
		contact.setEndPoint("9999999999");
		contact.setWebId("webId");
		
		pref.setCustomerContacts(new ArrayList<>(Collections.singletonList(contact)));
		contact = new CustomerContacts();
		contact.setWebId("webId");
		contact.setEndPoint("abc@abc.com");
		pref.getCustomerContacts().add(contact);
		
		return pref;
	}

}
