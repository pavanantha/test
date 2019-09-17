package com.aep.cx.utils.alerts.aws.request.handlers;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.aep.cx.utils.delivery.MessageDelivery;
import com.aep.cx.utils.delivery.MessageDeliveryHelper;
import com.aep.cx.utils.macss.services.DLVR2XI;
import com.aep.cx.utils.macss.services.DLVR2XIInput;
import com.aep.cx.utils.macss.services.DLVR2XIInputAwsInput;
import com.aep.cx.utils.macss.services.DLVR2XIOutput;
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

public class SendMessageHistoryHandler implements RequestHandler<S3Event, String> {

	ObjectFactory factory;
	DLVR2XI request;
	DLVR2XIInput input2MACSS;
	DLVR2XIInputAwsInput awsInput;
	DLVR2XIOutput output;
	S3Object response;

	private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();
	static final Logger logger = LogManager.getLogger(SendMessageHistoryHandler.class);

	@Override
	public String handleRequest(S3Event event, Context context) {

		if (event == null) {
			logger.debug("S3 Event is null");
			return "";
		}

		// Get the object from the event and show its content type
		String bucket = event.getRecords().get(0).getS3().getBucket().getName();

		String key = event.getRecords().get(0).getS3().getObject().getKey();

		logger.debug("SendMessageHistoryHandler-- Bucket: " + bucket + " and Key: " + key);

		for (S3EventNotificationRecord record : event.getRecords()) {
			try {
				response = s3.getObject(new GetObjectRequest(bucket, key));

				InputStream objectData = response.getObjectContent();
				ObjectMapper mapper = new ObjectMapper();

				MessageDeliveryHelper helper = new MessageDeliveryHelper(
						mapper.readValue(objectData, new TypeReference<ArrayList<String>>() {
						}), System.getenv("DELIVERY_TYPE_MACSS_HISTORY"));
				response.close();

				logger.debug("input Type:" + helper.getInput2MACSS().getAwsRequest());
				logger.debug("input Count:" + helper.getInput2MACSS().getAwsRecCnt());

				logger.debug("MessageDelivery.Call2Macss");
				DLVR2XIOutput output = MessageDelivery.Call2Macss(helper.getInput2MACSS());

				if (output.getAwsResult() == "1") {
					logger.error("call 2 macss failed for History" + DateTime.now().toString());
					throw new Exception("call 2 macss failed for History");
				}

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
