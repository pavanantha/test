package com.aep.cx.outage.alerts.aws.request.handlers;

//import static org.mockito.Mockito.when;

import java.io.IOException;

//junit 4
// import org.junit.Assert;
// import org.junit.Before;
// import org.junit.Test;
// import org.junit.runner.RunWith;
//junit 5
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;*/

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
//@RunWith(MockitoJUnitRunner.class)
public class ProcessSustainedOutagesTest {

    private final String CONTENT_TYPE = "image/jpeg";
    private S3Event event;

   /* @Mock
    private AmazonS3 s3Client;
    @Mock
    private S3Object s3Object;

    @Captor
    private ArgumentCaptor<GetObjectRequest> getObjectRequest;

    @BeforeEach
    public void setUp() throws IOException {
        event = TestUtils.parse("/s3-event.put.json", S3Event.class);

        // TODO: customize your mock logic for s3 client
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(CONTENT_TYPE);
        when(s3Object.getObjectMetadata()).thenReturn(objectMetadata);
        when(s3Client.getObject(getObjectRequest.capture())).thenReturn(s3Object);
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testProcessSustainedOutages() {
        ProcessSustainedOutages handler = new ProcessSustainedOutages(s3Client);
        Context ctx = createContext();

        String output = handler.handleRequest(event, ctx);

        // TODO: validate output here if needed.
        Assert.assertEquals(CONTENT_TYPE, output);
    }*/
}
