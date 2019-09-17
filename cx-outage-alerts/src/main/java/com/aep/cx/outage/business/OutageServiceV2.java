package com.aep.cx.outage.business;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.aep.cx.outage.alerts.domains.AlertsNotificationData;
import com.aep.cx.outage.alerts.domains.OutageData;
import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.outage.alerts.enums.ValueAddProcessingReasonType;
import com.aep.cx.outage.business.OutageManager;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class OutageServiceV2 {
	
	final Logger logger = LogManager.getLogger(OutageService.class);
	int momentaryWait = 10;
	AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
	ObjectMetadata metadata = new ObjectMetadata();
	
	public String processBatch(ArrayList<OutageEvent> batch, String batchKey) {

		S3Object refinedObject = null;
		S3Object momentaryObject = null;
		boolean premiseNumberExists;
		boolean hasPreviousOffOutageEvent = true;
		ObjectMapper mapper = null;
		String targetRefinedBucket = null;
		ArrayList<OutageEvent> outageEventList = new ArrayList<OutageEvent>();
		ArrayList<OutageEvent> previousOutageEventList = new ArrayList<OutageEvent>();
		ArrayList<AlertsNotificationData> previousOutageEvent = null;
		ArrayList<AlertsNotificationData> previousMomentaryEvent = null;
		InputStream refinedObjectData = null;
		InputStream momentaryObjectData = null;

		/* Get previousOutage (if it exists), for each OutageEvent in the batch */
		for(OutageEvent outageEvent : batch) {
			String eventKey = batchKey + "/" + outageEvent.getPremiseNumber();
			try {
				refinedObject = s3Client.getObject("alerts-dev-outage-customer-alert", outageEvent.getPremiseNumber());
				premiseNumberExists = true;
			} catch(SdkClientException e) {
				logger.info("premise number " + outageEvent.getPremiseNumber() + " does not exist in sustained bucket");
				logger.info(e.getMessage());
				premiseNumberExists = false;
			}
			
			// Store the previous and current outages
			if(premiseNumberExists) {
				refinedObjectData = refinedObject.getObjectContent();
				mapper = new ObjectMapper();

				try {
					previousOutageEvent = mapper.readValue(refinedObjectData, new TypeReference<ArrayList<AlertsNotificationData>>(){});
					//ArrayList<OutageData> dataList = mapper.readValue(rawObjectData, new TypeReference<ArrayList<OutageData>>(){});
					//powerOnDataList = dataList.get(0).getOutageDataList();
				} catch (IOException e) {
					e.printStackTrace();
					return "FAILED";
				}
				//previousOutageEventList.add(previousOutageEvent.get(0));
				//outageEventList.add(outageEvent);
				previousOutageEventList.add(convertToOutageEvent(previousOutageEvent.get(0)));
				outageEventList.add(outageEvent);

				// Delete corresponding OFF event for each ON event from sustained bucket
				if(outageEvent.getIsPremisePowerOn()) {
					s3Client.deleteObject("alerts-dev-outage-customer-alert", outageEvent.getPremiseNumber());
				}
			} else {
				// Check if power was restored before OFF notification was sent
				try {
					momentaryObject = s3Client.getObject("alerts-dev-outage-momentary", outageEvent.getPremiseNumber());
					hasPreviousOffOutageEvent = true;
				} catch(SdkClientException e) {
					logger.info("premise number " + outageEvent.getPremiseNumber() + " does not exist in momentary bucket");
					logger.info(e.getMessage());
					hasPreviousOffOutageEvent = false;
				}

				if(outageEvent.getIsPremisePowerOn() && hasPreviousOffOutageEvent) {
					momentaryObjectData = momentaryObject.getObjectContent();
					mapper = new ObjectMapper();
					try {
						previousMomentaryEvent = mapper.readValue(momentaryObjectData, new TypeReference<ArrayList<AlertsNotificationData>>(){});
					} catch (IOException e) {
						e.printStackTrace();
						return "FAILED";
					}
					OutageEvent prevMomEvent = convertToOutageEvent(previousMomentaryEvent.get(0));
					prevMomEvent.setValueAddProcessingReasonType(ValueAddProcessingReasonType.PowerRestoredBeforeOffNotificationSent);
					outageEvent.setValueAddProcessingReasonType(ValueAddProcessingReasonType.PowerRestoredBeforeOffNotificationSent);

					s3Client.deleteObject("alerts-dev-outage-momentary", outageEvent.getPremiseNumber());

					// TODO: PUT prevMomEvent and outageEvent in exceptions bucket (Need to create).
					logger.info("PUT OFF and ON events in exception bucket");
					logger.info("OFF: " + prevMomEvent.getPremiseNumber() + " " + prevMomEvent.getValueAddProcessingReasonType().name());
					logger.info("ON: " + outageEvent.getPremiseNumber() + " " + outageEvent.getValueAddProcessingReasonType().name());
				} else {
					OutageEvent nullOutageEvent = new OutageEvent();
					nullOutageEvent.setPremiseNumber(null);
					previousOutageEventList.add(nullOutageEvent);
					outageEventList.add(outageEvent);
				}
			}
		}

		/* 
		 * Pass current and previous outage event lists to OutageManager
		 * to be processed through the business rules.
		 */
		OutageManager outageManager = new OutageManager();
		ArrayList<OutageEvent> processedOutageEvents = outageManager.ProcessRulesBatch(outageEventList, previousOutageEventList);
		mapper = new ObjectMapper();
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// Send the processed events to either the sustained, momentary, or restored buckets
		for(OutageEvent outageEvent : processedOutageEvents) {
			String eventKey = batchKey + "/" + outageEvent.getPremiseNumber();
			/*
			 * If we get an exception state, do NOT add the event
			 * to the sustained or momentary bucket
			 */
			logger.info("Processed outageEvent premise number: " + outageEvent.getPremiseNumber());
			logger.info("Processed outageEvent reason type: " + outageEvent.getValueAddProcessingReasonType().name());
			if((outageEvent.getValueAddProcessingReasonType() ==
			ValueAddProcessingReasonType.PreviousOutageEventAsOfDateIsGreater) ||
			(outageEvent.getValueAddProcessingReasonType() ==
			ValueAddProcessingReasonType.None)) {
				continue;
			}

			if(!outageEvent.getIsPremisePowerOn()) { // OFF Event
				if(!outageEvent.getIsInMomentaryWait()) {
					targetRefinedBucket = "alerts-dev-outage-customer-alert";
				} else {
					targetRefinedBucket = "alerts-dev-outage-momentary";
				}
				logger.info("BUCKET: " + targetRefinedBucket);
				try {
					ArrayList<AlertsNotificationData> putEvent = new ArrayList<AlertsNotificationData>();
					putEvent.add(convertToNotificationData(outageEvent));
					byte[] bytes = mapper.writeValueAsBytes(putEvent);
					InputStream is = new ByteArrayInputStream(bytes);
					metadata = new ObjectMetadata();
					metadata.setContentLength(bytes.length);
					//metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
					metadata.setContentType("application/json");
					s3Client.putObject(targetRefinedBucket, outageEvent.getPremiseNumber(), is, metadata);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
					logger.debug("Error : " + e.getMessage());
					return "FAILED";
				} catch(SdkClientException e) {
					e.printStackTrace();
					logger.debug("Error : " + e.getMessage());
					return "FAILED";	
				}	
			} else { // ON Event
				try {
					ArrayList<AlertsNotificationData> putEvent = new ArrayList<AlertsNotificationData>();
					putEvent.add(convertToNotificationData(outageEvent));
					byte[] bytes = mapper.writeValueAsBytes(putEvent);
					InputStream is = new ByteArrayInputStream(bytes);
					metadata = new ObjectMetadata();
					metadata.setContentLength(bytes.length);
					//metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
					metadata.setContentType("application/json");
					s3Client.putObject("alerts-dev-outage-restored", outageEvent.getPremiseNumber(), is, metadata);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
					logger.debug("Error : " + e.getMessage());
					return "FAILED";
				} catch(SdkClientException e) {
					e.printStackTrace();
					logger.debug("Error : " + e.getMessage());
					return "FAILED";	
				}	
			}
		}
		return "SUCCESS";
	}

	public AlertsNotificationData convertToNotificationData(OutageEvent outageEvent) {

		AlertsNotificationData notificationData = new AlertsNotificationData();
		notificationData.setPremiseNumber(outageEvent.getPremiseNumber());
		notificationData.setAccountNumber(outageEvent.getPremiseNumber() + "0");
		notificationData.setOutageSimpleCause(outageEvent.getOutageSimpleCause());
		notificationData.setEtrType(outageEvent.getOutageETRType());
		notificationData.setOutageEtrTime(outageEvent.getOutageETR());
		notificationData.setOutageCreationTime(outageEvent.getOutageCreationTime());
		notificationData.setOutageRestorationTime(outageEvent.getOutageRestorationTime());
		notificationData.setAlertName(outageEvent.getValueAddAlertName());
		notificationData.setOutageMaxCount(outageEvent.getOutageCustomerMAXCount());
		notificationData.setOutageCurrentCount(outageEvent.getOutageCustomerCount());
		notificationData.setOutageNumber(Integer.parseInt(outageEvent.getOutageNumber()));
		notificationData.setPremisePowerStatus(outageEvent.getPremisePowerStatus());
		notificationData.setOutageStatus(outageEvent.getOutageStatus());
		//notificationData.setOutageAsOfTime(outageEvent.getOutageAsOfTime());
		return notificationData;
	}

	public OutageEvent convertToOutageEvent(AlertsNotificationData alertsNotification) {

		OutageEvent outageEvent = new OutageEvent();
		outageEvent.setPremiseNumber(alertsNotification.getPremiseNumber());
		outageEvent.setOutageSimpleCause(alertsNotification.getOutageSimpleCause());
		outageEvent.setOutageETRType(alertsNotification.getEtrType());
		outageEvent.setOutageETR(alertsNotification.getOutageEtrTime());
		outageEvent.setOutageCreationTime(alertsNotification.getOutageCreationTime());
		//outageEvent.setOutageRestorationTime(alertsNotification.getOutageRestorationTime());
		outageEvent.setOutageCustomerMAXCount(alertsNotification.getOutageMaxCount());
		outageEvent.setOutageCustomerCount(alertsNotification.getOutageCurrentCount());
		outageEvent.setOutageNumber(Integer.toString(alertsNotification.getOutageNumber()));
		outageEvent.setPremisePowerStatus(alertsNotification.getPremisePowerStatus());
		outageEvent.setOutageStatus(alertsNotification.getOutageStatus());
		outageEvent.setOutageType(alertsNotification.getAlertName());
		outageEvent.setOutageAsOfTime(new DateTime(2015, 5, 18, 22, 42, 0, 0));
		return outageEvent;
	}
	
	public ArrayList<OutageEvent> parseBatch(S3Event input)
	{
		for (S3EventNotificationRecord record : input.getRecords()) {
			String s3Bucket = record.getS3().getBucket().getName();
			String s3Key = record.getS3().getObject().getKey();
			logger.debug("found Bucket: " + s3Bucket + " and Key: " + s3Key);
			System.out.println(" Sysout ******** found Bucket: " + s3Bucket + " and Key: " + s3Key);
			// retrieve s3 object
			S3Object object = s3Client.getObject(new GetObjectRequest(s3Bucket, s3Key));
			InputStream objectData = object.getObjectContent();

			ObjectMapper mapper = new ObjectMapper();
			try {
				ArrayList<OutageEvent> outageEventList = mapper.readValue(objectData,
						new TypeReference<ArrayList<OutageEvent>>() {});

				System.out.println("Number of Records : " + outageEventList.size() + " %%%%%%%%%%");
				return outageEventList;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public String persistBatch(ArrayList<Object> inputRecList) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			byte[] bytes = mapper.writeValueAsBytes(inputRecList);
			InputStream is = new ByteArrayInputStream(bytes);
			metadata.setContentLength(bytes.length);
			metadata.setContentType("application/json");
			DateTime tm = new DateTime(System.currentTimeMillis());

			String s3BucketName = System.getenv("POWERON2S3");
			PutObjectRequest putObjRequest = new PutObjectRequest(s3BucketName, tm.toString("yyyy") + "/"
					+ tm.toString("MMM") + "/" + tm.getDayOfYear() + "/outage_alerts_" + tm.toString("HHmmssSSS"), is, metadata);

			logger.debug("**** Putting Object into S3 bucket: " + s3BucketName);
			s3Client.putObject(putObjRequest);

		} catch (JsonProcessingException | AmazonS3Exception ex) {

			ex.printStackTrace();
			logger.debug("Exception : " + ex.getMessage());
			return "FAILED";

		} catch (Exception ex) {

			ex.printStackTrace();
			logger.debug("Exception : " + ex.getMessage());
			return "FAILED";
		}

		return "SUCCESS";
	}
}
