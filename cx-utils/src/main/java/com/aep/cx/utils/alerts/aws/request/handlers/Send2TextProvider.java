package com.aep.cx.utils.alerts.aws.request.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.aep.cx.utils.alerts.common.data.AlertsNotificationData;
import com.aep.cx.utils.thirdparty.SMSDelivery;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Send2TextProvider implements RequestHandler<S3Event, String> {

    private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

    public Send2TextProvider() {}

    // Test purpose only.
    Send2TextProvider(AmazonS3 s3) {
        this.s3 = s3;
    }

    @Override
    public String handleRequest(S3Event event, Context context) {
        context.getLogger().log("Received event: " + event);

        // Get the object from the event and show its content type
        String bucket = event.getRecords().get(0).getS3().getBucket().getName();
        String key = event.getRecords().get(0).getS3().getObject().getKey();
        try {
            S3Object response = s3.getObject(new GetObjectRequest(bucket, key));
            String contentType = response.getObjectMetadata().getContentType();
            context.getLogger().log("CONTENT TYPE: " + contentType);
            
            context.getLogger().log("BuildEmailSMSAlertHandler: Number of Records ");
			InputStream objectData = response.getObjectContent();

			ObjectMapper mapper = new ObjectMapper();
			ArrayList<String> textNotifications = mapper.readValue(objectData,
					new TypeReference<ArrayList<String>>() {
					});
			String i2smsResponse = SMSDelivery.CallI2SMS(textNotifications);
            return "Send to I2SMS is SuccessFull from :"+ bucket+" and key is " + key +i2smsResponse;
            
        } 
        catch (JsonMappingException e) {
        	context.getLogger().log("Error:"+e.getMessage()+e.getStackTrace());
        	return "Send to I2SMS is Failed with Json Mapping Excepion :"+ bucket+" and key is " + key +":"+e.getMessage();
        }
        
        catch (JsonParseException e) {
        	context.getLogger().log("Error:"+e.getMessage()+e.getStackTrace());
        	return "Send to I2SMS is Failed with Json Parsing Excepion :"+ bucket+" and key is " + key +":"+e.getMessage();
        }
        
        catch (IOException e) {
        	context.getLogger().log("Error:"+e.getMessage()+e.getStackTrace());
        	return "Send to I2SMS is Failed with IO Excepion :"+ bucket+" and key is " + key +":"+e.getMessage();
        }
        catch (Exception e) {
            e.printStackTrace();
            context.getLogger().log(String.format(
                "Error getting object %s from bucket %s. Make sure they exist and"
                + " your bucket is in the same region as this function.", key, bucket));
            return "Send to I2SMS is Failed with Json Mapping Excepion :"+ bucket+" and key is " + key +":"+e.getMessage();
        }
    }
}