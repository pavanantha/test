package com.aep.cx.billing.alerts.lambda.handlers;
        
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.billing.alerts.business.CPPService;
import com.aep.cx.billing.events.CPPAlert;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProcessCPPAlerts implements RequestHandler<S3Event, String> {

    private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();
    final Logger logger = LogManager.getLogger(ProcessCPPAlerts.class);

    public ProcessCPPAlerts() {}

    // Test purpose only.
    ProcessCPPAlerts(AmazonS3 s3) {
        this.s3 = s3;
    }

    @Override
    public String handleRequest(S3Event event, Context context) {
        logger.debug("Received event: " + event);
        ObjectMapper mapper = new ObjectMapper();

        // Get the object from the event and show its content type
        String bucket = event.getRecords().get(0).getS3().getBucket().getName();
        String key = event.getRecords().get(0).getS3().getObject().getKey();
        try {
            S3Object response = s3.getObject(new GetObjectRequest(bucket, key));
            String contentType = response.getObjectMetadata().getContentType();
            
            ArrayList<CPPAlert> cppEnrollmentAlerts = mapper.readValue(response.getObjectContent(), new TypeReference<ArrayList<CPPAlert>>() {});
            
            CPPService service = new CPPService();
            service.buildCPPContent(cppEnrollmentAlerts, key);            
            logger.debug("CONTENT TYPE: " + contentType);
            return contentType;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(String.format(
                "Error getting object %s from bucket %s. Make sure they exist and"
                + " your bucket is in the same region as this function.", key, bucket));
            return "Failed";
        }
    }
}