package com.aep.cx.outage.alerts.aws.request.handlers;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.outage.business.OutageService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/* Used to Read Batch File via AEP ETL directed to the Landing Zone Lambda and Contains Outage Events*/
public class ProcessBatchOutages implements RequestHandler<ArrayList<Object>, String> {

	static final Logger logger = LogManager.getLogger(ProcessBatchOutages.class);

	@Override
	public String handleRequest(ArrayList<Object> inputRecList, Context context) {

		OutageService outageService = new OutageService();

		return outageService.persistBatch(inputRecList);
	}
}