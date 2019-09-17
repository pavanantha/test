//package com.aep.cx.utils.alerts.aws.request.handlers;
//
//import static org.junit.Assert.*;
//
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.commons.io.IOUtils;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.junit.Before;
//import org.junit.Test;
//
//import com.aep.cx.preferences.dao.CustomerPreferences;
////import com.aep.cx.outage.business.OutageNotificationManagerTestText;
//import com.amazonaws.services.lambda.runtime.events.S3Event;
//import com.amazonaws.services.s3.event.S3EventNotification;
//import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//
//public class LoadPreferencesTest {
//
//	LoadPreferences2S3Handler lp2s3;
//	
//	Preferences2DynamoDBHandler p2ddb;
//		
//	static final Logger logger =
//			  LogManager.getLogger(LoadPreferencesTest.class);
//			  
//	HashMap<String, ArrayList<CustomerPreferences>> loadPreferencesTestData(String resourceName) throws IOException {
//		return mapper.readValue(IOUtils.resourceToString(resourceName, Charset.defaultCharset()), cpListType);
//
//	}
//	
//	private HashMap<String, ArrayList<CustomerPreferences>> cpList;
//
//	ObjectMapper mapper = new ObjectMapper();
//	TypeReference<Map<String, ArrayList<CustomerPreferences>>> cpListType = new TypeReference<Map<String, ArrayList<CustomerPreferences>>>() {
//	};
//	
//	@Before
//	public void setUp()
//	{
//		
//	}
//	
//	@Test
//	public void LoadPreferences() {
//		logger.info("Running Test Case Load Preferences...");
//		ArrayList<Object> testArrayList = new ArrayList<Object>();
//		
//		String result = lp2s3.handleRequest(testArrayList, null);
//		assertEquals(result, "SUCCESS");
//	}
//
//	@Test
//	public void Preferences2DynoDB()
//	{
//		logger.info("Running Test Case Preferences 2 Dyno DB...");
//		try {
//			cpList = loadPreferencesTestData("/testpreferences.json");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		//S3EventNotification notification2 = cpList.
//		S3EventNotification notification = S3EventNotification.parseJson("{\"019999998\":[{\"customerContacts\":[{\"endPoint\":\"moba@aep.com\",\"webId\":\"aaron1\"},{\"endPoint\":\"6149499232\",\"webId\":\"aaron1\"}]}]}");
//		
////		S3EventNotificationRecord rec = new S3EventNotificationRecord();
////		notification = new S3EventNotification();
//				
//		
//		
//		S3Event s3event = new S3Event(notification.getRecords());
//		
//		String result = p2ddb.handleRequest(s3event, null);
//		assertEquals(result, "SUCCESS");
//	}
//}