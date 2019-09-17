package com.aep.cx.outage.alerts.domains;

import java.text.SimpleDateFormat;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "CustomerOutageNotificationState")
public class CustomerOutageNotificationState {

    /* @DynamoDBHashKey(attributeName = "premiseNumber") */
    /* @DynamoDBAttribute(attributeName = "customerAlertName") */
    /* @DynamoDBDocument public class Customer */
    /* @DynamoDBAttribute(attributeName = "customerAlertName") */

    public CustomerOutageNotificationState() {
    }

    /*
     * Notification Time
     */

    // @Getter @Setter int awesomeInteger = 5;
    private SimpleDateFormat notifiedTime; // time of alert we notified customer

    /*
     * Predicted / Estimate To Restoration time that we have notified customer
     */
    private SimpleDateFormat notifiedEstimateTimeToRestoration;

    /*
     * The Outage was held until the Outage Event pass the Sustained Threshold for
     * sustained outage
     */
    private Boolean notificationWasMomentarilyWaited;

    /*
     * The Name of the Alert used for this Outage Behavior
     */
    private String notificationAlertName; //// type of alert we have sent to customer

    /*
     * The Alert Type used for this Outage Behavior
     */
    private String notificationAlertType; //// type of alert we have sent to customer

}