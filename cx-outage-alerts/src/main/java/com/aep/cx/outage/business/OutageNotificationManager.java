package com.aep.cx.outage.business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.utils.alerts.common.data.AlertsNotificationData;
import com.aep.cx.utils.alerts.notification.msessages.BuildEmail;
import com.aep.cx.utils.alerts.notification.msessages.BuildSMS;

public class OutageNotificationManager {

	final Logger logger = LogManager.getLogger(OutageNotificationManager.class);

	public BuildEmail buildEmailNotification(OutageEvent outageEvent, String batchKey, String bucket,
			HashMap<String, ArrayList<CustomerPreferences>> customerPreference) {

		AlertsNotificationData notificationData = convertToNotificationData(outageEvent);
		try {
			BuildEmail buildEmail = new BuildEmail(
					new ArrayList<AlertsNotificationData>(Arrays.asList(notificationData)), customerPreference);
			return buildEmail;

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(String.format("Error getting object %s from bucket %s. Make sure they exist and"
					+ " your bucket is in the same region as this function.", batchKey, bucket));
			return null;
		}
	}

	public BuildSMS buildSMSNotification(OutageEvent outageEvent, String batchKey, String bucket,
			HashMap<String, ArrayList<CustomerPreferences>> customerPreference) {

		AlertsNotificationData notificationData = convertToNotificationData(outageEvent);

		try {
			BuildSMS buildSMS = new BuildSMS(new ArrayList<AlertsNotificationData>(Arrays.asList(notificationData)),
					customerPreference);
			return buildSMS;

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(String.format("Error getting object %s from bucket %s. Make sure they exist and"
					+ " your bucket is in the same region as this function.", batchKey, bucket));
			return null;
		}
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
