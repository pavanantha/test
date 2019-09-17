package com.aep.cx.utils.alerts.aws.request.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.ws.BindingProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.aep.cx.utils.delivery.MessageDelivery;
import com.aep.cx.utils.delivery.MessageDeliveryHelper;
import com.aep.cx.utils.macss.services.DLVR2XI;
import com.aep.cx.utils.macss.services.DLVR2XIInput;
import com.aep.cx.utils.macss.services.DLVR2XIInputAwsInput;
import com.aep.cx.utils.macss.services.DLVR2XIInputAwsInputAwsRecords;
import com.aep.cx.utils.macss.services.DLVR2XIOutput;
import com.aep.cx.utils.macss.services.MCSKMQXI;
import com.aep.cx.utils.macss.services.MCSKMQXISoap;
import com.aep.cx.utils.macss.services.ObjectFactory;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SendEmailNotificationsHandler implements RequestHandler<S3Event, String> {

	ObjectFactory factory;
	DLVR2XI request;
	DLVR2XIInput input2MACSS;
	DLVR2XIInputAwsInput awsInput;
	DLVR2XIOutput output;
	private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

	static final Logger logger = LogManager.getLogger(SendEmailNotificationsHandler.class);

	@Override
	public String handleRequest(S3Event event, Context context) {

		for (S3EventNotificationRecord record : event.getRecords()) {

			String s3Bucket = record.getS3().getBucket().getName();

			String s3Key = record.getS3().getObject().getKey();

			logger.debug(" Bucket: " + s3Bucket + " and Key: " + s3Key);

			try {
				// retrieve s3 object
				S3Object object = s3.getObject(new GetObjectRequest(s3Bucket, s3Key));

				InputStream objectData = object.getObjectContent();

				ObjectMapper mapper = new ObjectMapper();
				if (objectData != null) {

					ArrayList<String> messageValues = mapper.readValue(objectData,
							new TypeReference<ArrayList<String>>() {
							});

					MessageDeliveryHelper helper = new MessageDeliveryHelper(messageValues,
							System.getenv("DELIVERY_TYPE_EMAIL"));

					logger.debug("input Type:" + helper.getInput2MACSS().getAwsRequest());

					logger.debug("input Count:" + helper.getInput2MACSS().getAwsRecCnt());

					logger.debug("MessageDelivery.Call2Macss");
					DLVR2XIOutput output = MessageDelivery.Call2Macss(helper.getInput2MACSS());

					logger.debug("MessageDelivery.Call2Macss(input2MACSS)");
					output = MessageDelivery.Call2Macss(input2MACSS);

					if (output.getAwsResult() == "1") {
						logger.error("Call 2 macss failed for email" + DateTime.now().toString());
					}
				}
				object.close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(String.format("Error getting object %s from bucket %s. Make sure they exist and"
						+ " your bucket is in the same region as this function."));
				return "FAILED";
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(String.format("Error getting object %s from bucket %s. Make sure they exist and"
						+ " your bucket is in the same region as this function."));
				return "FAILED";
			}
		}
		return "SUCCESS";
	}
}