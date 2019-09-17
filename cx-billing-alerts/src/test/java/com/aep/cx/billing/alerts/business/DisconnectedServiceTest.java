package com.aep.cx.billing.alerts.business;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aep.cx.billing.events.BillingAlerts;
import com.aep.cx.billing.events.Disconnected;
import com.aep.cx.billing.events.DisconnectNotice;
import com.aep.cx.billing.events.Header;
import com.aep.cx.billing.events.Reconnected;
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

class DisconnectedServiceTest {
	
	@Mock
	AmazonS3 s3Client;

	@Mock
	ObjectMapper mapper;
	
	@Mock
	BillingPreferencesDAO dao;

	@InjectMocks
	BillingAlertsService service;
	
	BillingAlerts disconnectedAlerts;
	BillingAlerts discNoticeAlert;
	BillingAlerts reconnectedAlert;
	
	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		disconnectedAlerts = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTDISCONNECTED    02040096097020400960    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                 901.712018-11-  2018-11-21-23.00.00.074051 000000901.71  xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                   .00 000000000.00    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx  2001-01-01          .00      xxxxxxxxxxxxxxxxxxxxxxxxxxx                .002001-01-012001-01-01          .002001-01-01          .002001-01-01            .00          .002018-11-29-12.28.40.622977");
		discNoticeAlert = new BillingAlerts(
				"MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTDISC-NOTICE     96042205508964548095    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx       565.61      xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx       2001-01-01          .00          xxxxxxxxxxxxxxxxxxxxxxxxxxxx           .002001-01-010001-01-01       220.062018-03-28       945.552018-03-28         945.55       565.612018-04-05-12.50.40.058097");
		reconnectedAlert = new BillingAlerts("MAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxEXPALERTRECONNECTED     96042205508960422055    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx           000000000.002018-09-12-23.00.00.177924                         xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx    NNNN     xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx              NNNN       xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx       NNNN            .00 000000000.00    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx  2001-01-01          .00      xxxxxxxxxxxxxxxxxxxxxxxxxxx                .002001-01-012001-01-01          .002001-01-01          .002001-01-01            .00          .002018-11-07-10.12.48.715871");
	}

	@Test
	void testParseDisconnectedData() throws JsonParseException, JsonMappingException, IOException {
		
		Disconnected disconnected = disconnectedAlerts.getDisconnected();
		
		S3Event s3Event = mock(S3Event.class);
		S3ObjectInputStream s3ObjectStream = mock(S3ObjectInputStream.class);
		setupEventMocks(s3Client, s3Event, s3ObjectStream);
		
		doReturn(new ArrayList<>(Collections.singletonList(disconnected))).when(mapper).readValue(eq(s3ObjectStream), any(TypeReference.class));
		assertTrue(service.parseS3EventData(s3Event, disconnected).contains(disconnected));
	}

	@Test
	void testBuildDisconnectedContent() throws JsonParseException, JsonMappingException, IOException {
		service = new BillingAlertsService() {
			@Override
			public <T extends Header> HashMap<String, ArrayList<String>> buildEmail(T disconnected, CustomerPreferences prefs){
				return null;
			}
			
			@Override
			public <T extends Header> HashMap<String, ArrayList<String>> buildSMS(T objType, CustomerPreferences prefs) {
				return null;
			}
		};
		
		Disconnected disconnected = disconnectedAlerts.getDisconnected();
		service.dao = mock(BillingPreferencesDAO.class);
		doReturn(getCustomerPreferences()).when(service.dao).getCustomerPreferences(eq("02040096097"), eq(PreferencesTypes.BILLINGPAYMENT.toString()));
		
		assertTrue(service.buildAlertContent(new ArrayList<>(Collections.singletonList(disconnected))).contains("Successfully processed"));
	}

	@Test
	void testBuildEmail() {
		service = new BillingAlertsService() {
			@Override
			public void loadNotificationAlerts2S3(String bucketName, String bucketKey, ArrayList<String> alertsList,
					String historyBucketName, ArrayList<String> historyList) {
				// do nothing
			}
		};
		Disconnected disconnected = disconnectedAlerts.getDisconnected();
		HashMap<String, ArrayList<String>> result = service.buildEmail(disconnected, getCustomerPreferences());
		assertTrue(result.get("xat").get(0).contains("abc@abc.com"));
	}

	@Test
	void testDisconnectedBuildSMS() {
		service = new BillingAlertsService() {
			@Override
			public void loadNotificationAlerts2S3(String bucketName, String bucketKey, ArrayList<String> alertsList,
					String historyBucketName, ArrayList<String> historyList) {
				// do nothing
			}
		};
		Disconnected disconnected = disconnectedAlerts.getDisconnected();
		disconnected.setOpcoAbbreviatedName(service.getOpcoAbbreviatedName(disconnected));
		HashMap<String, ArrayList<String>> result = service.buildSMS(disconnected, getCustomerPreferences());
		assertTrue(result.get("sms").get(0).contains("9999999999")); 
	}
	
	@Test
	void testNonSWEPCODisconnectedBuildSMS() {
		service = new BillingAlertsService() {
			@Override
			public void loadNotificationAlerts2S3(String bucketName, String bucketKey, ArrayList<String> alertsList,
					String historyBucketName, ArrayList<String> historyList) {
				// do nothing
			}
		};
		Disconnected disconnected = disconnectedAlerts.getDisconnected();
		CustomerPreferences pref = getCustomerPreferences();
		pref.getCustomerInfo().setPremiseNumber("020400960");
		disconnected.setOpcoAbbreviatedName(service.getOpcoAbbreviatedName(disconnected));
		
		HashMap<String, ArrayList<String>> result = service.buildSMS(disconnected, pref);
		assertTrue(result.get("sms").get(0).contains("your breakers to the OFF position"));
	}
	
	@Test
	void testDisconnectedAmountThresholdBuildSMS() {
		service = new BillingAlertsService() {
			@Override
			public void loadNotificationAlerts2S3(String bucketName, String bucketKey, ArrayList<String> alertsList,
					String historyBucketName, ArrayList<String> historyList) {
				// do nothing
			}
		};
		
		Disconnected disconnected = disconnectedAlerts.getDisconnected();
		//Setting disconnected amount lessthan $20
		disconnected.setDisconnectedAmount(19);
		disconnected.setOpcoAbbreviatedName(service.getOpcoAbbreviatedName(disconnected));
		CustomerPreferences pref = getCustomerPreferences();
		
		HashMap<String, ArrayList<String>> result = service.buildSMS(disconnected, pref);
		assertEquals(null, result);;
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
		service.buildAlertContent(new ArrayList<>(Collections.singletonList(discNotice)));
		HashMap<String, ArrayList<String>> result = service.buildSMS(discNotice, getCustomerPreferences());
		assertTrue(result.get("sms").get(0).contains("5508"));
		assertFalse(result.get("sms").get(0).contains("abc@abc.com"));
	}
	
	@Test
	void testBuildReconnectContent() {
		service = new BillingAlertsService() {
			@Override
			public <T extends Header> HashMap<String, ArrayList<String>> buildEmail(T reconnected, CustomerPreferences prefs){
				return null;
			}
			
			@Override
			public <T extends Header> HashMap<String, ArrayList<String>> buildSMS(T reconnected, CustomerPreferences prefs) {
				return null;
			}
		};
		
		Reconnected reconnect = reconnectedAlert.getReconnected();
		service.dao = mock(BillingPreferencesDAO.class);
		doReturn(getCustomerPreferences()).when(service.dao).getCustomerPreferences(eq("02040096097"), eq(PreferencesTypes.BILLINGPAYMENT.toString()));
		
		assertTrue(service.buildAlertContent(new ArrayList<>(Collections.singletonList(reconnect))).contains("Successfully processed"));
	}
	
	@Test
	void testReconnectedBuildEmail() {
		service = new BillingAlertsService() {
			@Override
			public void loadNotificationAlerts2S3(String bucketName, String bucketKey, ArrayList<String> alertsList,
					String historyBucketName, ArrayList<String> historyList) {
				// do nothing
			}
		};
		Reconnected reconnect = reconnectedAlert.getReconnected();
		HashMap<String, ArrayList<String>> result = service.buildEmail(reconnect, getCustomerPreferences());
		assertTrue(result.get("xat").get(0).contains("abc@abc.com"));
		assertTrue(result.get("xat").get(0).contains("Your power has been reconnected"));
	}

	@Test
	void testReconnectedBuildSMS() {
		service = new BillingAlertsService() {
			@Override
			public void loadNotificationAlerts2S3(String bucketName, String bucketKey, ArrayList<String> alertsList,
					String historyBucketName, ArrayList<String> historyList) {
				// do nothing
			}
		};
		Reconnected reconnect = reconnectedAlert.getReconnected();
		
		service.buildAlertContent(new ArrayList<>(Collections.singletonList(reconnect)));
	
		HashMap<String, ArrayList<String>> result = service.buildSMS(reconnect, getCustomerPreferences());
		assertTrue(result.get("sms").get(0).contains("999999"));
		assertFalse(result.get("sms").get(0).contains("abc@abc.com"));
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
