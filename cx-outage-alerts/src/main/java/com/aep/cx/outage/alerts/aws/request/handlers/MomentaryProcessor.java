package com.aep.cx.outage.alerts.aws.request.handlers;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.outage.business.OutageService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class MomentaryProcessor implements RequestHandler<ArrayList<String>, String> {
	
	final Logger logger = LogManager.getLogger(MomentaryProcessor.class);

    @Override
    public String handleRequest(ArrayList<String> recordKeys, Context context) {
        
        OutageService os = new OutageService();
        os.processMomentaryEvents(recordKeys);
        logger.info("Momentary Invoked for Size = " + recordKeys.size());
        return "Momentary Invoked for Size = " + recordKeys.size();
    }

}
