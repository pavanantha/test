package com.aep.cx.outage.business;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.preferences.dao.CustomerPreference;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.aep.cx.utils.alerts.notification.msessages.BuildEmail;
import com.aep.cx.utils.alerts.notification.msessages.BuildSMS;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OutageNotificationService {

	final Logger logger = LogManager.getLogger(OutageNotificationService.class);
	AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
	ObjectMetadata metadata = new ObjectMetadata();

	public void processRecord(OutageEvent outageEvent, String batchKey, String bucket,
			HashMap<String, ArrayList<CustomerPreferences>> preferences) {
		/*
		 * Pass the outage and preferences information to the OutageNotificationManager
		 * which will build email and text payloads to be put into two S3 buckets
		 */

		LoadData2S3 loadData = new LoadData2S3();
		OutageNotificationManager notificationManager = new OutageNotificationManager();

		logger.debug("Building email and SMS notification payloads.");
		BuildEmail email = notificationManager.buildEmailNotification(outageEvent, batchKey, bucket, preferences);
		BuildSMS sms = notificationManager.buildSMSNotification(outageEvent, batchKey, bucket, preferences);

		logger.debug("Writing email payload: " + email.getXatPayload().get(0) + "\n into S3 bucket: "
				+ System.getenv("NOTIFICATION_EMAIL_BATCHED_BUCKET"));
		loadData.loadData(System.getenv("NOTIFICATION_EMAIL_BATCHED_BUCKET"), batchKey, email.getXatPayload());

		logger.debug("Writing SMS payload: " + sms.getSmsPayload().get(0) + "\n into S3 bucket: "
				+ System.getenv("NOTIFICATION_SMS_BATCHED_BUCKET"));
		loadData.loadData(System.getenv("NOTIFICATION_SMS_BATCHED_BUCKET"), batchKey, sms.getSmsPayload());

		logger.debug("Writing email to MACSS outage history: " + email.getMacssload().get(0) + "\nto S3 bucket "
				+ System.getenv("OUTAGE_HISTORY_MACSS"));
		loadData.loadData(System.getenv("OUTAGE_HISTORY_MACSS"), "email_" + batchKey, email.getMacssload());

		logger.debug("Writing SMS to MACSS outage history: " + sms.getMacssload().get(0) + "\nto S3 bucket "
				+ System.getenv("OUTAGE_HISTORY_MACSS"));
		loadData.loadData(System.getenv("OUTAGE_HISTORY_MACSS"), "sms_" + batchKey, sms.getMacssload());
	}

	public OutageEvent parseRecord(S3Event input) {

		for (S3EventNotificationRecord record : input.getRecords()) {

			String s3Bucket = record.getS3().getBucket().getName();
			String s3Key = record.getS3().getObject().getKey();

			ObjectMapper mapper = new ObjectMapper();
			try {
				S3Object object = s3Client.getObject(new GetObjectRequest(s3Bucket, s3Key));
				InputStream objectData = object.getObjectContent();

				ArrayList<OutageEvent> outageEventList = mapper.readValue(objectData,
						new TypeReference<ArrayList<OutageEvent>>() {
						});
				object.close();
				return outageEventList.get(0);
			} catch (IOException e) {
				logger.error("Failed in parseRecord. " + e.getMessage());
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public void buildEventNotifications(OutageEvent outageEvent, String batchKey, String bucket,
			HashMap<String, ArrayList<CustomerPreferences>> preferences) {
		logger.debug("Entered into buildEventNotifications() to build Email/SMS Notifications per Premise Number");
		/*
		 * Pass the outage and preferences information to the OutageNotificationManager
		 * which will build email and text payloads to be put into two S3 buckets
		 */

		LoadData2S3 loadData = new LoadData2S3();
		OutageNotificationManager notificationManager = new OutageNotificationManager();

		logger.debug("Building email and SMS notification payloads.");
		BuildEmail email = notificationManager.buildEmailNotification(outageEvent, batchKey, bucket, preferences);
		
		//SQSProducer sqsProducer = new SQSProducer();
		
		if(!email.getXatPayload().isEmpty()) {
			
			/*sqsProducer.InsertMesage(email.getXatPayload(), "email_test", 0);
			sqsProducer.InsertMesage(email.getMacssload(), "email_history_test", 0);*/
			
			loadData.loadData(System.getenv("NOTIFICATION_EMAIL_BATCHED_BUCKET"), batchKey, email.getXatPayload());
			loadData.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BATCHED_BUCKET"), "email_" + batchKey, email.getMacssload());
		}

		BuildSMS sms = notificationManager.buildSMSNotification(outageEvent, batchKey, bucket, preferences);
		
		if(!sms.getSmsPayload().isEmpty()) {
			/*sqsProducer.InsertMesage(sms.getSmsPayload(), "sms_test", 0);
			sqsProducer.InsertMesage(sms.getMacssload(), "sms_history_test", 0);*/
			loadData.loadData(System.getenv("NOTIFICATION_SMS_BATCHED_BUCKET"), batchKey, sms.getSmsPayload());
			loadData.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BATCHED_BUCKET"), "sms_" + batchKey, sms.getMacssload());
		}

	}
	
	public void buildEventNotificationsAsBatch(ArrayList<String> recordKeys, String bucket) {
		
		logger.debug("Entered into buildEventNotifications() to build Email/SMS Notifications per Premise Number");
		
		ArrayList<String> emailNotifications =  new ArrayList<>();
		ArrayList<String> smsNotifications =  new ArrayList<>();
		ArrayList<String> messagehistoryNotifications =  new ArrayList<>();
		
		
		try {
			CustomerPreferencesService customerPreferencesService = new CustomerPreferencesService();

			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
			for (int i = 0; i < recordKeys.size(); i++) {

				ObjectMapper mapper = new ObjectMapper();
				OutageEvent outageEvent = null;
				try {
					S3Object object = s3Client.getObject(new GetObjectRequest(bucket, recordKeys.get(i)));
					InputStream objectData = object.getObjectContent();
					logger.debug("mapper.readValue for Key -- " + recordKeys.get(i));
					ArrayList<OutageEvent> outageEventList = mapper.readValue(objectData,
							new TypeReference<ArrayList<OutageEvent>>() {
							});
					outageEvent = outageEventList.get(0);
					object.close();
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}

				if (outageEvent == null) {
					logger.error(
							"Sustained Outage Event not returned possible cause, record had a reference, but deleted before retrieval");
					continue;
				}

				logger.debug("Parsed outage event - premise: " + outageEvent.getPremiseNumber() + " alert name: "
						+ outageEvent.getValueAddAlertName());

				logger.debug("GetCustomerPreferences");
				HashMap<String, ArrayList<CustomerPreferences>> prefs = customerPreferencesService
						.getCustomerPreferences(outageEvent);

				if (prefs == null || prefs.isEmpty()) {
					logger.debug("Retrieved No Customer preferences : ");
					LoadData2S3 loadData = new LoadData2S3();
					loadData.loadData(System.getenv("NO_PREFERENCES"), recordKeys.get(i), outageEvent.toString());
					s3Client = AmazonS3ClientBuilder.standard().build();
					s3Client.deleteObject(bucket, recordKeys.get(i));
					continue;
				}

				logger.info("Retrieved customer preferences - account number: "
						+ prefs.get(outageEvent.getPremiseNumber()).get(0).getCustomerInfo().getAccountNumber());

				OutageNotificationManager notificationManager = new OutageNotificationManager();

				logger.info("Building email and SMS notification payloads.");
				BuildEmail email = notificationManager.buildEmailNotification(outageEvent, recordKeys.get(i), bucket,
						prefs);
				if (!email.getXatPayload().isEmpty()) {
					emailNotifications.addAll(email.getXatPayload());
					messagehistoryNotifications.addAll(email.getMacssload());
				}
				BuildSMS sms = notificationManager.buildSMSNotification(outageEvent, recordKeys.get(i), bucket, prefs);
				if (!sms.getSmsPayload().isEmpty()) {
					smsNotifications.addAll(sms.getSmsPayload());
					messagehistoryNotifications.addAll(sms.getMacssload());
				}
		    }
		 } catch(Exception e) {
			e.printStackTrace();
			logger.error("Failed in processing sustained outage Event.");
		 }
		 
		 String prefix = "email_";
	            
		 putBatchedListIntoS3(emailNotifications, System.getenv("NOTIFICATION_EMAIL_BATCHED_BUCKET"), prefix);
		 prefix = "sms_";
		 putBatchedListIntoS3(emailNotifications, System.getenv("NOTIFICATION_SMS_BATCHED_BUCKET"), prefix);
		 prefix = "msg_hist_";
		 putBatchedListIntoS3(emailNotifications, System.getenv("NOTIFICATION_MESSAGE_HISTORY_BATCHED_BUCKET"), prefix);
	}

	/**
	 * 
	 */
	private void putBatchedListIntoS3(ArrayList<String> list, String bucket, String prefix) {
		try {
			 ObjectMapper mapper = new ObjectMapper();
             byte[] bytes = mapper.writeValueAsBytes(list);
             InputStream is = new ByteArrayInputStream(bytes);
             metadata = new ObjectMetadata();
             metadata.setContentLength(bytes.length);
             metadata.setContentType("application/json");

             DateTime tm = new DateTime(System.currentTimeMillis());
             String key = tm.toString("yyyy") + "-" + tm.toString("MMM") + "-" + tm.getDayOfMonth() + "/prefix"
                     + tm.toString("HHmmssSSS");
             s3Client.putObject(bucket, key, is, metadata);
             logger.info(" Email Notification Events Batch Created as key: " + key + " in Bucket: " + bucket);             
         } catch (JsonProcessingException e) {
             e.printStackTrace();
             logger.debug("Error : " + e.getMessage());
         } catch (SdkClientException e) {
             e.printStackTrace();
             logger.debug("Error : " + e.getMessage());
         }
	}
}