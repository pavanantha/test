package com.aep.cx.outage.alerts.aws.request.handlers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BatchEmailNotifications implements RequestHandler<ArrayList<String>, String> {

    final Logger logger = LogManager.getLogger(BatchEmailNotifications.class);

    @Override
    public String handleRequest(ArrayList<String> recordKeys, Context context) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
        
        ObjectMetadata metadata;
        ObjectMapper mapper = null;
       
        logger.debug("Batching up " + recordKeys.size() + " records from the email notifications bucket");

        Stream<String> stream = Stream.of();
        for (int i = 0; i < recordKeys.size(); i++) {

            // Get each object from the bucket and build a list of email payloads to send to
            // ExactTarget

            ArrayList<String> emailPayload;
            S3Object emailPayloadObject = null;
            mapper = new ObjectMapper();
            try {
            	emailPayloadObject = s3Client
            			.getObject(new GetObjectRequest(System.getenv("NOTIFICATION_EMAIL_BUCKET"), recordKeys.get(i)));
            	
            	InputStream emailPayloadObjectData = emailPayloadObject.getObjectContent();
                logger.debug("mapper.readValue for Key -- " + recordKeys.get(i));
                emailPayload = mapper.readValue(emailPayloadObjectData, new TypeReference<ArrayList<String>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }finally{
            	if(null != emailPayloadObject) {
            		try {
						emailPayloadObject.close();
					} catch (IOException e) {
						//do nothing
					}
            	}
            }

            logger.debug("Retrieved email notification with premise number " + recordKeys.get(i) + " from bucket "
                    + System.getenv("NOTIFICATION_EMAIL_BUCKET") + " and payload:\n" + emailPayload);
            // Delete the objects as we read them from the bucket
            s3Client.deleteObject(System.getenv("NOTIFICATION_EMAIL_BUCKET"), recordKeys.get(i));

            logger.debug("Deleted email notification with premise number " + recordKeys.get(i) + " from bucket "
                    + System.getenv("NOTIFICATION_EMAIL_BUCKET") + ".");

            // combinedEmailPayload.addAll(emailPayload);
            stream = Stream.concat(stream, emailPayload.stream());

            if (i == (recordKeys.size() - 1)) {
                List<String> combinedEmailPayload = stream.collect(Collectors.toList());

                logger.debug("Batched email payload:");
                /*  
                 * uncomment the below code to see the payload in log files
                 */
                //combinedEmailPayload.forEach(System.out::println);

                try {
                    mapper = new ObjectMapper();
                    byte[] bytes = mapper.writeValueAsBytes(combinedEmailPayload);
                    InputStream is = new ByteArrayInputStream(bytes);
                    metadata = new ObjectMetadata();
                    metadata.setContentLength(bytes.length);
                    metadata.setContentType("application/json");

                    DateTime tm = new DateTime(System.currentTimeMillis());
                    String key = tm.toString("yyyy") + "-" + tm.toString("MMM") + "-" + tm.getDayOfMonth() + "/email_"
                            + tm.toString("HHmmssSSS");
                    s3Client.putObject(System.getenv("NOTIFICATION_EMAIL_BATCHED_BUCKET"), key, is, metadata);
                    logger.debug(" Email Notification Events Batch Created as key: " + key + " in Bucket: " + System.getenv("NOTIFICATION_EMAIL_BATCHED_BUCKET"));
                    stream = Stream.of();
                } catch (JsonProcessingException | SdkClientException e) {
                    e.printStackTrace();
                    logger.error("Error : " + e.getMessage());
                    return "FAILED";
                } 
            }
        }
        return "SUCCESS";
    }
}