package com.aep.cx.outage.business;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.aep.cx.outage.alerts.dao.NotifiedOutageAlertsState;
import com.aep.cx.outage.alerts.dao.NotifiedOutageAlertsStateDao;
import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.outage.alerts.enums.ValueAddAlertType;

public class CustomerOutageStatus {

	final Logger logger = LogManager.getLogger(CustomerOutageStatus.class);

	public OutageEvent setCustomeroutageStatus(OutageEvent outageEvent) {

		NotifiedOutageAlertsStateDao dao = new NotifiedOutageAlertsStateDao();
		NotifiedOutageAlertsState notifiedState = dao.getNotifiedOutageAlertState(outageEvent.getPremiseNumber());
		DateTimeFormatter dtFormatter = DateTimeFormat.forPattern("yyyyMMddHHmm");

		if (null != notifiedState) {
			DateTime previousAsOfTime = DateTime.parse(notifiedState.getAsOfTime(),
					dtFormatter.withZone(DateTimeZone.forID(outageEvent.getValueAddOperatingCompanyTimeZone())));
			DateTime previousETR = DateTime.parse(notifiedState.getNotifiedETR(),
					dtFormatter.withZone(DateTimeZone.forID(outageEvent.getValueAddOperatingCompanyTimeZone())));

			if (outageEvent.getOutageAsOfTime().getMillis() > previousAsOfTime.getMillis()) {

				if (!notifiedState.getAlertType().contentEquals(ValueAddAlertType.RESTORED.toString())
						&& !notifiedState.getAlertType().contentEquals(ValueAddAlertType.CANCELLED.toString())) {
					if (outageEvent.getPremisePowerStatus().equalsIgnoreCase("ON")) {
						if (outageEvent.getOutageRestorationTime().getMillis() > outageEvent.getOutageCreationTime()
								.getMillis()) {
							outageEvent.setValueAddAlertType(ValueAddAlertType.RESTORED);
						} else {
							outageEvent.setValueAddAlertType(ValueAddAlertType.CANCELLED);
						}
					} else {
						if (this.validateChangeETR(outageEvent.getOutageETR(), previousETR)) {
							outageEvent.setValueAddAlertType(ValueAddAlertType.ETR);
						} else {
							outageEvent.setValueAddAlertType(ValueAddAlertType.NONE);
						}
					}
				} else {
					setAlertTypeAtBeginingOfItsLifeCycle(outageEvent);
				}
			} else {
				logger.info("Could be Stale Event :- " + outageEvent.getPremiseNumber() + " from batch - "
						+ outageEvent.getBatchKey());
				outageEvent.setValueAddAlertType(ValueAddAlertType.NONE);//can set to Stale
			}
		} else {
			setAlertTypeAtBeginingOfItsLifeCycle(outageEvent);
		}

		/*
		 * updateNotifiedOutageAlertState when 1). it is Momentary and ON Event 2). or
		 * Alert Type is not NONE and Not Momentary
		 */

		String list = "";
		if (null == notifiedState || null == notifiedState.getIncomingFiles()
				|| notifiedState.getAlertType().equals(ValueAddAlertType.RESTORED.toString())) {
			list = outageEvent.getBatchKey();
		} else {
			list = notifiedState.getIncomingFiles() + "," + outageEvent.getBatchKey();
		}

		if ((outageEvent.getValueAddAlertType().equals(ValueAddAlertType.MOMENTARY)
				&& outageEvent.getIsPremisePowerOn())
				|| (!outageEvent.getValueAddAlertType().equals(ValueAddAlertType.NONE)
						&& !outageEvent.getValueAddAlertType().equals(ValueAddAlertType.MOMENTARY))) {
			notifiedState = new NotifiedOutageAlertsState();
			notifiedState.setAlertType(outageEvent.getValueAddAlertType().toString());
			notifiedState.setPremiseNumber(outageEvent.getPremiseNumber());
			notifiedState.setAsOfTime(outageEvent.getOutageAsOfTime().toString(dtFormatter));
			notifiedState.setNotifiedETR(outageEvent.getOutageETR().toString(dtFormatter));
			notifiedState.setIncomingFiles(list);
			dao.updateNotifiedOutageAlertState(notifiedState);
		}
		return outageEvent;
	}

	/**
	 * @param outageEvent
	 */
	private void setAlertTypeAtBeginingOfItsLifeCycle(OutageEvent outageEvent) {
		if (!outageEvent.getIsInMomentaryWait()) {
			if (outageEvent.getPremisePowerStatus().equalsIgnoreCase("OFF")) {
				if (this.validateETR(outageEvent.getOutageETR())) {
					outageEvent.setValueAddAlertType(ValueAddAlertType.PREDICTED);
				} else {
					outageEvent.setValueAddAlertType(ValueAddAlertType.PREDICTEDNOETR);
				}
			} else {
				outageEvent.setValueAddAlertType(ValueAddAlertType.NONE); //Momentary ON
			}
		} else {
			outageEvent.setValueAddAlertType(ValueAddAlertType.MOMENTARY);
		}
	}

	public Boolean validateETR(DateTime etr) {
		int minutesInTheFuture = 15;
		if (etr.getMillis() > DateTime.now().plusMinutes(minutesInTheFuture).getMillis()) {
			return true;
		} else {
			return false;
		}
	}

	public Boolean validateChangeETR(DateTime etr, DateTime notifiedETR) {

		int minutesOfDifference = 15;
		if (null != notifiedETR) {

			if (Math.abs(etr.getMillis() - notifiedETR.getMillis()) > (minutesOfDifference * 60 * 1000)
					&& validateETR(etr)) {
				return true;
			} else {
				return false;
			}
		} else {
			return validateETR(etr);
		}
	}
}
