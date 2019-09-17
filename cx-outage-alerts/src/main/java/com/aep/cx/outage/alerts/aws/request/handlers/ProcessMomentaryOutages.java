package com.aep.cx.outage.alerts.aws.request.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.outage.business.OutageService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

public class ProcessMomentaryOutages implements RequestHandler<ScheduledEvent, String> {

	final Logger logger = LogManager.getLogger(ProcessMomentaryOutages.class);

	@Override
	public String handleRequest(ScheduledEvent input, Context context) {

		logger.debug("***** Entering Momentary Outages Handler ******");
		OutageService outageService = new OutageService();

		int sizeOfBatch = 2000;
		String bucketNameToFetchFrom = System.getenv("OUTAGE_MOMENTARY_BUCKET");

		/* return outageService.retrieveAsBatch(bucketNameToFetchFrom, sizeOfBatch); */

		return outageService.processMomentaryEvents();
	}

}
