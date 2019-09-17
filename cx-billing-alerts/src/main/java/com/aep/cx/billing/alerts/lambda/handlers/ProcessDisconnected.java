package com.aep.cx.billing.alerts.lambda.handlers;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.billing.alerts.business.BillingAlertsService;
import com.aep.cx.billing.events.Disconnected;
import com.aep.cx.billing.events.DisconnectNotice;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class ProcessDisconnected implements RequestHandler<S3Event, String> {

    private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();
    private BillingAlertsService service;
    
    final Logger logger = LogManager.getLogger(ProcessDisconnected.class);

    public ProcessDisconnected() {
    	service = new BillingAlertsService();
    }

    // Test purpose only.
    ProcessDisconnected(AmazonS3 s3) {
        this.s3 = s3;
        service = new BillingAlertsService();
    }

    @Override
    public String handleRequest(S3Event event, Context context) {
        logger.debug("Received event: " + event);
        ArrayList<Disconnected> disconnectedList = service.parseS3EventData(event, new Disconnected());
        service.buildAlertContent(disconnectedList);
        return "Success";

    }
}