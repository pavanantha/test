package com.aep.cx.outage.business;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.preferences.dao.CustomerContacts;
import com.aep.cx.preferences.dao.CustomerPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.aep.cx.outage.business.TestUtils.generateCustomerInfo;
import static com.aep.cx.outage.business.TestUtils.generateEvent;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OutageNotificationManagerIT {

	private static final String BATCH_KEY = "key";
	private static final String BUCKET = "BUCKET";
	private HashMap<String, ArrayList<CustomerPreferences>> preferences;
	private OutageEvent event;

	private OutageNotificationManager manager;

	@BeforeEach
	void setup() {
		manager = new OutageNotificationManager();
		event = generateEvent();
		preferences = new HashMap<>();
		CustomerPreferences prefs = new CustomerPreferences();
		List<CustomerContacts> contacts = new ArrayList<>();
		CustomerContacts contact = new CustomerContacts();
		contact.setWebId("web-id");
		contact.setEndPoint("customer@end.point");
		contacts.add(contact);
		prefs.setCustomerContacts(contacts);
		prefs.setCustomerInfo(generateCustomerInfo());
		ArrayList<CustomerPreferences> prefsList = new ArrayList<>();
		prefsList.add(prefs);
		preferences.put(event.getPremiseNumber(), prefsList);
	}

	@Test
	void buildEmail() {
		assertNotNull(manager.buildEmailNotification(event, BATCH_KEY, BUCKET, preferences));
	}

	@Test
	void buildEmailException() {
		assertNull(manager.buildEmailNotification(event, BATCH_KEY, BUCKET, null));
	}

	@Test
	void buildSMS() {
		assertNotNull(manager.buildSMSNotification(event, BATCH_KEY, BUCKET, preferences));
	}

	@Test
	void buildSMSException() {
		assertNull(manager.buildSMSNotification(event, BATCH_KEY, BUCKET, null));
	}
}