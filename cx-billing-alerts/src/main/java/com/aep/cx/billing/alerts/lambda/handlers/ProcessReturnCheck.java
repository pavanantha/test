package com.aep.cx.billing.alerts.lambda.handlers;

import java.util.ArrayList;

import com.aep.cx.billing.alerts.business.ReturnCheckService;
import com.aep.cx.billing.events.ReturnCheck;
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

public class ProcessReturnCheck implements RequestHandler<S3Event, String> {

    private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

    public ProcessReturnCheck() {}

    // Test purpose only.
    ProcessReturnCheck(AmazonS3 s3) {
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
            ArrayList<ReturnCheck> returnCheckAlerts = mapper.readValue(response.getObjectContent(), new TypeReference<ArrayList<ReturnCheck>>() {});
            logger.log(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(returnCheckAlerts));
            
            String result = ReturnCheckService.BuildReturnCheckContent(returnCheckAlerts,key);
            
            logger.log("Return Check Service Result: " + result);
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