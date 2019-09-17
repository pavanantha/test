package com.aep.cx.billing.alerts.lambda.handlers;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.billing.alerts.business.OrderTrackingService;
import com.aep.cx.billing.alerts.business.PaymentService;
import com.aep.cx.billing.events.OrderTracking;
import com.aep.cx.billing.events.Payment;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProcessOrderTracking implements RequestHandler<S3Event, String> {

    private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();
    final Logger logger = LogManager.getLogger(PaymentService.class);

    public ProcessOrderTracking() {}

    // Test purpose only.
    ProcessOrderTracking(AmazonS3 s3) {
        this.s3 = s3;
    }

    
    @Override
    public String handleRequest(S3Event event, Context context) {
        context.getLogger().log("Received event: " + event);
        ObjectMapper mapper = new ObjectMapper();

        // Get the object from the event and show its content type
        String bucket = event.getRecords().get(0).getS3().getBucket().getName();
        String key = event.getRecords().get(0).getS3().getObject().getKey();
        try {
            S3Object response = s3.getObject(new GetObjectRequest(bucket, key));
            String contentType = response.getObjectMetadata().getContentType();
            context.getLogger().log("CONTENT TYPE: " + contentType);

            ArrayList<OrderTracking> orderTrackingAlerts = mapper.readValue(response.getObjectContent(), new TypeReference<ArrayList<OrderTracking>>() {});
            logger.info(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(orderTrackingAlerts));
            
            OrderTrackingService os = new OrderTrackingService();
            String result = os.BuildOrderTrackingContent(orderTrackingAlerts, key);
            
            logger.info("Payment Service Result: " + result);
            return "Success";
        } catch (Exception e) {
            e.printStackTrace();
            context.getLogger().log(String.format(
                "Error getting object %s from bucket %s. Make sure they exist and"
                + " your bucket is in the same region as this function.", key, bucket));
            return "Failed";
        }
    }
}