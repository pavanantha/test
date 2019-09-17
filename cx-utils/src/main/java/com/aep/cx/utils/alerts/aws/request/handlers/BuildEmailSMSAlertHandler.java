package com.aep.cx.utils.alerts.aws.request.handlers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.preferences.dao.CustomerPreferencesDao;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.aep.cx.utils.alerts.common.data.AlertsNotificationData;
import com.aep.cx.utils.alerts.notification.msessages.BuildEmail;
import com.aep.cx.utils.alerts.notification.msessages.BuildSMS;
import com.aep.cx.utils.enums.QueueType;
import com.aep.cx.utils.opco.OperatingCompany;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BuildEmailSMSAlertHandler implements RequestHandler<S3Event, String> {

	static final Logger logger = LogManager.getLogger(BuildEmailSMSAlertHandler.class);
	private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

	public BuildEmailSMSAlertHandler() {
	}

	// Test purpose only.
	BuildEmailSMSAlertHandler(AmazonS3 s3) {
		this.s3 = s3;
	}

	@Override
	public String handleRequest(S3Event event, Context context) {
		logger.debug("Received event: " + event);

		// Get the object from the event and show its content type
		String bucket = event.getRecords().get(0).getS3().getBucket().getName();
		String key = event.getRecords().get(0).getS3().getObject().getKey();
		logger.debug("Accessing Bucket: " + bucket + " and Key: " + key);
		int count = 0;
		for (S3EventNotificationRecord record : event.getRecords()) {
			try {
				logger.debug("BuildEmailSMSAlertHandler: Number of Records " + ++count);
				S3Object response = s3.getObject(new GetObjectRequest(bucket, key));
				InputStream objectData = response.getObjectContent();
				String contentType = response.getObjectMetadata().getContentType();

				ObjectMapper mapper = new ObjectMapper();
				mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
				ArrayList<AlertsNotificationData> alertNotifications = mapper.readValue(objectData,
						new TypeReference<ArrayList<AlertsNotificationData>>() {
						});

				CustomerPreferencesDao preferences = new CustomerPreferencesDao();
				HashMap<String, ArrayList<CustomerPreferences>> cpList = preferences
						.getPreferencesByPremise(alertNotifications);
				BuildEmail buildEmail = new BuildEmail(alertNotifications, cpList);
				LoadData2S3 loadData = new LoadData2S3();
				System.out.println("BuildEmailSMSAlertHandler ========= " + buildEmail.getXatPayload().get(0));
				loadData.loadData(System.getenv("NOTIFICATION_EMAIL_BUCKET"), key, buildEmail.getXatPayload());
				loadData.loadData(System.getenv("OUTAGE_HISTORY_MACSS"), "email_" + key, buildEmail.getMacssload());

				// SQSProducer.InsertMesage(buildEmail, QueueType.EMAIL);

				logger.info("completed build: Email#" + buildEmail.toString());

				BuildSMS sms = new BuildSMS(alertNotifications, cpList);
				loadData.loadData(System.getenv("NOTIFICATION_SMS_BUCKET"), key, sms.getSmsPayload());
				loadData.loadData(System.getenv("OUTAGE_HISTORY_MACSS"), "sms_" + key, sms.getMacssload());

				// SQSProducer.InsertMesage(sms, QueueType.TEXT);

				logger.info("completed build: SMS#" + sms.toString());

			} catch (Exception e) {
				e.printStackTrace();
				logger.error(String.format("Error getting object %s from bucket %s. Make sure they exist and"
						+ " your bucket is in the same region as this function.", key, bucket));
				return "FAILED";
			}
		}
		return "SUCCESS";
	}
}