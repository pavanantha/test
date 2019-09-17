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

public class BatchSMSNotifications implements RequestHandler<ArrayList<String>, String> {

    final Logger logger = LogManager.getLogger(BatchSMSNotifications.class);

    @Override
    public String handleRequest(ArrayList<String> recordKeys, Context context) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
       
        ObjectMetadata metadata;
        ObjectMapper mapper = null;
        
        logger.debug("Creating Batch --  " + recordKeys.size() + " records from the sms notifications bucket");

        Stream<String> stream = Stream.of();
        for (int i = 0; i < recordKeys.size(); i++) {

            // Get each object from the bucket and build a list of sms payloads to send to
            // ExactTarget
            ArrayList<String> smsPayload;
            mapper = new ObjectMapper();
            S3Object smsPayloadObject = null;
            try {
            	smsPayloadObject = s3Client
            			.getObject(new GetObjectRequest(System.getenv("NOTIFICATION_SMS_BUCKET"), recordKeys.get(i)));
            	InputStream smsPayloadObjectData = smsPayloadObject.getObjectContent();
                smsPayload = mapper.readValue(smsPayloadObjectData, new TypeReference<ArrayList<String>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }finally {
            	if(null != smsPayloadObject) {
            		try {
						smsPayloadObject.close();
					} catch (IOException e) {
						//Do nothing
					}
            	}
            }
            logger.debug("Retrieved sms notification with premise number " + recordKeys.get(i) + " from bucket "
                    + System.getenv("NOTIFICATION_SMS_BUCKET") + " and payload:\n" + smsPayload);

            // Delete the objects as we read them from the bucket
            s3Client.deleteObject(System.getenv("NOTIFICATION_SMS_BUCKET"), recordKeys.get(i));
            logger.debug("Deleted sms notification with premise number " + recordKeys.get(i) + " from bucket "
                    + System.getenv("NOTIFICATION_SMS_BUCKET") + ".");

            // combinedSMSPayload.addAll(SMSPayload);
            stream = Stream.concat(stream, smsPayload.stream());

            if ( i == (recordKeys.size() - 1)) {
                List<String> combinedSMSPayload = stream.collect(Collectors.toList());

                logger.debug("Batched sms payload:");
                /*  
                 * uncomment the below code to see the payload in log files
                 */
               // combinedSMSPayload.forEach(System.out::println);

                try {
                    mapper = new ObjectMapper();
                    byte[] bytes = mapper.writeValueAsBytes(combinedSMSPayload);
                    InputStream is = new ByteArrayInputStream(bytes);
                    metadata = new ObjectMetadata();
                    metadata.setContentLength(bytes.length);
                    metadata.setContentType("application/json");

                    DateTime tm = new DateTime(System.currentTimeMillis());
                    String key = tm.toString("yyyy") + "-" + tm.toString("MMM") + "-" + tm.getDayOfMonth() + "/sms_"
                            + tm.toString("HHmmssSSS");
                    s3Client.putObject(System.getenv("NOTIFICATION_SMS_BATCHED_BUCKET"), key, is, metadata);
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