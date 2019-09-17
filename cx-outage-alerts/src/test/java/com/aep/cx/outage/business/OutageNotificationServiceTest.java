package com.aep.cx.outage.business;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


class OutageNotificationServiceTest {

	@Mock
	private OutageNotificationManager notificationManager;
	@Mock
	private AmazonS3 s3Client;
	@Mock
	private LoadData2S3 loadData;
	@Mock
	private ObjectMapper mapper;
	@InjectMocks
	private OutageNotificationService service;

	private OutageEvent outageEvent;
	private String batchKey;
	private String bucketName;
	private HashMap<String, ArrayList<CustomerPreferences>> preferences;
	private List<String> smsPayload;
	private List<String> emailPayload;
	private ArrayList<OutageEvent> outageEventArrayList = new ArrayList<OutageEvent>(Arrays.asList(outageEvent));

	@BeforeEach
	void setup() {
		MockitoAnnotations.initMocks(this);
		outageEvent = new OutageEvent();
		batchKey = "ehh";
		bucketName = "a-bucket";
		preferences = new HashMap<>();
		smsPayload = Arrays.asList("sms", "payload");
		emailPayload = Arrays.asList("email", "payload");
	}

	@Disabled("need to fix - methods changed")
	@Test
	void processRecord() {
		doReturn(emailPayload).when(notificationManager).buildEmailNotification(any(), anyString(), anyString(), any());
		doReturn(smsPayload).when(notificationManager).buildSMSNotification(any(), anyString(), anyString(), any());

		service.processRecord(outageEvent, batchKey, bucketName, preferences);

		verify(loadData).loadData(eq("email"), eq(batchKey), eq(emailPayload));
		verify(loadData).loadData(eq("sms"), eq(batchKey), eq(smsPayload));
		verify(notificationManager).buildEmailNotification(eq(outageEvent), eq(batchKey), eq(bucketName), eq(preferences));
		verify(notificationManager).buildSMSNotification(eq(outageEvent), eq(batchKey), eq(bucketName), eq(preferences));
	}

	@Disabled("need to fix - methods changed")
	@Test
	void parseRecord() throws IOException {
		S3Event s3Event = mock(S3Event.class);
		S3ObjectInputStream s3ObjectStream = mock(S3ObjectInputStream.class);
		setupMocks(s3Client, s3Event, s3ObjectStream);
		//doReturn(outageEvent).when(mapper).readValue(eq(s3ObjectStream), eq(OutageEvent.class));
		doReturn(outageEventArrayList).when(mapper).readValue(eq(s3ObjectStream), eq(outageEventArrayList.getClass()));

		OutageEvent result = service.parseRecord(s3Event);

		assertTrue(Objects.deepEquals(result, outageEvent));
	}

	@Disabled("need to fix - methods changed")
	@Test
	void parseRecordError() throws IOException {
		S3Event s3Event = mock(S3Event.class);
		S3ObjectInputStream s3ObjectStream = mock(S3ObjectInputStream.class);
		setupMocks(s3Client, s3Event, s3ObjectStream);
		doThrow(IOException.class).when(mapper).readValue(eq(s3ObjectStream), eq(OutageEvent.class));

		OutageEvent result = service.parseRecord(s3Event);

		assertNull(result);
	}

	private static void setupMocks(AmazonS3 s3Client, S3Event s3Event, S3ObjectInputStream s3ObjectStream) {
		S3EventNotification.S3EventNotificationRecord record = mock(S3EventNotification.S3EventNotificationRecord.class);
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
		doReturn(object).when(s3Client).getObject(argThat(r -> r.getBucketName().equals("bucket-name") && r.getKey().equals("key")));

		doReturn(s3ObjectStream).when(object).getObjectContent();
	}
}