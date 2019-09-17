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

public class DisconnectNoticeService {
	AmazonS3 s3Client;
	final ObjectMapper mapper;
	CustomerPreferences prefs = null;
	BillingPreferencesDAO dao = null;
	String s3Key = null;

	public DisconnectNoticeService() {
		this.s3Client = AmazonS3ClientBuilder.standard().build();
		this.mapper = new ObjectMapper();
		this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	public DisconnectNoticeService(AmazonS3 s3Client, ObjectMapper mapper) {
		this.s3Client = s3Client;
		this.mapper = mapper;
		this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	final Logger logger = LogManager.getLogger(DisconnectNoticeService.class);

	public ArrayList<DisconnectNotice> parseDisconnectedNoticeData(S3Event input) {
		logger.debug(" Entering  parseDisconnectedNoticeBatch ");
		for (S3EventNotificationRecord record : input.getRecords()) {

			String s3Bucket = record.getS3().getBucket().getName();

			s3Key = record.getS3().getObject().getKey();

			logger.debug("parseDisconnectedNoticeData() -- Bucket: " + s3Bucket + " and Key: " + s3Key);

			S3Object object = s3Client.getObject(new GetObjectRequest(s3Bucket, s3Key));

			S3ObjectInputStream objectData = object.getObjectContent();

			try {
				if (objectData != null) {

					ArrayList<DisconnectNotice> disconnectedNoticeList = mapper.readValue(objectData,
							new TypeReference<ArrayList<DisconnectNotice>>() {
							});

					logger.debug(
							"DisconnectNotice.parseBatch() -- Number of Records : " + disconnectedNoticeList.size());
					return disconnectedNoticeList;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public String buildDisconnectedContent(ArrayList<DisconnectNotice> disconnectedNoticeList) {
		try {
			dao = new BillingPreferencesDAO();

			for (DisconnectNotice disconnectNotice : disconnectedNoticeList) {
				prefs = dao.getCustomerPreferences(disconnectNotice.getAccountNumber(),
						PreferencesTypes.BILLINGPAYMENT.toString());

				System.out.println("prefereces=" + prefs);
				if (null != prefs && null != prefs.getCustomerContacts() && prefs.getCustomerContacts().size() > 0) {
					buildEmail(disconnectNotice, prefs);
					buildSMS(disconnectNotice, prefs);
				}
			}
			return "Successfully processed : build content and stored into per account bucket = " + s3Key;
		} catch (Exception e) {
			return "Failed processing : content build for payment = " + s3Key;
		}
	}

	public HashMap<String, ArrayList<String>> buildEmail(DisconnectNotice disconnectNotice,
			CustomerPreferences prefs) {

		HashMap<String, String> emailTemplate = getEmailTemplate(disconnectNotice);
		HashMap<String, ArrayList<String>> emailDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> emailList = new ArrayList<String>();
		ArrayList<String> historyList = new ArrayList<String>();

		OperatingCompanyManager cm = new OperatingCompanyManager();
		Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
		OperatingCompanyV2 opcoDetails = opcoBuild.get(disconnectNotice.getAccountNumber().substring(0, 2));

		EmailDeliveryHeader emailHeader = new EmailDeliveryHeader();

		emailHeader.setAccountNumber(disconnectNotice.getAccountNumber());
		emailHeader.setAccountNickname("");
		emailHeader.setChannelMemberID(opcoDetails.getChannelID());
		emailHeader.setCity(prefs.getCustomerInfo().getCity());
		emailHeader.setStreetAddress1(prefs.getCustomerInfo().getStreetAddress() + "***");
		emailHeader.setESID(disconnectNotice.getEzid());
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
				emailList.add(disconnectNotice.getExternalID() + emailHeader.toString() + ","
						+ disconnectNotice.getEmailContent());

				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(disconnectNotice.getAccountNumber());
				bm.setAlertName(disconnectNotice.getAlertType());
				bm.setEndPoint(cc.getEndPoint());
				bm.setWebID(cc.getWebId());
				bm.setOutageNumber(0);
				bm.setEmailTemplate(emailHeader.getTemplate());
				bm.setEmailSubject(emailHeader.getSubject());
				bm.setMessageTrackId(emailHeader.getHashLink());
				bm.setEmailMetaData("Template:" + bm.getEmailTemplate() + disconnectNotice.getMacssEmailContent());
				historyList.add(bm.getMacssBuild());
			}
		}
		if (emailList.size() > 0) {
			String bucketKey = "email_" + disconnectNotice.getAccountNumber() + "_"
					+ DateTime.now().toString("yyyyMMddHHmmss");
			emailDetails.put("xat", emailList);
			emailDetails.put("history", historyList);
			LoadData2S3 load2S3 = new LoadData2S3();
			load2S3.loadData(System.getenv("NOTIFICATION_EMAIL_BUCKET"), bucketKey, emailList);
			load2S3.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), bucketKey, historyList);
		}
		return emailDetails;
	}

	public HashMap<String, ArrayList<String>> buildSMS(DisconnectNotice disconnectNotice,
			CustomerPreferences prefs) {

		HashMap<String, ArrayList<String>> smsDetails = new HashMap<String, ArrayList<String>>();
		ArrayList<String> smsList = new ArrayList<String>();
		ArrayList<String> historyList = new ArrayList<String>();

		OperatingCompanyManager cm = new OperatingCompanyManager();
		Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();

		NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

		OperatingCompanyV2 opcoDetails = opcoBuild.get(disconnectNotice.getAccountNumber().substring(0, 2));
		HashMap<String, String> smsTemplate = getSMSTemplate(disconnectNotice);
		String smsText = smsTemplate.get("SMSText").replaceAll("\\*OPCOSN", opcoDetails.getAbbreviatedName())
				.replaceAll("\\*ACCOUNTNUMBER", disconnectNotice.getAccountNumber().substring(6, 6 + 5))
				.replaceAll("\\*DISCAMOUNT", "\\" + nf.format(disconnectNotice.getDisconnectAmount()))
				.replaceAll("\\*DUEDATE",
						disconnectNotice.getBillDueDate().toString(DateTimeFormat.forPattern("M/d/yyyy")));
		// .replaceAll("\\*OPCOURL", opcoDetails.getOpcoSite());
		// EmailDeliveryHeader emailHeader = new EmailDeliveryHeader();

		for (CustomerContacts cc : prefs.getCustomerContacts()) {
			if (!cc.getEndPoint().contains("@") && cc.getEndPoint().length() > 0) {

				smsText = smsText.replaceAll("\\*ADDRESS", prefs.getCustomerInfo().getStreetAddress() + "***");
				smsList.add("1" + cc.getEndPoint() + "||" + smsText + "~~");
				BuildMessageHistory bm = new BuildMessageHistory();
				bm.setAccountNumber(disconnectNotice.getAccountNumber());
				bm.setAlertName(disconnectNotice.getAlertType());
				bm.setEndPoint(cc.getEndPoint());
				bm.setWebID(cc.getWebId());
				bm.setOutageNumber(0);
				bm.setMessageTrackId(UUID.randomUUID().toString().replaceAll("-", ""));
				bm.setSmsText(smsText.replaceAll("%26", "&"));
				historyList.add(bm.getMacssBuild());
			}
		}

		if (smsList.size() > 0) {
			String bucketKey = "sms_" + disconnectNotice.getAccountNumber() + "_"
					+ DateTime.now().toString("yyyyMMddHHmmss");
			LoadData2S3 load2S3 = new LoadData2S3();
			load2S3.loadData(System.getenv("NOTIFICATION_SMS_BUCKET"), bucketKey, smsList);
			load2S3.loadData(System.getenv("NOTIFICATION_MESSAGE_HISTORY_BUCKET"), bucketKey, historyList);
			smsDetails.put("sms", smsList);
			smsDetails.put("history", historyList);
		}
		return smsDetails;
	}

	public HashMap<String, String> getEmailTemplate(DisconnectNotice disconnectNotice) {

		HashMap<String, String> emailTemplate = new HashMap<String, String>();
		emailTemplate.put("Template", "DisconnectApproaching");
		emailTemplate.put("Subject", "Disconnection of Electric Service is Scheduled.");
		return emailTemplate;
	}

	public HashMap<String, String> getSMSTemplate(DisconnectNotice disconnectNotice) {

		HashMap<String, String> smsTemplate = new HashMap<String, String>();
		smsTemplate.put("SMSText",
				"*OPCOSN: Disconnect scheduled at *ADDRESS for acct ending in *ACCOUNTNUMBER. Payment of *DISCAMOUNT required by *DUEDATE to avoid disconnect. Pay:https://appalachianpower.com/pay.");

		return smsTemplate;
	}

	public String getS3Key() {
		return s3Key;
	}

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}
}
