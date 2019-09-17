package com.aep.cx.utils.delivery;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aep.cx.utils.alerts.aws.request.handlers.SendSMSNotificationsHandler;
import com.aep.cx.utils.enums.DeliveryType;
import com.aep.cx.utils.enums.MessageType;
import com.aep.cx.utils.macss.services.DLVR2XI;
import com.aep.cx.utils.macss.services.DLVR2XIInput;
import com.aep.cx.utils.macss.services.DLVR2XIInputAwsInput;
import com.aep.cx.utils.macss.services.DLVR2XIInputAwsInputAwsRecords;
import com.aep.cx.utils.macss.services.ObjectFactory;

public class MessageDeliveryHelper {
	ObjectFactory factory;
	DLVR2XI request;
	DLVR2XIInput input2MACSS;
	DLVR2XIInputAwsInput awsInput;
	
	static final Logger logger = LogManager.getLogger(MessageDeliveryHelper.class);

	public MessageDeliveryHelper(ArrayList<String> messageList, String deliveryType) {
		factory = new ObjectFactory();
		request = factory.createDLVR2XI();
		input2MACSS = factory.createDLVR2XIInput();
		input2MACSS.setAwsRequest(deliveryType);
		input2MACSS.setAwsRecCnt((short) messageList.size());
		DLVR2XIInputAwsInput awsInput = factory.createDLVR2XIInputAwsInput();
		request.setDLVR2XIInput(input2MACSS);
		input2MACSS.setAwsInput(awsInput);
		messageList.forEach(message -> {
			DLVR2XIInputAwsInputAwsRecords awsRecord = new DLVR2XIInputAwsInputAwsRecords();
			if (deliveryType.contentEquals(DeliveryType.TEXT_DELIVERY.getRealName())) {
				Boolean prefix = this.messageContains(message.substring(0, 6));
				awsRecord.setAwsDeliveryText(MessageType.SMSMMS.toString()+message);
			}
			else {
				if (deliveryType.contentEquals(DeliveryType.EMAIL_DELIVERY.getRealName())) {
					Boolean prefix = this.messageContains(message.substring(0, 6));
					if (prefix) {
						awsRecord.setAwsDeliveryText(message);
					} 
					else {
						awsRecord.setAwsDeliveryText(MessageType.XATFTP.toString()+message);
					}
				}
				else {
					awsRecord.setAwsDeliveryText(message);
				}
			}
			logger.debug("History Message:- " + awsRecord.getAwsDeliveryText());
			awsInput.getAwsRecords().add(awsRecord);
		});
		logger.debug("Message History List size: " + awsInput.getAwsRecords().size());
		input2MACSS.setAwsRecCnt((short) awsInput.getAwsRecords().size());
	}

	public MessageDeliveryHelper(String deliveryType) {
		factory = new ObjectFactory();
		request = factory.createDLVR2XI();
		input2MACSS = factory.createDLVR2XIInput();
		input2MACSS.setAwsRequest(deliveryType);
		DLVR2XIInputAwsInput awsInput = factory.createDLVR2XIInputAwsInput();
		request.setDLVR2XIInput(input2MACSS);
		input2MACSS.setAwsInput(awsInput);
	}

	public DLVR2XIInput DeliverHistory(ArrayList<String> messageList) {
		messageList.forEach(message -> {
			DLVR2XIInputAwsInputAwsRecords awsRecord = new DLVR2XIInputAwsInputAwsRecords();
			awsRecord.setAwsDeliveryText(message);
			awsInput.getAwsRecords().add(awsRecord);
		});
		input2MACSS.setAwsRecCnt((short) awsInput.getAwsRecords().size());
		return input2MACSS;
	}

	public DLVR2XIInput DeliverEmail(ArrayList<String> messageList) {
		messageList.forEach(message -> {
			DLVR2XIInputAwsInputAwsRecords awsRecord = new DLVR2XIInputAwsInputAwsRecords();
			awsRecord.setAwsDeliveryText(MessageType.XATFTP.toString() + message);
			awsInput.getAwsRecords().add(awsRecord);
		});
		input2MACSS.setAwsRecCnt((short) awsInput.getAwsRecords().size());
		return input2MACSS;
	}

	public DLVR2XIInput DeliverSMS(ArrayList<String> messageList) {
		messageList.forEach(message -> {
			DLVR2XIInputAwsInputAwsRecords awsRecord = new DLVR2XIInputAwsInputAwsRecords();
			awsRecord.setAwsDeliveryText(MessageType.SMSMMS.toString() + message);
			awsInput.getAwsRecords().add(awsRecord);
		});
		input2MACSS.setAwsRecCnt((short) awsInput.getAwsRecords().size());
		return input2MACSS;
	}

	public DLVR2XIInput getInput2MACSS() {
		return input2MACSS;
	}

	public void setInput2MACSS(DLVR2XIInput input2macss) {
		input2MACSS = input2macss;
	}
	
	public static boolean messageContains(String type) {

	    for (MessageType c : MessageType.values()) {
	        if (c.name().equals(type)) {
	            return true;
	        }
	    }

	    return false;
	}
}
