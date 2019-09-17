package com.aep.cx.outage.alerts.aws.request.handlers;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.outage.business.OutageNotificationService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class ProcessSustainedRestoredOutages implements RequestHandler<ArrayList<String>, String> {

    final Logger logger = LogManager.getLogger(ProcessSustainedRestoredOutages.class);

    private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

    public ProcessSustainedRestoredOutages() {
    }

    // Test purpose only.
    ProcessSustainedRestoredOutages(AmazonS3 s3) {
        this.s3 = s3;
    }

    
    @Override
    public String handleRequest(ArrayList<String> recordKeys, Context context) {
    	OutageNotificationService outageNotificationService = new OutageNotificationService();
        
        String bucketToLookInto = System.getenv("BUCKET_NM_TO_FETCH_KEYS");
        
        outageNotificationService.buildEventNotificationsAsBatch(recordKeys, bucketToLookInto);
        logger.info("Successfully Built Email/SMS event notifications from sustained outage event.");

        return "Success";
    }
}