package com.aep.cx.outage.business;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.outage.alerts.enums.ValueAddAlertType;
import com.aep.cx.outage.alerts.enums.ValueAddOutageEstimatedTimeToRestorationType;
import com.aep.cx.outage.alerts.enums.ValueAddOutageType;
import com.aep.cx.outage.alerts.enums.ValueAddProcessingReasonType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OutageManager {

	static final Logger logger = LogManager.getLogger(OutageManager.class);

	public OutageEvent ProcessRules(OutageEvent outageEvent, OutageEvent previousOutageEvent) {

		long diffMillis;

		if (outageEvent != null) {
			logger.debug("Deserialize Outage Event: " + outageEvent.toString());
		}

		if (previousOutageEvent != null) {
			logger.debug("Deserialize Previous Outage Event: " + previousOutageEvent.toString());
		}

		/*
		 * conditions below are based on a customer not being previously notified of an
		 * outage
		 */
		if (previousOutageEvent == null) {
			// Determine Momentary Status
			/*
			 * When an OFF Outage Event first comes in, it is placed in a held state, This
			 * is to allow time to determine, if a premise sustains a Power Status of OFF
			 * for at least 10 minutes. If so, then the process of notification begins. If a
			 * Power Status of ON comes In within that 10 minute window, the outage event is
			 * determined momentary and no notification is sent to the customer
			 */
			if (!outageEvent.getIsPremisePowerOn()) {
				if (outageEvent.getIsInMomentaryWait()) {
					outageEvent
							.setValueAddProcessingReasonType(ValueAddProcessingReasonType.CurrentStatusMomentaryWait);
					//outageEvent.setValueAddAlertType(ValueAddAlertType.PREDICTED);
					if (null == outageEvent.getOutageETR()
							|| outageEvent.getOutageETR().getMillis() <= DateTime.now().plusMinutes(15).getMillis()) {
						outageEvent.setValueAddProcessingReasonType(
								ValueAddProcessingReasonType.PredictedOutageFirstNotificationWithNoETR);
						outageEvent.setValueAddAlertType(ValueAddAlertType.PREDICTEDNOETR);
					}
					else {						
						outageEvent.setValueAddProcessingReasonType(
								ValueAddProcessingReasonType.Predicted);
						outageEvent.setValueAddAlertType(ValueAddAlertType.PREDICTED);
					}
					return outageEvent;
				}
			}

			// Alert Type: Predicted
			/*
			 * This is the first notification a customer would receive once it has been
			 * determined a sustain outage has occurred, the customer will be notified of
			 * the outage and a predicted estimated time of restoration of when the power
			 * should be restored is provided. all estimations and data has been given by
			 * PowerOn Event Feeds.
			 */
			if (!outageEvent.getIsPremisePowerOn()
					// && outageEvent.getValueAddOutageEstimatedTimeToRestorationType() ==
					// ValueAddOutageEstimatedTimeToRestorationType.Global
					// && (outageEvent.getValueAddOutageType() == ValueAddOutageType.Predicted ||
					// outageEvent.getValueAddOutageType() == ValueAddOutageType.Confirmed)
					&& (null != outageEvent.getOutageETR())
					&& outageEvent.getOutageETR().getMillis() > DateTime.now().plusMinutes(15).getMillis()) {
				outageEvent.setValueAddProcessingReasonType(ValueAddProcessingReasonType.Predicted);
				outageEvent.setValueAddAlertType(ValueAddAlertType.PREDICTED);

				return outageEvent;
			}

			// Alert Type: Predicted No-ETR
			/*
			 * This is the first notification a customer would receive once it has been
			 * determined a sustain outage has occurred, the customer will be notified of
			 * the outage and the estimated time of restoration is not provided. This is
			 * caused by the both the estimated time of restoration being in less than 15
			 * minutes of proccing the event and the etr type was unavaliable.
			 */
			if (!outageEvent.getIsPremisePowerOn()
					// && outageEvent.getValueAddOutageEstimatedTimeToRestorationType() ==
					// ValueAddOutageEstimatedTimeToRestorationType.Unavailable
					// && (outageEvent.getValueAddOutageType() == ValueAddOutageType.Predicted ||
					// outageEvent.getValueAddOutageType() == ValueAddOutageType.Confirmed)
					&& (null == outageEvent.getOutageETR()
							|| outageEvent.getOutageETR().getMillis() <= DateTime.now().plusMinutes(15).getMillis())) {
				outageEvent.setValueAddProcessingReasonType(
						ValueAddProcessingReasonType.PredictedOutageFirstNotificationWithNoETR);
				outageEvent.setValueAddAlertType(ValueAddAlertType.PREDICTEDNOETR);
				return outageEvent;
			}
		}
		/*
		 * All conditions below, are based on a previous sustained outage notifcation
		 * being sent
		 */
		if (previousOutageEvent != null) {

			// Get the absolute value of the difference between current outage ETR and
			// previous outage ETR

			if (null != outageEvent.getOutageETR()) {
				if (null != previousOutageEvent.getOutageETR()) {
					diffMillis = Math.abs(
							outageEvent.getOutageETR().getMillis() - previousOutageEvent.getOutageETR().getMillis());
				} else {
					diffMillis = Math.abs(outageEvent.getOutageETR().getMillis());
				}
			} else {
				DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmm");
				DateTime dt = formatter.parseDateTime("200101010101");
				outageEvent.setOutageETR(dt);
				diffMillis = 0;
			}

			// Alert Type: ETR
			/*
			 * This notification is sent, when PowerOnData has sent an updated outage event,
			 * indicating that the orginial estimated time of restoration has either
			 * increased by 15 minutes or more or has decreased by 15 minutes or less. 15
			 * minutes, is a business rule, that AEP has determined is the appropriate
			 * amount of change in time to occur, before notifing a customer.
			 */
			if (!outageEvent.getIsPremisePowerOn()
					// && outageEvent.getValueAddOutageEstimatedTimeToRestorationType() ==
					// ValueAddOutageEstimatedTimeToRestorationType.Global
					// && (outageEvent.getValueAddOutageType() == ValueAddOutageType.Predicted ||
					// outageEvent.getValueAddOutageType() == ValueAddOutageType.Confirmed)
					&& outageEvent.getOutageETR().getMillis() > DateTime.now().plusMinutes(15).getMillis()
					&& outageEvent.getOutageAsOfTime().getMillis() > previousOutageEvent.getOutageAsOfTime().getMillis()
					&& TimeUnit.MILLISECONDS.toMinutes(diffMillis) > 15) {
				outageEvent
						.setValueAddProcessingReasonType(ValueAddProcessingReasonType.OutageFirstNotificationWithETR);
				outageEvent.setValueAddAlertType(ValueAddAlertType.ETR);
				return outageEvent;
			}

			// Alert Type: Restored
			/*
			 * This notification is sent, when PowerOnData has sent an updated outage event,
			 * indicating that the power has been Restored.
			 */
			if (outageEvent.getIsPremisePowerOn()
					// && outageEvent.getValueAddOutageEstimatedTimeToRestorationType() ==
					// ValueAddOutageEstimatedTimeToRestorationType.Global
					&& outageEvent.getOutageRestorationTime() != null && outageEvent.getOutageRestorationTime()
							.getMillis() > outageEvent.getOutageCreationTime().getMillis()) {
				// && outageEvent.getValueAddOutageType() == ValueAddOutageType.Restored)

				outageEvent.setValueAddProcessingReasonType(ValueAddProcessingReasonType.PowerHasBeenRestored);
				outageEvent.setValueAddAlertType(ValueAddAlertType.RESTORED);
				return outageEvent;
			}

			// Alert Type: Cancelled Restored
			/*
			 * This notification is sent, when PowerOnData has sent an updated outage event,
			 * indicating that the power has been Restored, yet the Restoration happened
			 * before the event was created.
			 */
			if (outageEvent.getIsPremisePowerOn()
					// && outageEvent.getValueAddOutageEstimatedTimeToRestorationType() ==
					// ValueAddOutageEstimatedTimeToRestorationType.Global
					&& (outageEvent.getOutageRestorationTime() != null && outageEvent.getOutageRestorationTime()
							.getMillis() < outageEvent.getOutageCreationTime().getMillis()
							|| null == outageEvent.getOutageRestorationTime())) {
				// && outageEvent.getValueAddOutageType() == ValueAddOutageType.Restored)

				outageEvent.setValueAddProcessingReasonType(ValueAddProcessingReasonType.CancelledRestore);
				outageEvent.setValueAddAlertType(ValueAddAlertType.CANCELLED);
				return outageEvent;
			}

			// Exceptions are assuming a previous OFF event has occurred

			/*
			 * This expection happens, if we receive a new outage event, but the event is
			 * older than the event, that has processed.
			 */
			/*
			 * if (previousOutageEvent.getOutageAsOfTime().getMillis() >
			 * outageEvent.getOutageAsOfTime().getMillis()) {
			 * outageEvent.setValueAddProcessingReasonType(
			 * ValueAddProcessingReasonType.PreviousOutageEventAsOfDateIsGreater); return
			 * outageEvent; }
			 * 
			 * // Validate if this a newer or older OutageEvent what alert type is sent? if
			 * (!outageEvent.getIsPremisePowerOn() &&
			 * outageEvent.getOutageAsOfTime().getMillis() > previousOutageEvent
			 * .getOutageAsOfTime().getMillis()) {
			 * outageEvent.setValueAddProcessingReasonType(ValueAddProcessingReasonType.
			 * NewerOutageEventExist);
			 * 
			 * // TODO: Stop Processing return outageEvent; }
			 */
		}

		// Final Else
		/*
		 * If the outage event does not meet any conditions stated above then reason
		 * type of none is given, meaning, there action taken on the event
		 */
		outageEvent.setValueAddProcessingReasonType(ValueAddProcessingReasonType.None);
		return outageEvent;
	}

	public ArrayList<OutageEvent> ProcessRulesBatch(ArrayList<OutageEvent> outageEventList,
			ArrayList<OutageEvent> previousOutageEventList) {
		ArrayList<OutageEvent> list = new ArrayList<OutageEvent>();

		for (int i = 0; i < outageEventList.size(); i++) {
			if (previousOutageEventList.get(i).getPremiseNumber() == null) {
				list.add(ProcessRules(outageEventList.get(i), null));
			} else {
				list.add(ProcessRules(outageEventList.get(i), previousOutageEventList.get(i)));
			}
		}
		return list;
	}
}
