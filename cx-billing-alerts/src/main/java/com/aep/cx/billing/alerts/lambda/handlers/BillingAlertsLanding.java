package com.aep.cx.billing.alerts.lambda.handlers;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.aep.cx.load.billing.alerts.service.Load2S3BillingService;
import com.aep.cx.macss.customer.subscriptions.MACSSIntegrationWrapper;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

//api to landing zone

public class BillingAlertsLanding implements RequestHandler<Map<String, Object>, String> {
	
	final Logger logger = LogManager.getLogger(BillingAlertsLanding.class);

	@Override
	public String handleRequest(Map<String, Object> input, Context context) {
		context.getLogger().log("Input: " + input);
		String headerName = null;
		ArrayList<MACSSIntegrationWrapper> macssIntegrationDataList=null;
		String bucketName = System.getenv("ALERTS_RAW_BILLING");

		try {
			logger.info("Actual Message: " + input.get("body-json"));
			context.getLogger().log("Actual Message: " + input.get("body-json"));

			ObjectMapper mapper = new ObjectMapper();
			macssIntegrationDataList = new ArrayList<MACSSIntegrationWrapper>();

			Map<String,Object> bodyJson = (Map<String, Object>) (input.get("body-json"));
			for (Entry<String, Object> entry : bodyJson.entrySet())  {
	            System.out.println("Key = " + entry.getKey() + 
	                             ", Value = " + entry.getValue()); 
	            ArrayList<Map<String, String>> mapData = (ArrayList<Map<String, String>>) entry.getValue();
	            for (Entry<String, String> entry1 : mapData.get(0).entrySet()) {
	            	MACSSIntegrationWrapper iw = new MACSSIntegrationWrapper();
	            	iw.setMessageString(entry1.getValue());
	            	macssIntegrationDataList.add(iw);
	            }
			}
			
			Map<String, String> headers = ((Map<String, Map<String, String>>) input.get("params")).get("header");

			headerName = headers.get("RequestQueue");
			Load2S3BillingService.Load2s3byAlertHeader(macssIntegrationDataList, headerName);
		}

		catch (Exception e) {
			e.printStackTrace();
			DateTime dateTime = DateTime.now().toDateTime(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/New_York")));
			String bucketKey = dateTime.toString("yyyyMMdd")+ "/"+headerName + "_errored_" + dateTime.toString("HHmmss");
			LoadData2S3 load2s3 = new LoadData2S3();
			load2s3.loadData(bucketName, bucketKey, macssIntegrationDataList);
			System.out.println("stored error file :"+bucketKey);
			return "Lambda Failed:" + context.getFunctionName();
		}

		return "Lambda Succeeded:" + context.getFunctionName();
	}
}
