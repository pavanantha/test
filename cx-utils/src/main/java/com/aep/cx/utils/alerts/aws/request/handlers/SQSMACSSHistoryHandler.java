package com.aep.cx.utils.alerts.aws.request.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.aep.cx.utils.delivery.MessageDelivery;
import com.aep.cx.utils.delivery.MessageDeliveryHelper;
import com.aep.cx.utils.macss.services.DLVR2XIOutput;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SQSMACSSHistoryHandler implements RequestHandler<SQSEvent, String> {

	static final Logger logger = LogManager.getLogger(SQSMACSSHistoryHandler.class);
    @Override
    public String handleRequest(SQSEvent event, Context context) {
        //context.getLogger().log("Input: " + event);
        
    	ArrayList<String> listOE = new ArrayList<String>();
        ObjectMapper mapper = new ObjectMapper();
		
    	try {
			for(SQSMessage msg : event.getRecords()){
				String message = msg.getBody();
				listOE.add(message);
			    System.out.println("message from sqs :" + message);
			}
			
			MessageDeliveryHelper helper = new MessageDeliveryHelper(listOE, System.getenv("DELIVERY_TYPE_MACSS_HISTORY"));

			logger.debug("input Type:" + helper.getInput2MACSS().getAwsRequest());
			logger.debug("input Count:" + helper.getInput2MACSS().getAwsRecCnt());

			logger.debug("MessageDelivery.Call2Macss");
			DLVR2XIOutput output = MessageDelivery.Call2Macss(helper.getInput2MACSS());

			if (output.getAwsResult() == "1") {
				logger.error("call 2 macss failed for History" + DateTime.now().toString());
				throw new Exception("call 2 macss failed for History");
			}

		} catch (JsonParseException e) {
			e.printStackTrace();
			return "error Delivering from History Queue!" + e.getMessage();
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return "error Delivering from History Queue!"+ e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return "error Delivering from History Queue!"+ e.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			return "error Delivering from History Queue!"+ e.getMessage();
		}

        return "Successfully Delivered from SMS Queue!";
    }
}
