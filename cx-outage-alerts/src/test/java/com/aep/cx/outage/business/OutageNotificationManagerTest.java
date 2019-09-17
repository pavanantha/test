package com.aep.cx.outage.business;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.utils.alerts.common.data.AlertsNotificationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.aep.cx.outage.business.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class OutageNotificationManagerTest {

	private OutageNotificationManager manager;

	@BeforeEach
	void setup() {
		manager = new OutageNotificationManager();
	}

	@Test
	void convertToNotificationData() {
		OutageEvent event = generateEvent();

		AlertsNotificationData result = manager.convertToNotificationData(event);

		assertAll(
				() -> assertEquals(result.getPremiseNumber(), PREMISE_NUM),
				() -> assertTrue(result.getAccountNumber().contains(PREMISE_NUM)),
				() -> assertEquals(result.getOutageSimpleCause(), OUTAGE_SIMPLE_CAUSE),
				() -> assertEquals(result.getEtrType(), ETR_TYPE),
				() -> assertEquals(result.getOutageEtrTime(), OUTAGE_ETR),
				() -> assertEquals(result.getOutageCreationTime(), CREATION_TIME),
				() -> assertEquals(result.getOutageRestorationTime(), RESTORE_TIME),
				() -> assertEquals(result.getAlertName(), event.getValueAddAlertName()),
				() -> assertEquals(result.getOutageMaxCount(), CUSTOMER_MAX_COUNT),
				() -> assertEquals(result.getOutageCurrentCount(), CUSTOMER_COUNT),
				() -> assertEquals(result.getOutageNumber(), Integer.parseInt(OUTAGE_NUMBER)),
				() -> assertEquals(result.getPremisePowerStatus(), POWER_STATUS),
				() -> assertEquals(result.getOutageStatus(), OUTAGE_STATUS)
		);
	}
}
