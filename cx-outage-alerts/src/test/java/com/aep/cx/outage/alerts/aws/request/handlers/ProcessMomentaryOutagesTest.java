package com.aep.cx.outage.alerts.aws.request.handlers;

import java.io.IOException;


//junit 4
// import org.junit.Assert;
// import org.junit.BeforeClass;
// import org.junit.Test;
//junit 5
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.Context;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class ProcessMomentaryOutagesTest {

    private static Object input;

    @BeforeAll
    public static void createInput() throws IOException {
        // TODO: set up your sample input object here.
        input = null;
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

   /* @Test
    public void testProcessMomentaryOutages() {
        ProcessMomentaryOutages handler = new ProcessMomentaryOutages();
        Context ctx = createContext();

        String output = handler.handleRequest(input, ctx);

        // TODO: validate output here if needed.
        Assert.assertEquals("Hello from Lambda!", output);
    }*/
}
