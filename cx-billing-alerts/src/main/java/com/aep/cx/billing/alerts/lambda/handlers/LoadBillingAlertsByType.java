package com.aep.cx.billing.alerts.lambda.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.load.billing.alerts.service.Load2S3BillingService;
import com.aep.cx.macss.customer.subscriptions.MACSSIntegrationWrapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

//api to landing zone

public class LoadBillingAlertsByType implements RequestHandler<ArrayList<MACSSIntegrationWrapper>, String> {

    private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();
    final Logger logger = LogManager.getLogger(LoadBillingAlertsByType.class);

    public LoadBillingAlertsByType() {}

    // Test purpose only.
    LoadBillingAlertsByType(AmazonS3 s3) {
        this.s3 = s3;
    }

    @Override
    public String handleRequest(ArrayList<MACSSIntegrationWrapper> events, Context context) {
        logger.debug("Received event: " + events);
		
		for (MACSSIntegrationWrapper macssIntegrationWrapper : events) {
			
			logger.debug("alerts content:"+ macssIntegrationWrapper.getMessageString());
		}

        try {
    		Load2S3BillingService.Load2s3byAlert(events);

            return "Successful Loading Content from Alerts by TYPE!! Content Size=" +events.size()  ;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed Loading Content from Alerts by TYPE!! Content size=" + events.size());
            return "Failed Loading Content from Alerts by TYPE!! Content size=" +events.size()  ;
        }
    }
}