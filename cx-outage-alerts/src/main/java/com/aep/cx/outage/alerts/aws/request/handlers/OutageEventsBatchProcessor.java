package com.aep.cx.outage.alerts.aws.request.handlers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.outage.business.OutageManager;
import com.aep.cx.outage.business.ProcessOutageStatus;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class OutageEventsBatchProcessor implements RequestHandler<ArrayList<Object>, String> {

	private AmazonS3 s3;
	final ObjectMapper mapper;

	final Logger logger = LogManager.getLogger(OutageEventsBatchProcessor.class);

	public OutageEventsBatchProcessor() {
    	this(AmazonS3ClientBuilder.standard().build(), new ObjectMapper());
    }

	// Test purpose only.
	OutageEventsBatchProcessor(AmazonS3 s3, ObjectMapper mapper) {
		this.s3 = s3;
		this.mapper = mapper;
		this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	@Override
	public String handleRequest(ArrayList<Object> events, Context context) {
		ProcessOutageStatus po = new ProcessOutageStatus();
		ArrayList<OutageEvent> outageEvents = new ArrayList<OutageEvent>();

		try {
			byte[] bytes = mapper.writeValueAsBytes(events);
			InputStream is = new ByteArrayInputStream(bytes);
			outageEvents = mapper.readValue(is, new TypeReference<ArrayList<OutageEvent>>() {
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		po.processOutageEventsPartialBatch(outageEvents, false);
		return "Success";
		// OutageService outageService = new OutageService();
		// return outageService.processOutageEventsPartialBatch(events);
	}
}
