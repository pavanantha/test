package com.aep.cx.outage.alerts.aws.request.handlers;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.outage.business.CustomerPreferencesService;
import com.aep.cx.outage.business.OutageNotificationService;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class ProcessSustainedOutages implements RequestHandler<S3Event, String> {

    final Logger logger = LogManager.getLogger(ProcessSustainedOutages.class);

    private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

    public ProcessSustainedOutages() {
    }

    // Test purpose only.
    ProcessSustainedOutages(AmazonS3 s3) {
        this.s3 = s3;
    }

    @Override
    public String handleRequest(S3Event input, Context context) {
    	OutageEvent outageEvent = null;
        String key = null;
        String bucket = null;
        
        try {
        	
            CustomerPreferencesService customerPreferencesService = new CustomerPreferencesService();
            OutageNotificationService outageNotificationService = new OutageNotificationService();

            if (input == null) {
                logger.error("Sustained Input is null");
                return "";
            }

            key = input.getRecords().get(0).getS3().getObject().getKey();
            bucket = input.getRecords().get(0).getS3().getBucket().getName();

            logger.debug("Received sustained event record bucket: " + bucket + " key: " + key);
            outageEvent = outageNotificationService.parseRecord(input);

            if (outageEvent == null) {
                logger.debug(
                        "Sustained Outage Event not returned possible cause, record had a reference, but deleted before retrieval");

                return "";
            }

            logger.debug("Parsed outage event - premise: " + outageEvent.getPremiseNumber() + " alert name: "
                    + outageEvent.getValueAddAlertName());

            logger.debug("GetCustomerPreferences");
            HashMap<String, ArrayList<CustomerPreferences>> prefs = customerPreferencesService
                    .getCustomerPreferences(outageEvent);

            if (prefs == null || prefs.isEmpty()) {
                logger.info("Retrieved No Customer Preferences : ");
        		moveOutageEventToRelatedBucket(outageEvent, System.getenv("NO_PREFERENCES"), bucket, key);
                return "Success";
            }

            logger.debug("Retrieved customer preferences - account number: "
                    + prefs.get(outageEvent.getPremiseNumber()).get(0).getCustomerInfo().getAccountNumber());

            outageNotificationService.buildEventNotifications(outageEvent, key, bucket, prefs);
            logger.debug("Successfully Built Email/SMS event notifications from sustained outage event.");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Failed in processing sustained outage Event");
            if(null != outageEvent) {
            	try {
	        		moveOutageEventToRelatedBucket(outageEvent, System.getenv("NOTIFICATION_FAILED_SUSTAINED_BUCKET"), bucket, key);
				} catch (SdkClientException e2) {
					e2.printStackTrace();
					return "Failed";
				}
            }
            return "Failed";
        }

        return "Success";
    }
    
    /**
	 * @param outageEvent
	 * @param bucket
	 * @param key
	 */
	private void moveOutageEventToRelatedBucket(OutageEvent outageEvent, String moveObjectToBucket, String deleteObjectFromBucket, String key) {
		ArrayList<OutageEvent> eventList = new ArrayList<>();
		LoadData2S3 loadData = new LoadData2S3();
		outageEvent.setValueAddCurrentDateTime(DateTime.now());
		eventList.add(outageEvent);
		loadData.loadData(moveObjectToBucket, key, eventList);
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
		s3Client.deleteObject(deleteObjectFromBucket, key);
	}
}
