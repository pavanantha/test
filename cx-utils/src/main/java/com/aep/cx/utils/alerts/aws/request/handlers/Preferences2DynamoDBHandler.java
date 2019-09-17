package com.aep.cx.utils.alerts.aws.request.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.macss.customer.subscriptions.MACSSIntegrationWrapper;
import com.aep.cx.macss.customer.subscriptions.MACSSLayout;
import com.aep.cx.preferences.dao.BillingPreferencesDAO;
import com.aep.cx.preferences.dao.CustomerPreferencesDao;
import com.aep.cx.utils.enums.PreferencesTypes;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Preferences2DynamoDBHandler implements RequestHandler<S3Event, String> {

	static final Logger logger = LogManager.getLogger(Preferences2DynamoDBHandler.class);
	
	private Regions clientRegion = Regions.US_EAST_1;

	@Override
	public String handleRequest(S3Event input, Context context) {
		//AmazonS3Client defaultS3Client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());
		AmazonS3 defaultS3Client = AmazonS3ClientBuilder.standard().build();
		
		for(S3EventNotificationRecord record : input.getRecords()) {
			String s3Bucket = record.getS3().getBucket().getName();
			String s3Key = record.getS3().getObject().getKey();
			logger.debug("found Bucket: " + s3Bucket + " and Key: " + s3Key);
			System.out.println(" Sysout ******** found Bucket: " + s3Bucket + " and Key: " + s3Key);
	        // retrieve s3 object
	        S3Object object = defaultS3Client.getObject(new GetObjectRequest(s3Bucket, s3Key));
	        InputStream objectData = object.getObjectContent();
	        
	        ObjectMapper mapper = new ObjectMapper();
	        try {
				ArrayList<MACSSIntegrationWrapper> preferencesList = mapper.readValue(objectData, new TypeReference<ArrayList<MACSSIntegrationWrapper>>(){});
				for (MACSSIntegrationWrapper MQMessage : preferencesList) {
					MACSSLayout ml = new MACSSLayout(MQMessage.getMessageString());
					CustomerPreferencesDao cpDao = new CustomerPreferencesDao();
					cpDao.updateCustomerPreference(ml.getCustomerPreferences());
					
					System.out.println("Outage Preferences updated successfully : " +  preferencesList.size());
										
					BillingPreferencesDAO  bpDao = new BillingPreferencesDAO();					
					
					bpDao.updateCustomerPreference(ml.getBillPayPreferences(),PreferencesTypes.BILLINGPAYMENT.toString());
					System.out.println("Billing Preferences updated successfully : " +  ml.getBillPayPreferences().getCustomerContacts().size());
					
					bpDao.updateCustomerPreference(ml.getPowerPayPreferences(),PreferencesTypes.POWERPAY.toString());
					System.out.println("PowerPay Preferences updated successfully : " +  ml.getPowerPayPreferences().getCustomerContacts().size());
					
					bpDao.updateCustomerPreference(ml.getOrderPreferences(),PreferencesTypes.ORDER.toString());
					System.out.println("Order Preferences updated successfully : " +  ml.getOrderPreferences().getCustomerContacts().size());
					
				}
				
				System.out.println("Number of Records : " +  preferencesList.size() + " %%%%%%%%%% MQMessage " + preferencesList.get(0) + " %%%%%%%%%%");
			} catch (IOException e) {
				e.printStackTrace();
				return "FAILED";
			}	   
		}
		return "SUCCESS";
	}
}