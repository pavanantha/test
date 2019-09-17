package com.aep.cx.outage.alerts.aws.request.handlers;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.outage.business.OutageService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;

public class ProcessOutages implements RequestHandler<S3Event, String> {

	final Logger logger = LogManager.getLogger(ProcessOutages.class);

	@Override
	public String handleRequest(S3Event input, Context context) {

		ArrayList<OutageEvent> outageBatch = null;
		OutageService outageService = new OutageService();

		if (input == null || input.getRecords() == null) {
			logger.error("***** S3Event is Null ******");
			return "FAILED";
		}

		logger.debug("***** Parse Batch ******");
		outageBatch = outageService.parseBatch(input);

		logger.debug("***** Batch has been Parsed ****** Events Size " + outageBatch.size());

		// return outageService.processBatch(outageBatch,
		// input.getRecords().get(0).getS3().getObject().getKey());
		return outageService.createSmallBatchesToProcessOutageEvents(outageBatch,
				input.getRecords().get(0).getS3().getObject().getKey(),
				Integer.parseInt(System.getenv("PARTIAL_BATCH_SIZE")));
	}
}