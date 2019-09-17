package com.aep.cx.outage.alerts.domains;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.outage.alerts.enums.ValueAddProcessingReasonType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutageStatisticsMonitor {

    int numberOfEventsInBatch = 0;
    int numberOfEventToProcess = 0;
    int droppedEventCount = 0;

    int numberOfMomentaryEventsDeleted = 0;
    int numberOfSustainedEventsDeleted = 0;
    int numberOfSustainedEventsToProcess = 0;
    int numberOfRestoredEventToProcess = 0;
    int numberOfMomentaryEventToProcess = 0;

    // Reasons
    int numberProcessingReasonNone = 0;
    int numberProcessingReasonPreviousOutageEventAsOfDateIsGreater = 0;
    int numberProcessingReasonNewerOutageEventExist = 0;
    int numberProcessingReasonPowerHasBeenRestored = 0;
    int numberProcessingReasonPreviousOutageProcessedNotificationRule = 0;
    int numberProcessingReasonOutageHasExpired = 0;
    int numberProcessingReasonCancelledRestore = 0;
    int numberProcessingReasonHeld = 0;
    int numberProcessingReasonOutageFirstNotificationWithETR = 0;
    int numberProcessingReasonPredictedOutageFirstNotificationWithNoETR = 0;
    int numberProcessingReasonCurrentStatusMomentaryWait = 0;
    int numberProcessingReasonExceptionAlertNoETRValueForOffEvent = 0;
    int numberProcessingReasonPowerRestoredBeforeOffNotificationSent = 0;
    int numberProcessingReasonPredicted = 0;
    int numberOfStaleEventsInBatch = 0;

    final Logger logger = LogManager.getLogger(OutageStatisticsMonitor.class);

    public void incrNumberOfMomentaryEventsDeleted() {
        numberOfMomentaryEventsDeleted++;
    }

    public void incrNumberOfSustainedEventsDeleted() {
        numberOfSustainedEventsDeleted++;
    }

    public void incrNumberOfSustainedEventsToProcess() {
        numberOfSustainedEventsToProcess++;
    }

    public void incrNumberOfRestoredEventToProcess() {
        numberOfRestoredEventToProcess++;
    }

    public void incrNumberOfMomentaryEventToProcess() {
        numberOfMomentaryEventToProcess++;
    }

    public void incrDroppedEventCount() {
        droppedEventCount++;
    }

    public void incrNumberOfEventToProcess() {
        numberOfEventToProcess++;
    }

    public void incrNumberOfEventsInBatch() {
        numberOfEventsInBatch++;
    }

    public void incrNumberProcessingReasonNone() {
        numberProcessingReasonNone++;
    }

    public void incrNumberProcessingReasonPreviousOutageEventAsOfDateIsGreater() {
        numberProcessingReasonPreviousOutageEventAsOfDateIsGreater++;
    }

    public void incrNumberProcessingReasonNewerOutageEventExist() {
        numberProcessingReasonNewerOutageEventExist++;
    }

    public void incrNumberProcessingReasonPowerHasBeenRestored() {
        numberProcessingReasonPowerHasBeenRestored++;
    }

    public void incrNumberProcessingReasonPreviousOutageProcessedNotificationRule() {
        numberProcessingReasonPreviousOutageProcessedNotificationRule++;
    }

    public void incrNumberProcessingReasonOutageHasExpired() {
        numberProcessingReasonOutageHasExpired++;
    }

    public void incrNumberProcessingReasonCancelledRestore() {
        numberProcessingReasonCancelledRestore++;
    }

    public void incrNumberProcessingReasonHeld() {
        numberProcessingReasonHeld++;
    }

    public void incrNumberProcessingReasonOutageFirstNotificationWithETR() {
        numberProcessingReasonOutageFirstNotificationWithETR++;
    }

    public void incrNumberProcessingReasonPredictedOutageFirstNotificationWithNoETR() {
        numberProcessingReasonPredictedOutageFirstNotificationWithNoETR++;
    }

    public void incrNumberProcessingReasonCurrentStatusMomentaryWait() {
        numberProcessingReasonCurrentStatusMomentaryWait++;
    }

    public void incrNumberProcessingReasonExceptionAlertNoETRValueForOffEvent() {
        numberProcessingReasonExceptionAlertNoETRValueForOffEvent++;
    }

    public void incrNumberProcessingReasonPowerRestoredBeforeOffNotificationSent() {
        numberProcessingReasonPowerRestoredBeforeOffNotificationSent++;
    }

    public void incrNumberProcessingReasonPredicted() {
        numberProcessingReasonPredicted++;
    }
    
    public void incrNumberOfStaleEventsInBatch() {
        numberOfStaleEventsInBatch++;
    }

    public void incrProcessingReasonTypeCount(OutageEvent event) {

        switch (event.getValueAddProcessingReasonType()) {

			case None:
				incrNumberProcessingReasonNone();
				break;

			case PreviousOutageEventAsOfDateIsGreater:
				incrNumberProcessingReasonPreviousOutageEventAsOfDateIsGreater();
				break;

			case NewerOutageEventExist:
				incrNumberProcessingReasonNewerOutageEventExist();
				break;

			case PowerHasBeenRestored:
				incrNumberProcessingReasonPowerHasBeenRestored();
				break;

			case PreviousOutageProcessedNotificationRule:
				incrNumberProcessingReasonPreviousOutageProcessedNotificationRule();
				break;

			case OutageHasExpired:
				incrNumberProcessingReasonOutageHasExpired();
				break;

			case CancelledRestore:
				incrNumberProcessingReasonCancelledRestore();
				break;

			case Held:
				incrNumberProcessingReasonHeld();
				break;

			case OutageFirstNotificationWithETR:
				incrNumberProcessingReasonOutageFirstNotificationWithETR();
				break;

			case PredictedOutageFirstNotificationWithNoETR:
				incrNumberProcessingReasonPredictedOutageFirstNotificationWithNoETR();
				break;

			case CurrentStatusMomentaryWait:
				incrNumberProcessingReasonCurrentStatusMomentaryWait();
				break;

			case ExceptionAlertNoETRValueForOffEvent:
				incrNumberProcessingReasonExceptionAlertNoETRValueForOffEvent();
				break;

			case PowerRestoredBeforeOffNotificationSent:
				incrNumberProcessingReasonPowerRestoredBeforeOffNotificationSent();
				break;

			case Predicted:
				incrNumberProcessingReasonPredicted();
				break;
		}
    }

    public OutageBatchStatistic getOutageBatchStatistics(String batchKey) {

        logger.debug("Set Batch Key Statistics");
        OutageBatchStatistic outageBatchStatistic = new OutageBatchStatistic();
		outageBatchStatistic.setBatchKey(batchKey);

		outageBatchStatistic.setDroppedEventCount(droppedEventCount);
		outageBatchStatistic.setNumberOfEventToProcess(numberOfEventToProcess);
		outageBatchStatistic.setNumberOfMomentaryEventsDeleted(numberOfMomentaryEventsDeleted);
		outageBatchStatistic.setNumberOfMomentaryEventToProcess(numberOfMomentaryEventToProcess);

		outageBatchStatistic.setNumberOfRestoredEventToProcess(numberOfRestoredEventToProcess);

		outageBatchStatistic.setNumberOfSustainedEventsDeleted(numberOfSustainedEventsDeleted);
		outageBatchStatistic.setNumberOfSustainedEventsToProcess(numberOfSustainedEventsToProcess);

		outageBatchStatistic.setNumberOfEventsInBatch(numberOfEventsInBatch);

		outageBatchStatistic.setNumberProcessingReasonNone(numberProcessingReasonNone);

		outageBatchStatistic.setNumberProcessingReasonPreviousOutageEventAsOfDateIsGreater(
				numberProcessingReasonPreviousOutageEventAsOfDateIsGreater);

		outageBatchStatistic
				.setNumberProcessingReasonNewerOutageEventExist(numberProcessingReasonNewerOutageEventExist);

		outageBatchStatistic.setNumberProcessingReasonPowerHasBeenRestored(numberProcessingReasonPowerHasBeenRestored);

		outageBatchStatistic.setNumberProcessingReasonOutageFirstNotificationWithETR(
				numberProcessingReasonPreviousOutageProcessedNotificationRule);

		outageBatchStatistic.setNumberProcessingReasonOutageHasExpired(numberProcessingReasonOutageHasExpired);

		outageBatchStatistic.setNumberProcessingReasonCancelledRestore(numberProcessingReasonCancelledRestore);

		outageBatchStatistic.setNumberProcessingReasonHeld(numberProcessingReasonHeld);

		outageBatchStatistic.setNumberProcessingReasonOutageFirstNotificationWithETR(
				numberProcessingReasonOutageFirstNotificationWithETR);

		outageBatchStatistic.setNumberProcessingReasonPredictedOutageFirstNotificationWithNoETR(
				numberProcessingReasonPredictedOutageFirstNotificationWithNoETR);

		outageBatchStatistic
				.setNumberProcessingReasonCurrentStatusMomentaryWait(numberProcessingReasonCurrentStatusMomentaryWait);

		outageBatchStatistic.setNumberProcessingReasonExceptionAlertNoETRValueForOffEvent(
				numberProcessingReasonExceptionAlertNoETRValueForOffEvent);

		outageBatchStatistic.setNumberProcessingReasonPowerRestoredBeforeOffNotificationSent(
				numberProcessingReasonPowerRestoredBeforeOffNotificationSent);

        outageBatchStatistic.setNumberProcessingReasonPredicted(numberProcessingReasonPredicted);
        outageBatchStatistic.setNumberOfStaleEventsInBatch(numberOfStaleEventsInBatch);

        return outageBatchStatistic;
    }

	public int getDroppedEventCount() {
		return droppedEventCount;
	}

	public void setDroppedEventCount(int droppedEventCount) {
		this.droppedEventCount = droppedEventCount;
	}
}