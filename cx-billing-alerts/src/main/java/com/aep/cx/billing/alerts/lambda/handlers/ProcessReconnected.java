package com.aep.cx.billing.alerts.lambda.handlers;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.billing.alerts.business.BillingAlertsService;
import com.aep.cx.billing.events.Reconnected;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class ProcessReconnected implements RequestHandler<S3Event, String> {

    private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();
    
    final Logger logger = LogManager.getLogger(ProcessReconnected.class);
    
    private BillingAlertsService service;

    public ProcessReconnected() {
    	service = new BillingAlertsService();
    }

    // Test purpose only.
    ProcessReconnected(AmazonS3 s3) {
        this.s3 = s3;
        service = new BillingAlertsService();
    }
    
    @Override
    public String handleRequest(S3Event event, Context context) {
        logger.debug("Received event: " + event);
        ArrayList<Reconnected> reconnectedList = service.parseS3EventData(event, new Reconnected());
        service.buildAlertContent(reconnectedList);
        return "Success";

    }
}