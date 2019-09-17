package com.aep.cx.outage.alerts.aws.request.handlers;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

public class ProcessRestoredOutages implements RequestHandler<S3Event, String> {

    final Logger logger = LogManager.getLogger(ProcessRestoredOutages.class);
    private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

    public ProcessRestoredOutages() {
    }

    // Test purpose only.
    ProcessRestoredOutages(AmazonS3 s3) {
        this.s3 = s3;
    }

    @Override
    public String handleRequest(S3Event input, Context context) {
    	OutageEvent outageEvent = null;
    	String bucket = null;
    	String key = null;
    	try {
			CustomerPreferencesService customerPreferencesService = new CustomerPreferencesService();
			OutageNotificationService outageNotificationService = new OutageNotificationService();

			key = input.getRecords().get(0).getS3().getObject().getKey();
			bucket = input.getRecords().get(0).getS3().getBucket().getName();

			outageEvent = outageNotificationService.parseRecord(input);

			if (outageEvent == null) {
			    logger.info(
			            "Restored Outage Event not returned possible cause, record had a reference, but deleted before retrieval");

			    return "False";
			}

			logger.debug("Parsed outage event - premise: " + outageEvent.getPremiseNumber() + " alert name: "
			        + outageEvent.getValueAddAlertName());

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
			logger.info("Successfully processed restored outage Event. Deleting Event Key -- "+ key + " From Restored Bucket -- " + bucket);
			
			return "Success";
		} catch (Exception e) {
			e.printStackTrace();
            logger.info("Failed in processing Restored outage Event");
            if(null != outageEvent) {
            	try {
	        		moveOutageEventToRelatedBucket(outageEvent, System.getenv("NOTIFICATION_FAILED_RESTORED_BUCKET"), bucket, key);
				} catch (SdkClientException e2) {
					e2.printStackTrace();
					return "Failed";
				}
            }
			return "Failed";
		}
    }

	/**
	 * @param outageEvent
	 * @param bucket
	 * @param key
	 */
	private void moveOutageEventToRelatedBucket(OutageEvent outageEvent, String moveObjectToBucket, String deleteObjectFromBucket, String key) {
		ArrayList<OutageEvent> eventList = new ArrayList<>();
		LoadData2S3 loadData = new LoadData2S3();
		eventList.add(outageEvent);
		loadData.loadData(moveObjectToBucket, key, eventList);
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
		s3Client.deleteObject(deleteObjectFromBucket, key);
	}

}