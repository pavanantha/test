package com.aep.cx.outage.business;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.ArrayList;
import java.util.Calendar;

import static com.aep.cx.outage.alerts.enums.ValueAddAlertType.ETR;
import static com.aep.cx.outage.alerts.enums.ValueAddProcessingReasonType.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OutageManagerTest {

	private static final OutageEvent PREDICTED_NO_ETR;

	static {
		PREDICTED_NO_ETR = generateEvent();
		PREDICTED_NO_ETR.setOutageETR(DateTime.now());
		PREDICTED_NO_ETR.setOutageAsOfTime(DateTime.now());
		PREDICTED_NO_ETR.setOutageETRType("u");
		PREDICTED_NO_ETR.setOutageCreationTime(DateTime.now().minusMinutes(15));
	}

	private OutageManager manager;

	private static OutageEvent generateEvent() {
		OutageEvent event = new OutageEvent();
		event.setPremiseNumber("953456669");
		event.setPremisePowerStatus("OFF");
		event.setOutageCreationTime(DateTime.now());
		event.setOutageETR(DateTime.now().plusDays(1));
		event.setOutageETRType("g");
		event.setOutageType("predicted");
		event.setMomentaryWaitThresholdInMinutes(10);
		// New sets are required for changes to OutageEvent.getIsInMomentaryWait()
		event.setOutageAsOfTime(DateTime.now());
		event.setValueAddCurrentDateTime(DateTime.now());
		String currentTZ = Calendar.getInstance().getTimeZone().getDisplayName();
		event.setValueAddAlertName(currentTZ);
		event.setBatchKey("TEST");
		return event;
	}

	@BeforeEach
	void setUp() {
		manager = new OutageManager();
	}

	@Disabled("Needs to be fixed to fit new logic")
	@Test
	void testMomentaryWait() {
		OutageEvent result = manager.ProcessRules(generateEvent(), null);

		assertEquals(CurrentStatusMomentaryWait, result.getValueAddProcessingReasonType());
	}

	@Test
	void testAlertTypePredicted() {
		OutageEvent alertTypePredicted = generateEvent();
		alertTypePredicted.setOutageCreationTime(DateTime.now().minusMinutes(15));

		OutageEvent result = manager.ProcessRules(alertTypePredicted, null);

		assertEquals(Predicted, result.getValueAddProcessingReasonType());
	}

	@Test
	void testAlertTypePredictedNOETR() {
		OutageEvent result = manager.ProcessRules(PREDICTED_NO_ETR, null);

		assertEquals(PredictedOutageFirstNotificationWithNoETR, result.getValueAddProcessingReasonType());
	}

	@Test
	void testAlertTypeETR() {
		OutageEvent alertTypeETR = generateEvent();
		alertTypeETR.setOutageAsOfTime(DateTime.now().plusMinutes(20));
		alertTypeETR.setOutageETR(DateTime.now().minusDays(2));
		alertTypeETR.setOutageCreationTime(DateTime.now().minusMinutes(15));

		OutageEvent result = manager.ProcessRules(alertTypeETR, PREDICTED_NO_ETR);

		assertEquals(None, result.getValueAddProcessingReasonType());
	}

	@Test
	void testAlertTypeRestored() {
		OutageEvent alertTypeRestored = generateEvent();
		alertTypeRestored.setPremisePowerStatus("ON");
		alertTypeRestored.setOutageRestorationTime(DateTime.now().plusDays(1));
		alertTypeRestored.setOutageCreationTime(DateTime.now().minusMinutes(15));
		alertTypeRestored.setOutageType("restored");
		alertTypeRestored.setOutageETRType("g");
		alertTypeRestored.setMomentaryWaitThresholdInMinutes(1);
		alertTypeRestored.setOutageETR(DateTime.now().minusDays(2));

		OutageEvent result = manager.ProcessRules(alertTypeRestored, PREDICTED_NO_ETR);

		assertEquals(PowerHasBeenRestored, result.getValueAddProcessingReasonType());
	}

	@Test
	void testAlertTypeRestoredCancelled() {
		OutageEvent AlertTypeRestoredCancelled = generateEvent();
		AlertTypeRestoredCancelled.setPremisePowerStatus("ON");
		AlertTypeRestoredCancelled.setOutageRestorationTime(DateTime.now().minusDays(3));
		AlertTypeRestoredCancelled.setOutageCreationTime(DateTime.now().minusMinutes(15));
		AlertTypeRestoredCancelled.setOutageType("restored");
		AlertTypeRestoredCancelled.setOutageETRType("g");
		AlertTypeRestoredCancelled.setMomentaryWaitThresholdInMinutes(1);
		AlertTypeRestoredCancelled.setOutageETR(DateTime.now().minusDays(2));

		OutageEvent result = manager.ProcessRules(AlertTypeRestoredCancelled, PREDICTED_NO_ETR);

		assertEquals(CancelledRestore, result.getValueAddProcessingReasonType());
	}

	@Disabled("Need to verify what ought to happen here.")
	@Test
	void testNewerEventExists() {
		OutageEvent exceptionNoOutageETRValue = generateEvent();
		exceptionNoOutageETRValue.setOutageETR(DateTime.now().plusMinutes(20));
		exceptionNoOutageETRValue.setOutageAsOfTime(DateTime.now().minusMinutes(15));
		exceptionNoOutageETRValue.setOutageETRType("g");
		exceptionNoOutageETRValue.setOutageCreationTime(DateTime.now().minusMinutes(15));

		OutageEvent prevEvent = generateEvent();
		prevEvent.setOutageETR(DateTime.now());
		prevEvent.setOutageAsOfTime(DateTime.now().minusHours(1));

		OutageEvent result = manager.ProcessRules(exceptionNoOutageETRValue, prevEvent);

		assertEquals(NewerOutageEventExist, result.getValueAddProcessingReasonType());
		assertEquals(ETR, result.getValueAddAlertType());
	}

	@Test
	void processRulesBatchNullPrevList() {
		final ArrayList<OutageEvent> events = new ArrayList<>();
		events.add(generateEvent());
		events.add(generateEvent());
		final ArrayList<OutageEvent> prevEvents = new ArrayList<>();
		prevEvents.add(generateEvent());
		prevEvents.add(generateEvent());
		prevEvents.forEach(event -> event.setPremiseNumber(null));

		ArrayList<OutageEvent> results = manager.ProcessRulesBatch(events, prevEvents);

		assertAll(
				() -> assertEquals(results.get(0).getValueAddProcessingReasonType(), Predicted),
				() -> assertEquals(results.get(1).getValueAddProcessingReasonType(), Predicted)
		);
	}

	@Test
	void processRulesBatch() {
		final ArrayList<OutageEvent> events = new ArrayList<>();
		events.add(generateEvent());
		events.add(generateEvent());
		final ArrayList<OutageEvent> prevEvents = new ArrayList<>();
		OutageEvent prevEvent = generateEvent();
		prevEvent.setOutageETR(DateTime.now());
		prevEvent.setOutageAsOfTime(DateTime.now().minusHours(1));
		prevEvents.add(prevEvent);
		prevEvent = generateEvent();
		prevEvent.setOutageETR(DateTime.now());
		prevEvent.setOutageAsOfTime(DateTime.now().minusHours(1));		
		prevEvents.add(prevEvent);

		ArrayList<OutageEvent> results = manager.ProcessRulesBatch(events, prevEvents);

		assertAll(
				() -> assertEquals(results.get(0).getValueAddProcessingReasonType(), OutageFirstNotificationWithETR),
				() -> assertEquals(results.get(1).getValueAddProcessingReasonType(), OutageFirstNotificationWithETR)
		);
	}
}
