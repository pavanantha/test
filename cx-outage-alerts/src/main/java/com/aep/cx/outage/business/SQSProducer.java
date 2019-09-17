package com.aep.cx.outage.business;

import java.util.List;
import java.util.Optional;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.SendMessageRequest;


public class SQSProducer {
	final AmazonSQS sqs = AmazonSQSClientBuilder.standard().build();
	public String InsertMesage(List<String> message,String queueName,int delaySeconds) {
		System.out.println("sending new message to SQS"+queueName+ message.toString());
        try {
        	
        	String queueUrl = getUrlForQueue(queueName).toString();

        	SendMessageRequest sendRequest = null;
            for (String deliveryMessage : message) {	
            	    sendRequest = new SendMessageRequest(queueUrl, deliveryMessage);
            	    sendRequest.setDelaySeconds(delaySeconds);
	            	System.out.println("sending new message to SQS"+queueName+ " queue:"+deliveryMessage);
	            	sqs.sendMessage(sendRequest);
            }

            return "Successful";

        } catch (final AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means " +
                    "your request made it to Amazon SQS, but was " +
                    "rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
            return "SQS service exception occurred when inserting message" + ase.getCause();
        } catch (final AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means " +
                    "the client encountered a serious internal problem while " +
                    "trying to communicate with Amazon SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
            return "Amazon client exception occurred when inserting message" + ace.getCause();
        }
    }
	
	String getUrlForQueue(String queueName) {
		String queueUrl = null;
	    try {
	        GetQueueUrlResult queueUrlResult = sqs.getQueueUrl(queueName);
	        if (null !=queueUrlResult.getQueueUrl()) {
	            queueUrl = queueUrlResult.getQueueUrl();
	        }
	    } catch (QueueDoesNotExistException e) {
	        
	    	System.out.println("Queue " + queueName + " does not exist, try to create it"+e.getErrorMessage());
	        
	        CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
	        try {
	            queueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
	        } catch (AmazonClientException e2) {
	            System.out.println("Could not create queue " + queueName + ", bundle won't work"+e2.getMessage());
	        }
	    }
	    return queueUrl;
	}	 
}