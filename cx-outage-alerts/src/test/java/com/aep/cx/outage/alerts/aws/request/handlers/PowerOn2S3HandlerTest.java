package com.aep.cx.outage.alerts.aws.request.handlers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/*import javax.ws.rs.core.Response;*/
//junit 4
// import org.junit.Assert;
// import org.junit.BeforeClass;
// import org.junit.Test;
//junit 5
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//import com.aep.cx.outage.alerts.data.PowerOnData;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Unit test for simple App.
 */
public class PowerOn2S3HandlerTest
{
    /*
	private static ArrayList<PowerOnData> input;
	
       @BeforeClass
       public static void createInput() throws IOException {
           // set up your sample input object here.
           input = null;
       }

       private Context createContext() {
    	   
    	   TestContext ctx = new TestContext();

    	   //Context ctx = createContext();
           ctx.setFunctionName("Your Function Name");

           return ctx;
       }
       
       @Test
       public void powerOnRequestHandlerTest() throws JsonParseException, JsonMappingException, IOException {
			String json = "{\"outageETR\":\"201904011112\",\"outageNumber\":\"99999999\"}";

			ArrayList<Object> testObj  = new ArrayList<>();
			testObj.add(json);
			testObj.add(json);
			   
			LoadOutageData2S3Handler s3handler = new LoadOutageData2S3Handler();
			Context ctx = createContext();
			Assert.assertEquals("SUCCESS",  s3handler.handleRequest(testObj, ctx));
       }
       */
}
