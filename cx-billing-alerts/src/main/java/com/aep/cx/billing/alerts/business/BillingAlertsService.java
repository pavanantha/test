package com.aep.cx.billing.alerts.business;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.aep.cx.billing.events.DisconnectNotice;
import com.aep.cx.billing.events.Disconnected;
import com.aep.cx.billing.events.Header;
import com.aep.cx.billing.events.Reconnected;
import com.aep.cx.preferences.dao.BillingPreferencesDAO;
import com.aep.cx.preferences.dao.CustomerContacts;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.aep.cx.utils.alerts.notification.msessages.BuildMessageHistory;
import com.aep.cx.utils.alerts.notification.msessages.EmailDeliveryHeader;
import com.aep.cx.utils.enums.PreferencesTypes;
import com.aep.cx.utils.opco.OperatingCompanyManager;
import com.aep.cx.utils.opco.OperatingCompanyV2;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class BillingAlertsService {

	AmazonS3 s3Client;
	final ObjectMapper mapper;
	CustomerPreferences prefs = null;
	BillingPreferencesDAO dao = null;
	String s3Key = null;
	final double DISCONNECT_AMOUNT_THRESHOLD = 19.99;

	public BillingAlertsService() {
		this(AmazonS3ClientBuilder.standard().build(), new ObjectMapper(), new BillingPreferencesDAO());
	}

	public BillingAlertsService(AmazonS3 s3Client, ObjectMapper mapper, BillingPreferencesDAO dao) {
		this.s3Client = s3Client;
		this.mapper = mapper;
		this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		this.dao = dao;
	}

	final Logger logger = LogManager.getLogger(BillingAlertsService.class);

	public <T extends Header> ArrayList<T> parseS3EventData(S3Event input, T type) {
		logger.debug("Entering  parseS3EventData ");
		for (S3EventNotificationRecord record : input.getRecords()) {

			String s3Bucket = record.getS3().getBucket().getName();

			s3Key = record.getS3().getObject().getKey();

			logger.debug("Bucket:- " + s3Bucket + " and Key:- " + s3Key);

			S3Object object = s3Client.getObject(new GetObjectRequest(s3Bucket, s3Key));

			S3ObjectInputStream objectData = object.getObjectContent();

			try {
				if (objectData != null) {
					ArrayList<T> typeList = null;
					if(type instanceof Disconnected) {
						typeList = mapper.readValue(objectData,
								new TypeReference<ArrayList<Disconnected>>() {
						});
					}
					else if(type instanceof DisconnectNotice) {
						typeList = mapper.readValue(objectData,
								new TypeReference<ArrayList<DisconnectNotice>>() {
						});
					}else if(type instanceof Reconnected) {
						typeList = mapper.readValue(objectData,
								new TypeReference<ArrayList<Reconnected>>() {
						});
					}

					logger.debug("parsing " +  type.getClass().getName() + " -- Number of Records : " + typeList.size());
					return typeList;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public <T extends Header> String buildAlertContent(ArrayList<T> itemList) {
		try {
			for (T obj : itemList) {
				obj.setOpcoAbbreviatedName(getOpcoAbbreviatedName(obj));
				prefs = dao.getCustomerPreferences(obj.getAccountNumber(), PreferencesTypes.BILLINGPAYMENT.toString());

				if (null != prefs && null != prefs.getCustomerContacts() && prefs.getCustomerContacts().size() > 0) {
					buildEmail(obj, prefs);
					buildSMS(obj, prefs);
				}
			}
			return "Successfully processed : build content and stored into per account bucket = " + s3Key;
		} catch (Exception e) {
			return "Failed processing : content build for payment = " + s3Key;
		}
	}

	public <T extends Header> HashMap<String, ArrayList<String>> buildEmail(T objType, CustomerPreferences prefs) {

		HashMap<String, String> emailTemplate = getEmailTemplate(objType);
		HashMap<String, ArrayList<String>> emailDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> emailList = new ArrayList<String>();
		ArrayList<String> historyList = new ArrayList<String>();

		OperatingCompanyManager cm = new OperatingCompanyManager();
		Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
		OperatingCompanyV2 opcoDetails = opcoBuild.get(objType.getAccountNumber().substring(0, 2));

		EmailDeliveryHeader emailHeader = new EmailDeliveryHeader();
		
		if(!isThresholdAmountExcededToNotifyCustomer(objType))
			return null;

		emailHeader.setAccountNumber(objType.getAccountNumber());
		emailHeader.setAccountNickname("");
		emailHeader.setChannelMemberID(opcoDetails.getChannelID());
		emailHeader.setCity(prefs.getCustomerInfo().getCity());
		emailHeader.setStreetAddress1(prefs.getCustomerInfo().getStreetAddress() + "***");
		emailHeader.setESID(objType.getEzid());
		emailHeader.setFirstName(prefs.getCustomerInfo().getName());
		emailHeader.setLearnMoreLink(opcoDetails.getOpcoSite() + opcoDetails.getLearnMoreLinkUrl());
		emailHeader.setPreheader(emailTemplate.get("Subject").replaceAll("\\*OPCOSN", opcoDetails.getAbbreviatedName())
				.replaceAll("%26", "&"));
		emailHeader.setState(prefs.getCustomerInfo().getState());
		emailHeader.setStreetAddress2("");
		emailHeader.setSubject(emailHeader.getPreheader());
		emailHeader.setTemplate(emailTemplate.get("Template"));
		emailHeader.setZipCode(prefs.getCustomerInfo().getZipCode());

		for (CustomerContacts cc : prefs.getCustomerContacts()) {
			if (cc.getEndPoint().contains("@")) {
				emailHeader.setHashLink(UUID.randomUUID().toString().replaceAll("-", ""));
				emailHeader.setSubscriberKey(emailHeader.getHashLink());
				emailHeader.setEmailAddress(cc.getEndPoint());
				emailList.add(
						objType.getExternalID() + emailHeader.toString() + "," + objType.getEmailContent());

				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(objType.getAccountNumber());
				bm.setAlertName(objType.getAlertType());
				bm.setEndPoint(cc.getEndPoint());
				bm.setWebID(cc.getWebId());
				bm.setOutageNumber(0);
				bm.setEmailTemplate(emailHeader.getTemplate());
				bm.setEmailSubject(emailHeader.getSubject());
				bm.setMessageTrackId(emailHeader.getHashLink());
				bm.setEmailMetaData("Template:" + bm.getEmailTemplate() + objType.getMacssEmailContent());
				historyList.add(bm.getMacssBuild());
			}
		}
		if (emailList.size() > 0) {
			String bucketKey = "email_" + objType.getAccountNumber() + "_"
					+ DateTime.now().toString("yyyyMMddHHmmssSSS");
			emailDetails.put("xat", emailList);
			emailDetails.put("history", historyList);
			loadNotificationAlerts2S3(System.getenv("NOTIFICATION_EMAIL_BUCKET"), bucketKey, emailList,
					System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), historyList);
		}
		return emailDetails;
	}

	/**
	 * @param objType
	 */
	private <T extends Header> boolean isThresholdAmountExcededToNotifyCustomer(T objType) {
		double disconnectedAmount = 0;
		if (objType instanceof Disconnected) {
			disconnectedAmount = ((Disconnected) objType).getDisconnectedAmount();
		} else if (objType instanceof DisconnectNotice) {
			disconnectedAmount = ((DisconnectNotice) objType).getDisconnectAmount();
		}else {
			return true;
		}
		
		if ( disconnectedAmount < DISCONNECT_AMOUNT_THRESHOLD )
		{
			return false;
		}
		return true;
	}

	public <T extends Header> HashMap<String, ArrayList<String>> buildSMS(T objType, CustomerPreferences prefs) {

		HashMap<String, ArrayList<String>> smsDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> smsList = new ArrayList<String>();
		ArrayList<String> historyList = new ArrayList<String>();
		
		if(null == objType.getOpcoAbbreviatedName()) {
			objType.setOpcoAbbreviatedName(getOpcoAbbreviatedName(objType));
		}

		OperatingCompanyManager cm = new OperatingCompanyManager();
		Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();

		NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

		OperatingCompanyV2 opcoDetails = opcoBuild.get(objType.getAccountNumber().substring(0, 2));
		HashMap<String, String> smsTemplate = getSMSTemplate(objType);
		
		if(!isThresholdAmountExcededToNotifyCustomer(objType))
			return null;

		double disconnectedAmount = 0;
		if (objType instanceof Disconnected) {
			disconnectedAmount = ((Disconnected) objType).getDisconnectedAmount();
		} else if (objType instanceof DisconnectNotice) {
			disconnectedAmount = ((DisconnectNotice) objType).getDisconnectAmount();
		}

		String smsText = smsTemplate.get("SMSText").replaceAll("\\*OPCOSN", objType.getOpcoAbbreviatedName())
				.replaceAll("\\*ACCOUNTNUMBER", objType.getAccountNumber().substring(6, 6 + 5))
				.replaceAll("\\*DISCAMOUNT", "\\" + nf.format(disconnectedAmount))
				.replaceAll("\\*OPCOURL", opcoDetails.getOpcoSite());
		
		if (objType instanceof DisconnectNotice) {
			smsText = smsText.replaceAll("\\*DUEDATE",
					((DisconnectNotice) objType).getBillDueDate().toString(DateTimeFormat.forPattern("M/d/yyyy")));
		}else if(objType instanceof Reconnected) {
			smsText = smsText.replaceAll("\\*RECONNECTDATE", ((Reconnected) objType).getReconnectDateAndTime().toString(DateTimeFormat.forPattern("M/d/yyyy")));
		}

		for (CustomerContacts cc : prefs.getCustomerContacts()) {
			if (!cc.getEndPoint().contains("@") && cc.getEndPoint().length() > 0) {

				smsText = smsText.replaceAll("\\*ADDRESS", prefs.getCustomerInfo().getStreetAddress() + "***");
				smsList.add("1" + cc.getEndPoint() + "||" + smsText + "~~");
				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(objType.getAccountNumber());
				bm.setAlertName(objType.getAlertType());
				bm.setEndPoint(cc.getEndPoint());
				bm.setWebID(cc.getWebId());
				bm.setOutageNumber(0);
				bm.setMessageTrackId(UUID.randomUUID().toString().replaceAll("-", ""));
				bm.setSmsText(smsText.replaceAll("%26", "&"));
				historyList.add(bm.getMacssBuild());
			}
		}

		if (smsList.size() > 0) {
			String bucketKey = "sms_" + objType.getAccountNumber() + "_" + DateTime.now().toString("yyyyMMddHHmmssSSS");
			loadNotificationAlerts2S3(System.getenv("NOTIFICATION_SMS_BUCKET"), bucketKey, smsList,
					System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), historyList);

			smsDetails.put("sms", smsList);
			smsDetails.put("history", historyList);
		}
		return smsDetails;
	}

	public void loadNotificationAlerts2S3(String bucketName, String bucketKey, ArrayList<String> alertsList,
			String historyBucketName, ArrayList<String> historyList) {
		LoadData2S3 load2S3 = new LoadData2S3();
		load2S3.loadData(bucketName, bucketKey, alertsList);
		load2S3.loadData(historyBucketName, bucketKey, historyList);
	}

	public <T extends Header> HashMap<String, String> getEmailTemplate(T type) {
		HashMap<String, String> emailTemplate = new HashMap<String, String>();
		String subject = "";
		String template = "";
		
		if (type instanceof Disconnected) {
			template = "HasDisconnected";
			subject = "*OPCOSN: Your Power is disconnected for Non Payment.";
		}else if(type instanceof DisconnectNotice) {
			template = "DisconnectApproaching";
			subject = "Disconnection of Electric Service is Scheduled.";
		}else if(type instanceof Reconnected) {
			template = "Reconnected";
			subject = "*OPCOSN: Your power has been reconnected.";
		}
		emailTemplate.put("Template", template);
		emailTemplate.put("Subject", subject);
		return emailTemplate;
	}

	/**
	 * @param type
	 */
	public <T extends Header> String getOpcoAbbreviatedName(T type) {
		OperatingCompanyManager operatingCompanyManager = new OperatingCompanyManager();
		Map<String, OperatingCompanyV2> operatingCompanyMap = operatingCompanyManager.BuildOperatingCompanyMap();
		OperatingCompanyV2 opco = operatingCompanyMap.get(type.getPremiseNumber().substring(0,2));
		return opco.getAbbreviatedName();
	}

	public <T> HashMap<String, String> getSMSTemplate(T type) {
		HashMap<String, String> smsTemplate = new HashMap<String, String>();
		if (type instanceof Disconnected) {
			String tmeplateStr = "*OPCOSN: Account at *ADDRESS has been disconnected due to non-payment of *DISCAMOUNT. View reconnection time and fees *OPCOURL/account.";
			if(!((Disconnected) type).getOpcoAbbreviatedName().equalsIgnoreCase("SWEPCO")) {
				tmeplateStr +=  " Please turn all your breakers to the OFF position for service reconnection.";
			}
			smsTemplate.put("SMSText", tmeplateStr);
		}else if(type instanceof DisconnectNotice) {
			smsTemplate.put("SMSText",
					"*OPCOSN: Disconnect scheduled at *ADDRESS for acct ending in *ACCOUNTNUMBER. Payment of *DISCAMOUNT required by *DUEDATE to avoid disconnect. Pay:*OPCOURL/pay.");
		}else if(type instanceof Reconnected) {
			if(!((Reconnected) type).getOpcoAbbreviatedName().equalsIgnoreCase("PSO") && ! ((Reconnected) type).getOpcoAbbreviatedName().equalsIgnoreCase("SWEPCO")) {
				smsTemplate.put("SMSText",
						"*OPCOSN: Service at *ADDRESS was connnected on *RECONNECTDATE. If your power is not on, please ensure main breaker is turned ON.");
			}else {
				smsTemplate.put("SMSText",
						"*OPCOSN: Service at *ADDRESS was connnected on *RECONNECTDATE.");
			}
		}

		return smsTemplate;
	}

	public String getS3Key() {
		return s3Key;
	}

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}

}
