package com.aep.cx.utils.alerts.aws.request.handlers;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaAsyncClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FetchListOfKeysFromS3Wrapper implements RequestHandler<Object, String> {
	
	final Logger logger = LogManager.getLogger(FetchListOfKeysFromS3Wrapper.class);

    @Override
    public String handleRequest(Object input, Context context) {
    	ObjectMapper mapper = new ObjectMapper();
         String bucketName = System.getenv("BUCKET_NM_TO_FETCH_KEYS");
         logger.info("Fetching list of keys from bucket:- " + bucketName);

         ArrayList<String> keyList = LoadData2S3.getKeyList(bucketName);
         ArrayList<String> keyListToCreateBatch = new ArrayList<String>();
         
         String lambdaToInvoke = System.getenv("LAMBDA_TO_INVOKE");
         logger.info("Invoking a Lambda:- " + lambdaToInvoke);
         
         InvokeRequest request = new InvokeRequest().withFunctionName(lambdaToInvoke)
         		.withInvocationType("Event");
         
 		int keyCount = 0;
         for (String key : keyList) {
        	 keyCount++;
 			keyListToCreateBatch.add(key);
 			if ( keyListToCreateBatch.size() == 200 || keyList.size() == keyCount ) {
 				/*System.out.println("****************** " + keyListToCreateBatch.size() + "  Events in a Batch ");
 				keyListToCreateBatch.forEach(keys -> {System.out.println(keys);});*/
 				try {
 					request.withPayload(mapper.writeValueAsString(keyListToCreateBatch));
 					AWSLambdaAsyncClient asyncClient = new AWSLambdaAsyncClient();
 					asyncClient.withRegion(Regions.US_EAST_1);

 					InvokeResult invResult = asyncClient.invoke(request);
 					keyListToCreateBatch = new ArrayList<String>();

 				} catch (JsonProcessingException e) {
 					e.printStackTrace();
 					logger.error("Exception:- " + e.getMessage());
 				}
 			}
 		}
 		
 		return "SuccessFul Orchestration of Batch Creator wrapper";
    }

}
