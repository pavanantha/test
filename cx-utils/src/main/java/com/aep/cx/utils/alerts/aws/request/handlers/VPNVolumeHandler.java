package com.aep.cx.utils.alerts.aws.request.handlers;

import javax.xml.ws.BindingProvider;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.aep.cx.utils.macss.services.DLVR2XI;
import com.aep.cx.utils.macss.services.DLVR2XIInput;
import com.aep.cx.utils.macss.services.DLVR2XIInputAwsInput;
import com.aep.cx.utils.macss.services.DLVR2XIInputAwsInputAwsRecords;
import com.aep.cx.utils.macss.services.DLVR2XIOutput;
import com.aep.cx.utils.macss.services.MCSKMQXI;
import com.aep.cx.utils.macss.services.MCSKMQXISoap;
import com.aep.cx.utils.macss.services.ObjectFactory;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

public class VPNVolumeHandler implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object input, Context context) {
        context.getLogger().log("Input: " + input);
        
        context.getLogger().log("date time triggered: " + DateTime.now().toDateTime(DateTimeZone.forID("America/New_York")));


		ObjectFactory factory = new ObjectFactory();
		DLVR2XI request = factory.createDLVR2XI();
		DLVR2XIInput input2MACSS = factory.createDLVR2XIInput();
		input2MACSS.setAwsRequest("MACSS-HISTORY");
		input2MACSS.setAwsRecCnt((short) 700);
		
		String mqMessage = "{\\\"outageNumber\\\":1111111,\\\"outageType\\\":\\\"predicted\\\",\\\"premiseNumber\\\":\\\"953456666\\\",\\\"premisePowerStatus\\\":\\\"OFF\\\",\\\"outageStatus\\\":\\\"predicted\\\",\\\"outageCreationTime\\\":\\\"201905181114\\\",\\\"outageETR\\\":\\\"201905310702\\\",\\\"outageRestorationTime\\\":\\\"201905300702\\\",\\\"outageAsOfTime\\\":\\\"201905181114\\\",\\\"outageCause\\\":\\\"accident\\\",\\\"outageSimpleCause\\\":\\\"accident\\\",\\\"outageOverideFlag\\\":\\\"n\\\",\\\"outageArea\\\":\\\"1234\\\",\\\"outageETRType\\\":\\\"g\\\",\\\"outageCustomerCount\\\":15,\\\"outageCustomerMAXCount\\\":300}{\\\"outageNumber\\\":1111111,\\\"outageType\\\":\\\"predicted\\\",\\\"premiseNumber\\\":\\\"953456666\\\",\\\"premisePowerStatus\\\":\\\"OFF\\\",\\\"outageStatus\\\":\\\"predicted\\\",\\\"outageCreationTime\\\":\\\"201905181114\\\",\\\"outageETR\\\":\\\"201905310702\\\",\\\"outageRestorationTime\\\":\\\"201905300702\\\",\\\"outageAsOfTime\\\":\\\"201905181114\\\",\\\"outageCause\\\":\\\"accident\\\",\\\"outageSimpleCause\\\":\\\"accident\\\",\\\"outageOverideFlag\\\":\\\"n\\\",\\\"outageArea\\\":\\\"1234\\\",\\\"outageETRType\\\":\\\"g\\\",\\\"outageCustomerCount\\\":15,\\\"outageCustomerMAXCount\\\":300}";

		DLVR2XIInputAwsInput awsInput = factory.createDLVR2XIInputAwsInput();
		
		//ArrayList<DLVR2XIInputAwsInputAwsRecords> recList = new ArrayList<DLVR2XIInputAwsInputAwsRecords>();
		request.setDLVR2XIInput(input2MACSS);
		input2MACSS.setAwsInput(awsInput);	
		
		DLVR2XIInputAwsInputAwsRecords awsRecord;
		
		for (int i=0;i>600;i++) {
			awsRecord = new DLVR2XIInputAwsInputAwsRecords();
			awsRecord.setAwsDeliveryText(mqMessage);
			awsInput.getAwsRecords().add(awsRecord);
		}

		//awsInput.getAwsRecords().addAll(recList);
		//awsInput.setAwsRecords(recList);
 
        MCSKMQXI sv1 = new MCSKMQXI();
		
        
       MCSKMQXISoap soap = sv1.getMCSKMQXISoaphttp();
		
    
        ((BindingProvider) soap).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "webdev");
        ((BindingProvider) soap).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "webpwd$1");
        ((BindingProvider) soap).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://co1m.aepsc.com:1211/macssws/mcskmqxi.zws");


        DLVR2XIOutput output = soap.dlvr2XI(input2MACSS);
        System.out.println("MACSS Request was ####"+output.getAwsRequest());
        System.out.println("MACSS Call Successfull####"+output.getAwsResult());
        
        
        return "hello from macss service";
    }

}
