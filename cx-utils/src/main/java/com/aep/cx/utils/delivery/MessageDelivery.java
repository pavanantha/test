package com.aep.cx.utils.delivery;

import java.util.HashMap;

import javax.xml.ws.BindingProvider;

import com.aep.cx.alerts.aws.services.AWSCredentials;
import com.aep.cx.utils.macss.services.DLVR2XI;
import com.aep.cx.utils.macss.services.DLVR2XIInput;
import com.aep.cx.utils.macss.services.DLVR2XIInputAwsInput;
import com.aep.cx.utils.macss.services.DLVR2XIInputAwsInputAwsRecords;
import com.aep.cx.utils.macss.services.DLVR2XIOutput;
import com.aep.cx.utils.macss.services.MCSKMQXI;
import com.aep.cx.utils.macss.services.MCSKMQXISoap;
import com.aep.cx.utils.macss.services.ObjectFactory;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

public class MessageDelivery {
    public static DLVR2XIOutput Call2Macss(DLVR2XIInput input2MACSS) {

    	HashMap<String, String> secrets = AWSCredentials.getSecret("alerts/shadow", "us-east-1");
        ObjectFactory factory = new ObjectFactory();
        DLVR2XI request = factory.createDLVR2XI();

        MCSKMQXI sv1 = new MCSKMQXI();

        MCSKMQXISoap soap = sv1.getMCSKMQXISoaphttp();

        ((BindingProvider) soap).getRequestContext().put(BindingProvider.USERNAME_PROPERTY,
                secrets.get("ShadowUser"));
        ((BindingProvider) soap).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY,
        		secrets.get("ShadowPass"));
        ((BindingProvider) soap).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
        		secrets.get("ShadowUrl"));

        DLVR2XIOutput output = soap.dlvr2XI(input2MACSS);
        System.out.println("Suceessfull handling of MACSS Call="+output.getAwsResult());
        System.out.println("Suceessfull handling of Call count="+output.getAwsRecCnt());
        return output;
    }

}
