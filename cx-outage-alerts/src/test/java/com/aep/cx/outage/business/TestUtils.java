package com.aep.cx.outage.business;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.preferences.dao.CustomerInfo;
import org.joda.time.DateTime;

import static com.aep.cx.outage.alerts.enums.ValueAddAlertType.PREDICTED;

class TestUtils {

	static final String PREMISE_NUM = "10345";
	static final String OUTAGE_SIMPLE_CAUSE = "simple-cause";
	static final String ETR_TYPE = "etr-type";
	static final DateTime OUTAGE_ETR = DateTime.parse("2019-06-22T03:00:00Z");
	static final DateTime CREATION_TIME = DateTime.parse("2019-06-22T12:00:00Z");
	static final DateTime RESTORE_TIME = DateTime.parse("2019-06-22T03:30Z");
	static final int CUSTOMER_MAX_COUNT = 100;
	static final int CUSTOMER_COUNT = 55;
	static final String OUTAGE_NUMBER = "4";
	static final String POWER_STATUS = "power-off";
	static final String OUTAGE_STATUS = "outage-status";

	static OutageEvent generateEvent() {
		OutageEvent event = new OutageEvent();
		event.setPremiseNumber(PREMISE_NUM);
		event.setOutageSimpleCause(OUTAGE_SIMPLE_CAUSE);
		event.setOutageETRType(ETR_TYPE);
		event.setOutageETR(OUTAGE_ETR);
		event.setOutageCreationTime(CREATION_TIME);
		event.setOutageRestorationTime(RESTORE_TIME);
		event.setValueAddAlertType(PREDICTED);
		event.setOutageCustomerMAXCount(CUSTOMER_MAX_COUNT);
		event.setOutageCustomerCount(CUSTOMER_COUNT);
		event.setOutageNumber(OUTAGE_NUMBER);
		event.setPremisePowerStatus(POWER_STATUS);
		event.setOutageStatus(OUTAGE_STATUS);
		event.setBatchKey("TEST");
		return event;
	}

	static CustomerInfo generateCustomerInfo() {
		CustomerInfo info = new CustomerInfo();
		info.setPremiseNumber(PREMISE_NUM);
		info.setAccountNumber(PREMISE_NUM + "0");
		info.setCity("city");
		info.setName("info");
		info.setState("state");
		info.setStreetAddress("123 street lane");
		info.setZipCode("12345");
		return info;
	}

}
