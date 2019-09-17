package com.aep.cx.billing.alerts.lambda.handlers;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aep.cx.billing.alerts.business.OrderTrackingService;
import com.aep.cx.billing.events.BillingAlerts;
import com.aep.cx.billing.events.OrderTracking;
import com.aep.cx.preferences.dao.BillingPreferencesDAO;
import com.aep.cx.preferences.dao.CustomerContacts;
import com.aep.cx.preferences.dao.CustomerInfo;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.utils.enums.PreferencesTypes;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class ProcessOrderTrackingTest {

    private S3Event s3Event;

    @Mock
    private AmazonS3 s3Client;
    
    @Mock
    private S3Object s3Object;

    @Mock
    private OrderTrackingService service;
    
    @InjectMocks
    ProcessDisconnected handler;
    
    @BeforeEach
    public void setUp() throws IOException {
    	 MockitoAnnotations.initMocks(this);

         ObjectMetadata objectMetadata = new ObjectMetadata();
         
         when(s3Object.getObjectMetadata()).thenReturn(objectMetadata);
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Disabled("Error during build -- The bucket name parameter must be specified when uploading an object")
    @Test
    public void testProcessOrderTracking() {

    	String MQMessage = "MXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX0XXXXXXXXXXXXXEXPALERTINSP-COMPLETE   |94234567890|123456789|NI01|2019-08-02|EDIR2019-08-102019-08-15S007063XXXXXXXXX~CURR2019-08-102019-08-15S007063XXXXXXXXX~~~~|ISTA2019-08-102019-08-15S007063XXXXXXXXX~ICNT2019-08-102019-08-15S007063XXXXXXXXX~~~~";   	
        BillingAlerts ba = new BillingAlerts(MQMessage);
        ArrayList<OrderTracking> orderList = new ArrayList<OrderTracking>();
        orderList.add(ba.orderTracking);
        service = new OrderTrackingService();
        InsertOrder(ba.getAccountNumber());
        
        String result = service.BuildOrderTrackingContent(orderList, PreferencesTypes.ORDER.toString());
        System.out.println("result="+result);
        
        DeleteOrder(ba.getAccountNumber());
    }
    
	public void InsertOrder(String billAccountNumber) {
		CustomerPreferences cp = new CustomerPreferences();
		CustomerInfo ci = new CustomerInfo();
		ci.setAccountNumber(billAccountNumber);
		ci.setCity("TestCity");
		ci.setState("TT");
		ci.setName("test Account");
		ci.setStreetAddress("T***");
		ci.setZipCode("12345");
		ArrayList<CustomerContacts> ccList = new ArrayList<CustomerContacts>();
		CustomerContacts cc = new CustomerContacts();
		cc.setEndPoint("6145981329");
		cc.setWebId("moba@aep.com");
		ccList.add(cc);
		
		cc = new CustomerContacts();
		cc.setEndPoint("6149499232");
		cc.setWebId("moba@aep.com");
		ccList.add(cc);
		
		cc = new CustomerContacts();
		cc.setEndPoint("6148051126");
		cc.setWebId("moba@aep.com");
		ccList.add(cc);
		
		cc = new CustomerContacts();
		cc.setEndPoint("moba@aep.com");
		cc.setWebId("moba@aep.com");
		ccList.add(cc);
		cp.setCustomerContacts(ccList);
		cp.setCustomerInfo(ci);
		BillingPreferencesDAO bp = new BillingPreferencesDAO();
		bp.updateCustomerPreference(cp, PreferencesTypes.ORDER.toString());
		
		ArrayList<CustomerContacts> ccList1 = new ArrayList<CustomerContacts>();
		cc = new CustomerContacts();
		cc.setEndPoint(null);
		cc.setWebId("moba@aep.com");
		ccList.add(cc);
		cp.setCustomerContacts(ccList);
		cp.setCustomerInfo(ci);
		bp.updateCustomerPreference(cp, PreferencesTypes.ORDER.toString());		
	}
	
	public void DeleteOrder(String billAccountNumber) {
		CustomerPreferences cp = new CustomerPreferences();
		CustomerInfo ci = new CustomerInfo();
		ci.setAccountNumber(billAccountNumber);
		ci.setCity("TestCity");
		ci.setState("TT");
		ci.setName("test Account");
		ci.setStreetAddress("T***");
		ci.setZipCode("12345");
		ArrayList<CustomerContacts> ccList = new ArrayList<CustomerContacts>();
		CustomerContacts cc = new CustomerContacts();
		cc.setEndPoint(null);
		cc.setWebId("moba@aep.com");
		ccList.add(cc);
		
		cp.setCustomerContacts(ccList);
		cp.setCustomerInfo(ci);
		BillingPreferencesDAO bp = new BillingPreferencesDAO();
		bp.updateCustomerPreference(cp, PreferencesTypes.ORDER.toString());	
	}
}
