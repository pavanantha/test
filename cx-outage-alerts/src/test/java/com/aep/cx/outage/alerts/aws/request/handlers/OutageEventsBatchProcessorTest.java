package com.aep.cx.outage.alerts.aws.request.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.utils.opco.OperatingCompanyManager;
import com.aep.cx.utils.opco.OperatingCompanyV2;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class OutageEventsBatchProcessorTest {
	private final byte[] BYTES = new byte[] { (byte) 1 };

	@Mock
	private ObjectMapper mapper;

	@InjectMocks
	private OutageEventsBatchProcessor processor;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
	}

//	@Disable("tuning off for no")
	@Test
	public void testOutageEventsBatchProcessor() throws IOException {

		Context ctx = createContext();
		ArrayList<OutageEvent> events = new ArrayList<>();
		ArrayList<Object> list = new ArrayList<>();
		
		OutageEvent event = generateEvent();
		list.add(event);
		
		events.add(event);
		
		doReturn(BYTES).when(mapper).writeValueAsBytes(any());
		doReturn(null).when(mapper).readValue(any(InputStream.class), any(TypeReference.class));

		assertEquals("Success", processor.handleRequest(list, ctx));
	}

	private static OutageEvent generateEvent() {
		OutageEvent event = new OutageEvent();
		event.setPremiseNumber("953456666");
		event.setPremisePowerStatus("OFF");
		event.setBatchKey("Test");

		// Set OperatingCompany for later timezone fixing
		OperatingCompanyV2 outageOpco = operatingCompanyMap.get(event.getPremiseNumber().substring(0, 2));
		event.setValueAddOperatingCompanyTimeZone(outageOpco.getTimeZone());
		setOutageCreationTime(event, DateTime.now());

		return event;
	}

	private static OperatingCompanyManager operatingCompanyManager = new OperatingCompanyManager();
	private static Map<String, OperatingCompanyV2> operatingCompanyMap = operatingCompanyManager
			.BuildOperatingCompanyMap();

	private static void setOutageCreationTime(OutageEvent event, DateTime dt) {
		DateTime localDT = dt.withZone(DateTimeZone.forID(event.getValueAddOperatingCompanyTimeZone()));
		event.setOutageCreationTime(event.convertToLocalTime(localDT));
	}

	private Context createContext() {

		TestContext ctx = new TestContext();

		// Context ctx = createContext();
		ctx.setFunctionName("OutageEventsBatchProcessor");

		return ctx;
	}
}
