package com.aep.cx.outage.business;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.preferences.dao.CustomerPreferencesDao;
import com.aep.cx.utils.alerts.common.data.AlertsNotificationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;

import static com.aep.cx.outage.business.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

class CustomerPreferencesServiceTest {

	@Mock
	private CustomerPreferencesDao customerPreferencesDao;
	@InjectMocks
	private CustomerPreferencesService service;

	@BeforeEach
	void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void getCustomerPreferences() {
		HashMap<String, ArrayList<CustomerPreferences>> preferences = new HashMap<>();
		doReturn(preferences).when(customerPreferencesDao).getPreferencesByPremise(argThat(l -> l.get(0).getPremiseNumber().equals(PREMISE_NUM)));

		assertEquals(preferences, service.getCustomerPreferences(generateEvent()));
	}

	@Test
	void convertToNotificationData() {
		OutageEvent event = generateEvent();

		AlertsNotificationData result = service.convertToNotificationData(event);

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