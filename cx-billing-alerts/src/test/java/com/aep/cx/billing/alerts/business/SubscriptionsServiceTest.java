package com.aep.cx.billing.alerts.business;

import java.io.InputStream;
import java.util.ArrayList;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.aep.cx.billing.alerts.enrollment.events.EnrollmentAlerts;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SubscriptionsServiceTest {

	@Disabled("It needs to be updated to use mock data")
	@Test
	public void test() {
		try {
			String prefix = "20190803";
			String key = "enrollment_102108";
			ObjectMapper mapper = new ObjectMapper();
			ProfileCredentialsProvider profile = new ProfileCredentialsProvider("customer_dev");
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(profile).build();
			InputStream is = LoadData2S3.getObject("dev-alerts-billing-profile-e1", prefix, key, s3Client);
			ArrayList<EnrollmentAlerts> enrollList = mapper.readValue(is,new TypeReference<ArrayList<EnrollmentAlerts>>() {});
			SubscriptionsService ss = new SubscriptionsService();
			String result = ss.BuildProfileContent(enrollList,prefix+"/"+key);
			System.out.println("subs processed ="+result);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SdkClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
