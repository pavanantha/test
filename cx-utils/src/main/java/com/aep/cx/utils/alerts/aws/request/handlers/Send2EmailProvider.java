package com.aep.cx.utils.alerts.aws.request.handlers;

import com.aep.cx.alerts.aws.services.AWSCredentials;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.aep.cx.utils.enums.MessageType;
import com.aep.cx.utils.thirdparty.EmailProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;


import org.joda.time.DateTime;

public class Send2EmailProvider implements RequestHandler<Object, String> {

	@Override
    public String handleRequest(Object input, Context context) {
        context.getLogger().log("Input: " + input);  
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
        String bucketName = System.getenv("DELIVERY_TYPE_EMAIL");
        ArrayList<String> keys = LoadData2S3.getKeyList(bucketName);
        S3Object object;
        ArrayList<String> emailListAll = new ArrayList<String>();
        for (String s3Key : keys) {
    			System.out.println(" Sysout ******** found Bucket: " + bucketName + " and Key: " + s3Key);
    	        // retrieve s3 object
    	        object = s3Client.getObject(new GetObjectRequest(bucketName, s3Key));
    	        InputStream objectData = object.getObjectContent();
    	        
    	        ObjectMapper mapper = new ObjectMapper();
    	        try {
    				ArrayList<String> emailList = mapper.readValue(objectData, new TypeReference<ArrayList<String>>(){});
    				for (String string : emailList) {
    					emailListAll.add(string);						
					}
    				
    				System.out.println("Number of Records : " +  emailList.size() + " %%%%%%%%%%");
    				
    				
    			} catch (IOException e) {
    				e.printStackTrace();
    				return "FAILED";
    			}			
		}
        
        String response = EmailProvider.Call2ExactTarget(MessageType.MCSXAT.toString(), emailListAll);
        
        return "Respomse from Exact Traget" + response;
    }
}
