package com.aep.cx.billing.alerts.lambda.handlers;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoRule;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.aep.cx.billing.alerts.business.PaymentService;
import com.aep.cx.billing.alerts.lambda.handlers.ProcessPayments;
import com.aep.cx.billing.events.Payment;
// import com.aep.cx.preferences.dao.BillingDAOTest;
import com.aep.cx.preferences.dao.BillingPreferencesDAO;
import com.aep.cx.preferences.dao.CustomerChannel;
import com.aep.cx.preferences.dao.CustomerContacts;
import com.aep.cx.preferences.dao.CustomerInfo;
import com.aep.cx.preferences.dao.CustomerPreference;
import com.aep.cx.utils.enums.PreferencesTypes;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
//@RunWith(MockitoJUnitRunner.class)
public class ProcessPaymentsTest {

    private final String CONTENT_TYPE = "image/jpeg";
    private S3Event event;

    @Mock
    private AmazonS3 s3Client;
    @Mock
    private S3Object s3Object;

    @Captor
    private ArgumentCaptor<GetObjectRequest> getObjectRequest;

    //@Before
    public void setUp() throws IOException {
        event = null; //TestUtils.parse("/s3-event.put.json", S3Event.class);

        // TODO: customize your mock logic for s3 client
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(CONTENT_TYPE);
        when(s3Object.getObjectMetadata()).thenReturn(objectMetadata);
        when(s3Client.getObject(getObjectRequest.capture())).thenReturn(s3Object);
    }

    private Context createContext() {
        //TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        //ctx.setFunctionName("Your Function Name");

        return null;
    }

    //@Test
    public void testProcessPayments() {
        ProcessPayments handler = new ProcessPayments(s3Client);
        Context ctx = createContext();

        String output = handler.handleRequest(event, ctx);

        // TODO: validate output here if needed.
        Assert.assertEquals(CONTENT_TYPE, output);
    }

 //      @Test
       public void TestPayments() {
        ArrayList<Payment> paylist = new ArrayList<Payment>();
        Payment pay = new Payment();
        pay.setAccountNumber("accountNumber");
        
        //pay.setregio
        paylist.add(pay);
        
        PaymentService.BuildPaymentContent(paylist, "45");


        // TODO: validate output here if needed.
        //Assert.assertEquals(CONTENT_TYPE, output);
    }
}
