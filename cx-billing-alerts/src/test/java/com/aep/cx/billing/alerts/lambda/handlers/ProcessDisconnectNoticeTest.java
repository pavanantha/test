package com.aep.cx.billing.alerts.lambda.handlers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aep.cx.billing.alerts.business.BillingAlertsService;
import com.aep.cx.billing.events.DisconnectNotice;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */

public class ProcessDisconnectNoticeTest {

    private S3Event s3Event;

    @Mock
    private AmazonS3 s3Client;
    @Mock
    private S3Object s3Object;
    @Mock
    private BillingAlertsService service;
    
    @InjectMocks
    ProcessDisconnectNotice handler;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        
        when(s3Object.getObjectMetadata()).thenReturn(objectMetadata);
        
    }

    private Context createContext() {
        TestContext ctx = new TestContext();
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testProcessDisconnectNotice() {
        Context ctx = createContext();
        
        s3Event =  mock(S3Event.class);
        
        ArrayList<DisconnectNotice> discNoticeList = new ArrayList<>();
        
        when(service.parseS3EventData(s3Event, new DisconnectNotice())).thenReturn(discNoticeList);
        when(service.buildAlertContent(discNoticeList)).thenReturn("Success");

        String output = handler.handleRequest(s3Event, ctx);
        assertEquals("Success", output);
    }
}
