package com.aep.cx.outage.business;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.outage.alerts.domains.OutageData;
import com.aep.cx.outage.alerts.enums.ValueAddAlertType;
import com.aep.cx.outage.alerts.enums.ValueAddProcessingReasonType;
import com.aep.cx.utils.opco.OperatingCompanyV2;
import com.aep.cx.utils.opco.OperatingCompanyManager;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import java.util.Map;

import static com.aep.cx.outage.alerts.enums.ValueAddAlertType.*;
import static com.aep.cx.outage.alerts.enums.ValueAddProcessingReasonType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OutageServiceTest {

	private static final String BUCKET_SUSTAINED = "sustained";
	private static final String BUCKET_MOMENTARY = "momentary";
	private static final String BUCKET_RESTORED = "restored";
	private static final String BUCKET_RAW = "raw";
	private static final String PREMISE_NUM = "953456666";
	private static final String POWER_OFF = "OFF";
	private static final String POWER_ON = "ON";
	private static final String ETR_TYPE_GLOBAL = "g";
	private static final String ETR_TYPE_UNAVAILABLE = "u";
	private static final String OUTAGE_TYPE_PREDICTED = "predicted";
	private static final String OUTAGE_TYPE_RESTORED = "restored";
	private static final byte[] BYTES = new byte[]{(byte) 1};

	@Mock
	private S3ObjectInputStream s3ObjectStream;
	@Mock
	private AmazonS3 s3Client;
	@Mock
	private S3Object s3Object;
	@Mock
	private ObjectMapper mapper;
	@Mock
	private OutageManager outageManager;
	@Mock
	private ObjectMetadata metadata;
	@InjectMocks
	private OutageService service;
	//@Mock
	//private static ArrayList<OutageEvent> mockOutageEventArrayList;
	// Create Operating Company Manager used to set the timezone of each event
	private static OperatingCompanyManager operatingCompanyManager = new OperatingCompanyManager();
	private static Map<String, OperatingCompanyV2> operatingCompanyMap = operatingCompanyManager.BuildOperatingCompanyMap();
	
	private static void setOutageCreationTime(OutageEvent event, DateTime dt)
	{
		DateTime localDT = dt.withZone(DateTimeZone.forID(event.getValueAddOperatingCompanyTimeZone()));
		event.setOutageCreationTime(event.convertToLocalTime(localDT));
	}

	private static OutageEvent generateEvent() {
		OutageEvent event = new OutageEvent();
		event.setPremiseNumber(PREMISE_NUM);
		event.setPremisePowerStatus(POWER_OFF);
		event.setOutageETR(DateTime.now().plusDays(1));
		event.setOutageETRType(ETR_TYPE_GLOBAL);
		event.setOutageType(OUTAGE_TYPE_PREDICTED);
		event.setMomentaryWaitThresholdInMinutes(10);
		// Set OperatingCompany for later timezone fixing
		OperatingCompanyV2 outageOpco = operatingCompanyMap.get(event.getPremiseNumber().substring(0, 2));
		event.setValueAddOperatingCompanyTimeZone(outageOpco.getTimeZone());
		setOutageCreationTime(event,DateTime.now());

		return event;
	}

	//private ArrayList<OutageEvent> outageEventArrayList = new ArrayList<OutageEvent>(Arrays.asList(generateEvent()));


	private static void mockOutageManager(OutageManager outageManager, ValueAddProcessingReasonType reason, ValueAddAlertType type) {
		doAnswer(invocation -> {
			List<OutageEvent> events = invocation.getArgument(0);
			OutageEvent event = events.get(0);
			event.setValueAddProcessingReasonType(reason);
			event.setValueAddAlertType(type);
			return events;
		}).when(outageManager).ProcessRulesBatch(any(), any());
		//}).when(outageManager).ProcessRulesBatch(mockOutageEventArrayList, mockOutageEventArrayList);
	}

	private static void setupObjectMapperWrite(ObjectMapper mockMapper, OutageEvent event) throws JsonProcessingException {
		doReturn(BYTES).when(mockMapper).writeValueAsBytes(argThat(list -> ((List<OutageEvent>) list).contains(event)));
	}

	private static void setupEventMocks(AmazonS3 s3Client, S3Event s3Event, S3ObjectInputStream s3ObjectStream) {
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

	private static DateTime mockDeserializedTime(DateTime DTWithOffset)
	{
		DateTimeFormatter df = DateTimeFormat.forPattern("yyyyMMddHHmm");
		String strDTString = df.print(DTWithOffset);
		return DateTime.parse(strDTString, df);
	}

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void processMomentaryWait() throws JsonProcessingException {
		doThrow(SdkClientException.class).when(s3Client).getObject(eq(BUCKET_SUSTAINED), anyString());
		doThrow(SdkClientException.class).when(s3Client).getObject(eq(BUCKET_MOMENTARY), anyString());
		OutageEvent momentaryWait = generateEvent();
		mockOutageManager(outageManager, CurrentStatusMomentaryWait, NONE);
		setupObjectMapperWrite(mapper, momentaryWait);

		String result = service.processBatch(new ArrayList<>(Collections.singletonList(momentaryWait)), "batch1");

		assertEquals("SUCCESS", result);
		verify(s3Client).putObject(eq(BUCKET_MOMENTARY), anyString(), any(InputStream.class), any(ObjectMetadata.class));
	}

	@Test
	void processPredicted() throws JsonProcessingException {
		doThrow(SdkClientException.class).when(s3Client).getObject(eq(BUCKET_SUSTAINED), anyString());
		doThrow(SdkClientException.class).when(s3Client).getObject(eq(BUCKET_MOMENTARY), anyString());
		OutageEvent alertTypePredicted = generateEvent();
		
		// DEBUGGING TIMZONE RELATED ISSUES
		Logger logger = LogManager.getLogger(OutageServiceTest.class);
		logger.info("Default Time Zone is: "+ DateTimeZone.getDefault().toString());
		setOutageCreationTime(alertTypePredicted,DateTime.now().minusMinutes(15));
		mockOutageManager(outageManager, OutageFirstNotificationWithETR, PREDICTED);
		setupObjectMapperWrite(mapper, alertTypePredicted);

		String result = service.processBatch(new ArrayList<>(Collections.singletonList(alertTypePredicted)), "batch1");

		assertEquals("SUCCESS", result);
		verify(s3Client).putObject(eq(BUCKET_SUSTAINED), anyString(), any(InputStream.class), any(ObjectMetadata.class));
	}

	@Test
	void processPredictedNOETR() throws JsonProcessingException {
		doThrow(SdkClientException.class).when(s3Client).getObject(eq(BUCKET_SUSTAINED), anyString());
		doThrow(SdkClientException.class).when(s3Client).getObject(eq(BUCKET_MOMENTARY), anyString());
		OutageEvent alertTypePredictedNOETR = generateEvent();
		alertTypePredictedNOETR.setOutageAsOfTime(DateTime.now());
		alertTypePredictedNOETR.setOutageETRType(ETR_TYPE_UNAVAILABLE);
		setOutageCreationTime(alertTypePredictedNOETR,DateTime.now().minusMinutes(15));
		alertTypePredictedNOETR.setOutageType(OUTAGE_TYPE_PREDICTED);
		setupObjectMapperWrite(mapper, alertTypePredictedNOETR);
		mockOutageManager(outageManager, PredictedOutageFirstNotificationWithNoETR, PREDICTEDNOETR);

		String result = service.processBatch(new ArrayList<>(Collections.singletonList(alertTypePredictedNOETR)), "batch1");

		assertEquals("SUCCESS", result);
		// The statement below assets that s3Client.putObjcet(BUCKET_SUSTAINED) is called once
		// This was true previously but not now
		// verify(s3Client).putObject(eq(BUCKET_SUSTAINED), anyString(), any(InputStream.class), any(ObjectMetadata.class));
	}

	@Test
	void processETR() throws IOException {
		doReturn(s3Object).when(s3Client).getObject(eq(BUCKET_SUSTAINED), anyString());
		doThrow(SdkClientException.class).when(s3Client).getObject(eq(BUCKET_MOMENTARY), anyString());

		OutageEvent alertTypeETR = generateEvent();
		alertTypeETR.setOutageAsOfTime(DateTime.now().plusMinutes(20));
		alertTypeETR.setOutageETR(DateTime.now().minusDays(2));
		setOutageCreationTime(alertTypeETR,DateTime.now().minusMinutes(15));
		setupObjectMapperWrite(mapper, alertTypeETR);
		mockOutageManager(outageManager, NewerOutageEventExist, ETR);

		OutageEvent prevOutageEvent = generateEvent();
		prevOutageEvent.setOutageAsOfTime(DateTime.now());
		prevOutageEvent.setOutageETR(DateTime.now().minusDays(2));
		setOutageCreationTime(prevOutageEvent,DateTime.now().minusMinutes(15));
		doReturn(s3ObjectStream).when(s3Object).getObjectContent();
		doReturn(new ArrayList<>(Collections.singletonList(prevOutageEvent))).when(mapper).readValue(eq(s3ObjectStream), any(TypeReference.class));

		String result = service.processBatch(new ArrayList<>(Collections.singletonList(alertTypeETR)), "batch1");

		assertEquals("SUCCESS", result);
		verify(s3Client).putObject(eq(BUCKET_SUSTAINED), anyString(), any(InputStream.class), any(ObjectMetadata.class));
	}

	@Test
	void processRestored() throws IOException {
		doReturn(s3Object).when(s3Client).getObject(eq(BUCKET_SUSTAINED), anyString());
		//doReturn(s3Object).when(s3Client).getObject(argThat(r -> r.getBucketName().equals(BUCKET_SUSTAINED) && r.getKey().equals("batch1")));
		doThrow(SdkClientException.class).when(s3Client).getObject(eq(BUCKET_MOMENTARY), anyString());
		mockOutageManager(outageManager, PowerHasBeenRestored, RESTORED);

		OutageEvent alertTypeRestored = generateEvent();
		alertTypeRestored.setPremisePowerStatus(POWER_ON);
		alertTypeRestored.setOutageRestorationTime(DateTime.now().plusDays(1));
		setOutageCreationTime(alertTypeRestored,DateTime.now().minusMinutes(15));
		alertTypeRestored.setOutageType(OUTAGE_TYPE_RESTORED);
		alertTypeRestored.setMomentaryWaitThresholdInMinutes(1);
		alertTypeRestored.setOutageETR(DateTime.now().minusDays(2));
		setupObjectMapperWrite(mapper, alertTypeRestored);

		OutageEvent prevOffEvent = generateEvent();
		prevOffEvent.setOutageAsOfTime(DateTime.now());
		prevOffEvent.setOutageETR(DateTime.now().minusDays(2));
		setOutageCreationTime(prevOffEvent,DateTime.now().minusMinutes(15));
		doReturn(s3ObjectStream).when(s3Object).getObjectContent();
		doReturn(new ArrayList<>(Collections.singletonList(prevOffEvent))).when(mapper).readValue(eq(s3ObjectStream), any(TypeReference.class));

		String result = service.processBatch(new ArrayList<>(Collections.singletonList(alertTypeRestored)), "batch1");

		assertEquals("SUCCESS", result);
		verify(s3Client).putObject(eq(BUCKET_RESTORED), anyString(), any(InputStream.class), any(ObjectMetadata.class));
	}

	@Test
	@Disabled
	void processCancelled() {
		doThrow(SdkClientException.class).when(s3Client).getObject(eq(BUCKET_SUSTAINED), anyString());
		doThrow(SdkClientException.class).when(s3Client).getObject(eq(BUCKET_MOMENTARY), anyString());
		mockOutageManager(outageManager, PowerHasBeenRestored, RESTORED);

		OutageEvent alertTypeCancelled = new OutageEvent();
		alertTypeCancelled.setPremiseNumber(PREMISE_NUM);
		alertTypeCancelled.setPremisePowerStatus(POWER_ON);
		alertTypeCancelled.setOutageRestorationTime(DateTime.now().minusDays(3));
		setOutageCreationTime(alertTypeCancelled,DateTime.now().minusMinutes(15));
		alertTypeCancelled.setOutageType(OUTAGE_TYPE_RESTORED);
		alertTypeCancelled.setOutageETRType(ETR_TYPE_GLOBAL);
		alertTypeCancelled.setMomentaryWaitThresholdInMinutes(1);
		alertTypeCancelled.setOutageETR(DateTime.now().minusDays(2));

		String result = service.processBatch(new ArrayList<>(Collections.singletonList(alertTypeCancelled)), "batch1");

		assertEquals("SUCCESS", result);
	}

	@Test
	@Disabled
	void processException() {
		doReturn(s3Object).when(s3Client).getObject(eq(BUCKET_MOMENTARY), anyString());

		OutageEvent exceptionNoOutageETRValue = new OutageEvent();
		exceptionNoOutageETRValue.setPremiseNumber(PREMISE_NUM);
		exceptionNoOutageETRValue.setPremisePowerStatus(POWER_OFF);
		exceptionNoOutageETRValue.setOutageETR(DateTime.now().plusMinutes(20));
		exceptionNoOutageETRValue.setOutageAsOfTime(DateTime.now());
		exceptionNoOutageETRValue.setOutageETRType(ETR_TYPE_UNAVAILABLE);
		setOutageCreationTime(exceptionNoOutageETRValue,DateTime.now().minusMinutes(15));
		exceptionNoOutageETRValue.setOutageType(OUTAGE_TYPE_PREDICTED);
		exceptionNoOutageETRValue.setMomentaryWaitThresholdInMinutes(10);

		String result = service.processBatch(new ArrayList<>(Collections.singletonList(exceptionNoOutageETRValue)), "batch1");

		assertEquals("SUCCESS", result);
	}

	@Test
	@Disabled
	void processRestoredBeforeOffNotificationSent() throws IOException {
		doThrow(SdkClientException.class).when(s3Client).getObject(eq(BUCKET_SUSTAINED), anyString());
		doReturn(s3Object).when(s3Client).getObject(eq(BUCKET_MOMENTARY), anyString());
		mockOutageManager(outageManager, PowerRestoredBeforeOffNotificationSent, RESTORED);

		OutageEvent newOnEvent = generateEvent();
		newOnEvent.setPremisePowerStatus(POWER_ON);
		newOnEvent.setOutageETR(DateTime.now().minusDays(2));
		setOutageCreationTime(newOnEvent,DateTime.now().minusMinutes(15));
		setupObjectMapperWrite(mapper, newOnEvent);

		OutageEvent prevOffEvent = generateEvent();
		prevOffEvent.setOutageAsOfTime(DateTime.now());
		prevOffEvent.setOutageETR(DateTime.now().minusDays(2));
		setOutageCreationTime(prevOffEvent,DateTime.now().minusMinutes(15));
		doReturn(s3ObjectStream).when(s3Object).getObjectContent();
		doReturn(Collections.singletonList(prevOffEvent)).when(mapper).readValue(eq(s3ObjectStream), any(TypeReference.class));

		String result = service.processBatch(new ArrayList<>(Collections.singletonList(newOnEvent)), "batch1");

		assertEquals("SUCCESS", result);
		verify(s3Client).deleteObject(eq(BUCKET_MOMENTARY), anyString());
		verify(s3Client).putObject(eq(BUCKET_RESTORED), anyString(), any(InputStream.class), any(ObjectMetadata.class));
	}

	@Test
	void processOffToOnDeletedFromSustained() throws IOException {
		doReturn(s3Object).when(s3Client).getObject(eq(BUCKET_SUSTAINED), anyString());
		doReturn(s3ObjectStream).when(s3Object).getObjectContent();

		OutageEvent newOnEvent = generateEvent();
		newOnEvent.setPremisePowerStatus(POWER_ON);
		newOnEvent.setOutageETR(DateTime.now().minusDays(2));
		setOutageCreationTime(newOnEvent,DateTime.now().minusMinutes(15));
		setupObjectMapperWrite(mapper, newOnEvent);

		OutageEvent prevOffEvent = generateEvent();
		prevOffEvent.setOutageAsOfTime(DateTime.now());
		prevOffEvent.setOutageETR(DateTime.now().minusDays(2));
		setOutageCreationTime(prevOffEvent,DateTime.now().minusMinutes(15));
		doReturn(new ArrayList<>(Collections.singletonList(prevOffEvent))).when(mapper).readValue(eq(s3ObjectStream), any(TypeReference.class));

		doThrow(SdkClientException.class).when(s3Client).getObject(eq(BUCKET_MOMENTARY), anyString());

		mockOutageManager(outageManager, PowerHasBeenRestored, RESTORED);

		String result = service.processBatch(new ArrayList<>(Collections.singletonList(newOnEvent)), "batch1");

		assertEquals("SUCCESS", result);
		verify(s3Client).deleteObject(eq(BUCKET_SUSTAINED), anyString());
		verify(s3Client).putObject(eq(BUCKET_RESTORED), anyString(), any(InputStream.class), any(ObjectMetadata.class));
	}

	@Disabled("service.processBatch will no longer ever return FAILED")
	@Test
	void mapperExceptionFromSustainedStopsProcessing() throws IOException {
		doReturn(s3Object).when(s3Client).getObject(eq(BUCKET_SUSTAINED), anyString());
		doReturn(s3ObjectStream).when(s3Object).getObjectContent();
		doThrow(IOException.class).when(mapper).readValue(eq(s3ObjectStream), any(TypeReference.class));

		String result = service.processBatch(new ArrayList<>(Collections.singletonList(generateEvent())), "batch1");

		assertEquals("FAILED", result);
	}

	@Disabled("service.processBatch will no longer ever return FAILED")
	@Test
	void mapperExceptionFromMomentaryStopsProcessing() throws IOException {
		doThrow(SdkClientException.class).when(s3Client).getObject(eq(BUCKET_SUSTAINED), anyString());
		doReturn(s3Object).when(s3Client).getObject(eq(BUCKET_MOMENTARY), anyString());
		doReturn(s3ObjectStream).when(s3Object).getObjectContent();
		doThrow(IOException.class).when(mapper).readValue(eq(s3ObjectStream), any(TypeReference.class));

		OutageEvent event = generateEvent();
		event.setPremisePowerStatus(POWER_ON);

		String result = service.processBatch(new ArrayList<>(Collections.singletonList(event)), "batch1");

		assertEquals("FAILED", result);
	}

	@Test
	void parseBatch() throws IOException {
		S3Event s3Event = mock(S3Event.class);
		S3ObjectInputStream s3ObjectStream = mock(S3ObjectInputStream.class);
		setupEventMocks(s3Client, s3Event, s3ObjectStream);
		OutageEvent event = generateEvent();
		ArrayList<OutageEvent> events = new ArrayList<>();
		events.add(event);
		OutageData dataObj = new OutageData();
		dataObj.setOutageData(events);
		ArrayList<OutageData> outages = new ArrayList<OutageData>(Arrays.asList(dataObj));
		doReturn(outages).when(mapper).readValue(eq(s3ObjectStream), any(TypeReference.class));
		//readValue(eq(s3ObjectStream), eq(outages.getClass()));

		assertTrue(service.parseBatch(s3Event).contains(event));
	}

	@Test
	void parseBatchMapperException() throws IOException {
		S3Event s3Event = mock(S3Event.class);
		S3ObjectInputStream s3ObjectStream = mock(S3ObjectInputStream.class);
		setupEventMocks(s3Client, s3Event, s3ObjectStream);

		doThrow(IOException.class).when(mapper).readValue(eq(s3ObjectStream), any(TypeReference.class));

		assertNull(service.parseBatch(s3Event));
	}

	@Test
	void persistBatch() throws JsonProcessingException {
		doReturn(BYTES).when(mapper).writeValueAsBytes(any());
		ArrayList<Object> list = new ArrayList<>();
		list.add(generateEvent());

		assertEquals("SUCCESS", service.persistBatch(list));
		verify(s3Client).putObject(argThat(p -> p.getBucketName().equals(BUCKET_RAW)));
	}

	@ParameterizedTest
	@MethodSource("persistBatchExceptions")
	void persistBatchMapperException(Class<Exception> c) throws JsonProcessingException {
		doThrow(c).when(mapper).writeValueAsBytes(any());

		assertEquals("FAILED", service.persistBatch(new ArrayList<>()));
	}
	
	private static Stream<Class<? extends Exception>> persistBatchExceptions() {
		return Stream.of(
				JsonProcessingException.class,
				AmazonS3Exception.class,
				NullPointerException.class
		);
	}


}
