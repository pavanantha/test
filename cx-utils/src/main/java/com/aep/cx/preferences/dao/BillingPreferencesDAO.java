package com.aep.cx.preferences.dao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.aep.cx.utils.alerts.common.data.AlertsNotificationData;
import com.aep.cx.utils.enums.PreferencesTypes;
import com.aep.cx.utils.time.JsonDateTimeSerializer;
import com.amazonaws.SdkClientException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public class BillingPreferencesDAO {

	static final Logger logger = LogManager.getLogger(BillingPreferencesDAO.class);
	
	AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
	
	ObjectMetadata metadata;
	//S3Object refinedObject = null;
	S3Object accountPreferencesObject = null;

	private final String billingPreferencesBucket = System.getenv("BILLING_PREFERENCES_BUCKET");
	private final String powerPayPreferencesBucket = System.getenv("POWERPAY_PREFERENCES_BUCKET");
	private final String orderPreferencesBucket = System.getenv("ORDER_PREFERENCES_BUCKET");
	private HashMap<String, CustomerPreferences> preferencesMap = new HashMap<String, CustomerPreferences>();

	private ArrayList<CustomerContacts> customerContacts;
	private ArrayList<CustomerContacts> customerContactsforPreferences;


	public HashMap<String, CustomerPreferences> getPreferencesByAccountNumber(
			ArrayList<AlertsNotificationData> accountList,String alertName) {

		logger.info("Initialize ArrayList<CustomerPreferences>");
		ArrayList<CustomerPreferences> respDaoList;

		logger.info("Convert Outage Event into AlertsNotificationData");
		for (AlertsNotificationData alertData : accountList) {

			logger.info("getCustomerPreferences(alertData)");
			CustomerPreferences cp = getCustomerPreferences(alertData.getAccountNumber(),alertName);
			
			if(cp.getCustomerContacts().size() > 0) {
				logger.info("preferencesMap.put(alertData.getPremiseNumber(), respDaoList)");
				preferencesMap.put(alertData.getAccountNumber(), cp);
			}
		}
		logger.info("Convert Outage Event into AlertsNotificationData");
		return preferencesMap;
	}

	public CustomerPreferences getCustomerPreferences(String accountNumber,String alertName) {
		String bucketName=this.getBucketname(alertName);
		ObjectMapper mapper = new ObjectMapper();

		logger.info("Initialize ArrayList<CustomerPreferences>");
		ArrayList<CustomerPreferences> cpList = new ArrayList<CustomerPreferences>();

		logger.info("DateTimeFormat.forPattern");
		DateTimeFormatter patternFormat = DateTimeFormat.forPattern("yyyyMMddHHmm");
		CustomerPreferences pref=null;
		
		try {

			accountPreferencesObject = s3Client.getObject(bucketName, accountNumber);
			InputStream is = accountPreferencesObject.getObjectContent();
			pref = mapper.readValue(is,new TypeReference<CustomerPreferences>() {});

		} catch (SdkClientException e) {
			logger.debug("account Number " + accountNumber + " does not exist in "+alertName + " Bucket");
		} catch (JsonParseException e) {
			// TODO: handle exception
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		CustomerPreferences cp = new CustomerPreferences();
		customerContactsforPreferences = new ArrayList<CustomerContacts>();
		if (null != pref) {
			for (CustomerContacts cc : pref.getCustomerContacts()) {
				
				if (null != cc.getEndPoint() && null !=cc.getWebId()) {
						customerContactsforPreferences.add(cc);
					}		
			}
			if (customerContactsforPreferences.size() > 0) {
				cp.setCustomerInfo(pref.getCustomerInfo());
				cp.setCustomerContacts(customerContactsforPreferences);
			}
		}
		
		return cp;
	}

	public void UpdateCustomerPreferences(ArrayList<CustomerPreferences> preferencesList,String alertName) {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("EST"));
		System.out.println("Start of Update preferences :" +c.getTime());
		System.out.println("Size of Records :" + preferencesList.size());
		for (CustomerPreferences preference : preferencesList) {
			updateCustomerPreference(preference,alertName);
		}
		System.out.println("End of Update preferences :" +c.getTime());
	}

	public void updateCustomerPreference(CustomerPreferences preference,String alertName) {
		System.out.println("Alert getting Processed:" + alertName + " for the account " + preference.getCustomerInfo().getAccountNumber());
		//Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new JsonDateTimeSerializer()).create();
		String bucketName=this.getBucketname(alertName);

		CustomerPreferences oldPrefs = getCustomerPreferences(preference.getCustomerInfo().getAccountNumber(),alertName);
		try {
			ArrayList<CustomerContacts> clist = new ArrayList<CustomerContacts>();
			if (null != oldPrefs.getCustomerContacts() && oldPrefs.getCustomerContacts().size() > 0) {
				for (CustomerContacts oldCC : oldPrefs.getCustomerContacts()) {
					Boolean matchFound = false;
					for (CustomerContacts newCC : preference.getCustomerContacts()) {
						if (oldCC.getWebId().equalsIgnoreCase(newCC.getWebId())) {
							matchFound = true;
						}
					}
					if (!matchFound) {
						clist.add(oldCC);
					}
					
				}
				for (CustomerContacts customerContacts : preference.getCustomerContacts()) {
					if (null != customerContacts.getEndPoint()) {
						clist.add(customerContacts);
					}
				}
				preference.setCustomerContacts(clist);
			}
			else {
				for (CustomerContacts customerContacts : preference.getCustomerContacts()) {
					if (null != customerContacts.getEndPoint()) {
						clist.add(customerContacts);
					}
				}
				preference.setCustomerContacts(clist);
			}
			if (preference.getCustomerContacts().size() > 0) {
				
				LoadData2S3 load2S3 = new LoadData2S3();
				load2S3.loadData(bucketName, preference.getCustomerInfo().getAccountNumber(), preference);
				
				System.out.println("Update Account Number:"+ preference.getCustomerInfo().getPremiseNumber()+ "#Acct:"+preference.getCustomerInfo().getAccountNumber() + " bucket name :" + bucketName );
			} else {
				s3Client.deleteObject(bucketName, preference.getCustomerInfo().getAccountNumber());
				System.out.println("Delete Account Number:"+ preference.getCustomerInfo().getPremiseNumber()+ "#Acct:"+preference.getCustomerInfo().getAccountNumber() + " bucket name :" + bucketName);
			}
			// }
		} catch (Exception ex) {
			System.out.println("Exception occured updating S3 Buket name:"+billingPreferencesBucket + ex.getMessage());
		}
	}
	
	public String getBucketname(String alertName) {

		String bucketName=null;
		if (alertName.contentEquals(PreferencesTypes.BILLINGPAYMENT.toString())) {
			bucketName = billingPreferencesBucket;
		}
		
		if (alertName.contentEquals(PreferencesTypes.POWERPAY.toString())) {
			bucketName = powerPayPreferencesBucket;
		}
		
		if (alertName.contentEquals(PreferencesTypes.ORDER.toString())) {
			bucketName = orderPreferencesBucket;
		}
		
		return bucketName;
	}
}
