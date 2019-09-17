package com.aep.cx.utils.alerts.aws.request.handlers;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.aep.cx.macss.customer.subscriptions.MACSSIntegrationWrapper;
import com.aep.cx.macss.customer.subscriptions.MACSSLayout;
import com.aep.cx.preferences.dao.BillingPreferencesDAO;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.aep.cx.utils.enums.PreferencesTypes;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@RunWith(MockitoJUnitRunner.class)
public class AllPreferennces2S3Test {

    private final String CONTENT_TYPE = "image/jpeg";
    private S3Event event;

    @Mock
    private AmazonS3 s3Client;
    @Mock
    private S3Object s3Object;

    @Captor
    private ArgumentCaptor<GetObjectRequest> getObjectRequest;


    @Test
    public void testAllPreferennces2S3() throws JsonParseException, JsonMappingException, IOException {
    	
    	BillingPreferencesDAO baDao = new BillingPreferencesDAO();
    	ObjectMapper mapper = new ObjectMapper();
    	ObjectMetadata metadata;
    	S3Object accountPreferencesObject = null;
    	ProfileCredentialsProvider profile = new ProfileCredentialsProvider("customer_dev");
    	AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(profile).build();
    	//06/preferences_0_fmt.txt
    	InputStream is =  LoadData2S3.getObject("dev-alerts-preferences-onetime-raw-e1", "06", "preferences_12_fmt.txt", s3Client);
    	
    	ArrayList<MACSSIntegrationWrapper> iw = mapper.readValue(is, new TypeReference<ArrayList<MACSSIntegrationWrapper>>() {});
    	
		for (MACSSIntegrationWrapper MQMessage : iw) {
			MACSSLayout ml = new MACSSLayout(MQMessage.getMessageString());
								
			BillingPreferencesDAO  bpDao = new BillingPreferencesDAO();					
			bpDao.updateCustomerPreference(ml.getBillPayPreferences(),PreferencesTypes.BILLINGPAYMENT.toString());
			System.out.println("Billing Preferences updated successfully : " +  ml.getBillPayPreferences().getCustomerContacts().size());
			
			if (null != ml.getPowerPayPreferences() && ml.getPowerPayPreferences().getCustomerInfo().getAccountNumber().startsWith("95")) {
				bpDao.updateCustomerPreference(ml.getPowerPayPreferences(),PreferencesTypes.POWERPAY.toString());
				System.out.println("PowerPay Preferences updated successfully ");
			}
			
			bpDao.updateCustomerPreference(ml.getOrderPreferences(),PreferencesTypes.ORDER.toString());
			System.out.println("Order Preferences updated successfully : ");
			
			/*
			 * if (null != ml.getOrderPreferences() &&
			 * (ml.getOrderPreferences().getCustomerInfo().getAccountNumber().startsWith(
			 * "94") ||
			 * ml.getOrderPreferences().getCustomerInfo().getAccountNumber().startsWith("97"
			 * ))) {
			 * 
			 * bpDao.updateCustomerPreference(ml.getOrderPreferences(),PreferencesTypes.
			 * ORDER.toString());
			 * System.out.println("Order Preferences updated successfully : "); }
			 */

		}


    }
}
