package com.aep.cx.outage.alerts.enums;

public enum ValueAddProcessingReasonType {
    None, PreviousOutageEventAsOfDateIsGreater, NewerOutageEventExist, PowerHasBeenRestored,
    PreviousOutageProcessedNotificationRule, OutageHasExpired, CancelledRestore, Held, OutageFirstNotificationWithETR,
    PredictedOutageFirstNotificationWithNoETR, CurrentStatusMomentaryWait, ExceptionAlertNoETRValueForOffEvent,
    PowerRestoredBeforeOffNotificationSent, Predicted
}