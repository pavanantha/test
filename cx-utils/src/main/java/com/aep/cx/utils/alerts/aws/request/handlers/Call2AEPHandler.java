package com.aep.cx.utils.alerts.aws.request.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.aep.cx.utils.delivery.MessageDelivery;
import com.aep.cx.utils.delivery.MessageDeliveryHelper;
import com.aep.cx.utils.enums.DeliveryType;
import com.aep.cx.utils.macss.services.DLVR2XIOutput;
import com.aep.cx.utils.thirdparty.SMSDelivery;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;



public class Call2AEPHandler {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*
		 * ArrayList<String> list = new ArrayList<String>();
		 * list.add("16145981329||venkytest1~~"); list.add("16149499232||venkytest2~~");
		 * list.add("16463003672||venkytest3~~"); MessageDeliveryHelper dh = new
		 * MessageDeliveryHelper(list ,"TEXT-DELIVERY"); DLVR2XIOutput output =
		 * MessageDelivery.Call2Macss(dh.getInput2MACSS());
		 */
		
		String prefix = "2019-Jul-29";
		String key = "email_193122161";
		ObjectMapper mapper = new ObjectMapper();

		try {
			ProfileCredentialsProvider profile = new ProfileCredentialsProvider("customer_dev");
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(profile).build();
			InputStream is = LoadData2S3.getObject("dev-alerts-outage-email-content-e1", prefix, key, s3Client);
			ArrayList<String> payList = mapper.readValue(is,new TypeReference<ArrayList<String>>() {});
			MessageDeliveryHelper dh = new MessageDeliveryHelper(payList ,DeliveryType.EMAIL_DELIVERY.getRealName());
			DLVR2XIOutput output = MessageDelivery.Call2Macss(dh.getInput2MACSS());
			//SMSDelivery.CallI2SMS(payList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}