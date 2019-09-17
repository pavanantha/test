package com.aep.cx.billing.alerts.lambda.handlers;

import java.util.ArrayList;

import com.aep.cx.billing.alerts.business.PaymentService;
import com.aep.cx.billing.alerts.business.SubscriptionsService;
import com.aep.cx.billing.alerts.enrollment.events.EnrollmentAlerts;
import com.aep.cx.billing.events.Payment;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProcessSubscriptions implements RequestHandler<S3Event, String> {

    private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

    public ProcessSubscriptions() {}

    // Test purpose only.
    ProcessSubscriptions(AmazonS3 s3) {
        this.s3 = s3;
    }

    @Override
    public String handleRequest(S3Event event, Context context) {
    	LambdaLogger logger = context.getLogger();
    	logger.log("Received event: " + event);

        ObjectMapper mapper = new ObjectMapper();
        // Get the object from the event and show its content type
        String bucket = event.getRecords().get(0).getS3().getBucket().getName();
        String key = event.getRecords().get(0).getS3().getObject().getKey();
        try {
            S3Object response = s3.getObject(new GetObjectRequest(bucket, key));
            String contentType = response.getObjectMetadata().getContentType();
            ArrayList<EnrollmentAlerts> enrollmentAlerts = mapper.readValue(response.getObjectContent(), new TypeReference<ArrayList<EnrollmentAlerts>>() {});
            logger.log(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(enrollmentAlerts));
            
            SubscriptionsService ss = new SubscriptionsService();
            String result = ss.BuildProfileContent(enrollmentAlerts,key);
            
            logger.log("Subscriptions Service Result: " + result);
            return contentType;
        } catch (Exception e) {
            e.printStackTrace();
            context.getLogger().log(String.format(
                "Error getting object %s from bucket %s. Make sure they exist and"
                + " your bucket is in the same region as this function.", key, bucket));
            return "Failed";
        }
    }
}