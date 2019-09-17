package com.aep.cx.utils.alerts.aws.request.handlers;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.ws.BindingProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.aep.cx.utils.delivery.MessageDelivery;
import com.aep.cx.utils.delivery.MessageDeliveryHelper;
import com.aep.cx.utils.enums.MessageType;
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

public class SendSMSNotificationsHandler implements RequestHandler<S3Event, String> {

	ObjectFactory factory;
	DLVR2XI request;
	DLVR2XIInput input2MACSS;
	DLVR2XIInputAwsInput awsInput;
	DLVR2XIOutput output;
	private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

	static final Logger logger = LogManager.getLogger(SendSMSNotificationsHandler.class);

	@Override
	public String handleRequest(S3Event event, Context context) {

		String bucket = event.getRecords().get(0).getS3().getBucket().getName();
		String key = event.getRecords().get(0).getS3().getObject().getKey();
		
		S3Object response = s3.getObject(new GetObjectRequest(bucket, key));
		InputStream objectData = response.getObjectContent();

		ObjectMapper mapper = new ObjectMapper();
		try {

			MessageDeliveryHelper helper = new MessageDeliveryHelper(
					mapper.readValue(objectData, new TypeReference<ArrayList<String>>() {
					}), System.getenv("DELIVERY_TYPE_SMS"));
			response.close();

			DLVR2XIOutput output = MessageDelivery.Call2Macss(helper.getInput2MACSS());

			if (output.getAwsResult() == "1") {
				logger.error("call 2 macss failed for SMS" + DateTime.now().toString());
			}
			/*
			 * smsNotificationList.forEach(message ->{ DLVR2XIInputAwsInputAwsRecords
			 * awsRecord = new DLVR2XIInputAwsInputAwsRecords();
			 * awsRecord.setAwsDeliveryText(MessageType.SMSMMS.toString() + message);
			 * awsInput.getAwsRecords().add(awsRecord); }); input2MACSS.setAwsRecCnt((short)
			 * awsInput.getAwsRecords().size());
			 */

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(String.format("Error getting object %s from bucket %s. Make sure they exist and"
					+ " your bucket is in the same region as this function.", key, bucket));
			return "FAILED";
			}
		
		return "SUCCESS";
	}
}