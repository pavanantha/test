package com.aep.cx.outage.alerts.domains;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutageBatchStatistic {

    String batchKey;
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

    public String getBatchKey() {
        return batchKey;
    }

    public void setBatchKey(String batchKey) {
        this.batchKey = batchKey;
    }

    public int getNumberOfMomentaryEventsDeleted() {
        return numberOfMomentaryEventsDeleted;
    }

    public void setNumberOfMomentaryEventsDeleted(int value) {
        this.numberOfMomentaryEventsDeleted = value;
    }

    public int getNumberOfSustainedEventsDeleted() {
        return numberOfSustainedEventsDeleted;
    }

    public void setNumberOfSustainedEventsDeleted(int value) {
        this.numberOfSustainedEventsDeleted = value;
    }

    public int getNumberOfSustainedEventsToProcess() {
        return numberOfSustainedEventsToProcess;
    }

    public void setNumberOfSustainedEventsToProcess(int value) {
        this.numberOfSustainedEventsToProcess = value;
    }

    public int getNumberOfRestoredEventToProcess() {
        return numberOfRestoredEventToProcess;
    }

    public void setNumberOfRestoredEventToProcess(int value) {
        this.numberOfRestoredEventToProcess = value;
    }

    public int getNumberOfMomentaryEventToProcess() {
        return numberOfMomentaryEventToProcess;
    }

    public void setNumberOfMomentaryEventToProcess(int value) {
        this.numberOfMomentaryEventToProcess = value;
    }

    public int getDroppedEventCount() {
        return droppedEventCount;
    }

    public void setDroppedEventCount(int value) {
        this.droppedEventCount = value;
    }

    public int getNumberOfEventToProcess() {
        return numberOfEventToProcess;
    }

    public void setNumberOfEventToProcess(int value) {
        this.numberOfEventToProcess = value;
    }

    public int getNumberOfEventsInBatch() {
        return numberOfEventsInBatch;
    }

    public void setNumberOfEventsInBatch(int value) {
        this.numberOfEventsInBatch = value;
    }

    public int getNumberProcessingReasonNone() {
        return numberProcessingReasonNone;
    }

    public void setNumberProcessingReasonNone(int value) {
        this.numberProcessingReasonNone = value;
    }

    public int getNumberProcessingReasonPreviousOutageEventAsOfDateIsGreater() {
        return numberProcessingReasonPreviousOutageEventAsOfDateIsGreater;
    }

    public void setNumberProcessingReasonPreviousOutageEventAsOfDateIsGreater(int value) {
        this.numberProcessingReasonPreviousOutageEventAsOfDateIsGreater = value;
    }

    public int getNumberProcessingReasonNewerOutageEventExist() {
        return numberProcessingReasonNewerOutageEventExist;
    }

    public void setNumberProcessingReasonNewerOutageEventExist(int value) {
        this.numberProcessingReasonNewerOutageEventExist = value;
    }

    public int getNumberProcessingReasonPowerHasBeenRestored() {
        return numberProcessingReasonPowerHasBeenRestored;
    }

    public void setNumberProcessingReasonPowerHasBeenRestored(int value) {
        this.numberProcessingReasonPowerHasBeenRestored = value;
    }

    public int getNumberProcessingReasonPreviousOutageProcessedNotificationRule() {
        return numberProcessingReasonPreviousOutageProcessedNotificationRule;
    }

    public void setNumberProcessingReasonPreviousOutageProcessedNotificationRule(int value) {
        this.numberProcessingReasonPreviousOutageProcessedNotificationRule = value;
    }

    public int getNumberProcessingReasonOutageHasExpired() {
        return numberProcessingReasonOutageHasExpired;
    }

    public void setNumberProcessingReasonOutageHasExpired(int value) {
        this.numberProcessingReasonOutageHasExpired = value;
    }

    public int getNumberProcessingReasonCancelledRestore() {
        return numberProcessingReasonCancelledRestore;
    }

    public void setNumberProcessingReasonCancelledRestore(int value) {
        this.numberProcessingReasonCancelledRestore = value;
    }

    public int getNumberProcessingReasonHeld() {
        return numberProcessingReasonHeld;
    }

    public void setNumberProcessingReasonHeld(int value) {
        this.numberProcessingReasonHeld = value;
    }

    public int getNumberProcessingReasonOutageFirstNotificationWithETR() {
        return numberProcessingReasonOutageFirstNotificationWithETR;
    }

    public void setNumberProcessingReasonOutageFirstNotificationWithETR(int value) {
        this.numberProcessingReasonOutageFirstNotificationWithETR = value;
    }

    public int getNumberProcessingReasonPredictedOutageFirstNotificationWithNoETR() {
        return numberProcessingReasonPredictedOutageFirstNotificationWithNoETR;
    }

    public void setNumberProcessingReasonPredictedOutageFirstNotificationWithNoETR(int value) {
        this.numberProcessingReasonPredictedOutageFirstNotificationWithNoETR = value;
    }

    public int getNumberProcessingReasonCurrentStatusMomentaryWait() {
        return numberProcessingReasonCurrentStatusMomentaryWait;
    }

    public void setNumberProcessingReasonCurrentStatusMomentaryWait(int value) {
        this.numberProcessingReasonCurrentStatusMomentaryWait = value;
    }

    public int getNumberProcessingReasonExceptionAlertNoETRValueForOffEvent() {
        return numberProcessingReasonExceptionAlertNoETRValueForOffEvent;
    }

    public void setNumberProcessingReasonExceptionAlertNoETRValueForOffEvent(int value) {
        this.numberProcessingReasonExceptionAlertNoETRValueForOffEvent = value;
    }

    public int getNumberProcessingReasonPowerRestoredBeforeOffNotificationSent() {
        return numberProcessingReasonPowerRestoredBeforeOffNotificationSent;
    }

    public void setNumberProcessingReasonPowerRestoredBeforeOffNotificationSent(int value) {
        this.numberProcessingReasonPowerRestoredBeforeOffNotificationSent = value;
    }

    public int getNumberProcessingReasonPredicted() {
        return numberProcessingReasonPredicted;
    }

    public void setNumberProcessingReasonPredicted(int value) {
        this.numberProcessingReasonPredicted = value;
    }

	public int getNumberOfStaleEventsInBatch() {
		return numberOfStaleEventsInBatch;
	}

	public void setNumberOfStaleEventsInBatch(int numberOfStaleEventsInBatch) {
		this.numberOfStaleEventsInBatch = numberOfStaleEventsInBatch;
	}
}