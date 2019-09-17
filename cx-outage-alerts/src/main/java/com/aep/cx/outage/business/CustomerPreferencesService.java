package com.aep.cx.outage.business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.preferences.dao.CustomerPreferencesDao;
import com.aep.cx.utils.alerts.common.data.AlertsNotificationData;

public class CustomerPreferencesService {

	final Logger logger = LogManager.getLogger(CustomerPreferencesService.class);

	private final CustomerPreferencesDao preferences;

	public CustomerPreferencesService() {
		this(new CustomerPreferencesDao());
	}

	public CustomerPreferencesService(CustomerPreferencesDao customerPreferencesDao) {
		this.preferences = customerPreferencesDao;
	}

    public HashMap<String, ArrayList<CustomerPreferences>> getCustomerPreferences(OutageEvent outageEvent) {

		logger.debug("Convert Outage Event into AlertsNotificationData");
		AlertsNotificationData notificationData = convertToNotificationData(outageEvent);

		logger.debug("Serialize ArrayList<CustomerPreferences>");
		HashMap<String, ArrayList<CustomerPreferences>> custPref = preferences
				.getPreferencesByPremise(new ArrayList<AlertsNotificationData>(Arrays.asList(notificationData)));

		logger.debug("Return Customer Preferences");
		return custPref;
	}

	public AlertsNotificationData convertToNotificationData(OutageEvent outageEvent) {

		AlertsNotificationData notificationData = new AlertsNotificationData();
		notificationData.setPremiseNumber(outageEvent.getPremiseNumber());
		notificationData.setAccountNumber(outageEvent.getPremiseNumber() + "0");
		notificationData.setOutageSimpleCause(outageEvent.getOutageSimpleCause());
		notificationData.setEtrType(outageEvent.getOutageETRType());
		notificationData.setOutageEtrTime(outageEvent.getOutageETR());
		notificationData.setOutageCreationTime(outageEvent.getOutageCreationTime());
		if (null == outageEvent.getOutageRestorationTime()) {
			notificationData.setOutageRestorationTime(null);
		} else {
			notificationData.setOutageRestorationTime(outageEvent.getOutageRestorationTime());
		}
		notificationData.setAlertName(outageEvent.getValueAddAlertName());
		notificationData.setOutageMaxCount(outageEvent.getOutageCustomerMAXCount());
		notificationData.setOutageCurrentCount(outageEvent.getOutageCustomerCount());
		notificationData.setOutageNumber(Integer.parseInt(outageEvent.getOutageNumber()));
		notificationData.setPremisePowerStatus(outageEvent.getPremisePowerStatus());
		notificationData.setOutageStatus(outageEvent.getOutageStatus());
		notificationData.setBatchKey(outageEvent.getBatchKey());
		return notificationData;
	}
}