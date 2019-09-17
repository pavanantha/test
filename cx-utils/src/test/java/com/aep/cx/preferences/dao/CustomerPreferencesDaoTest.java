package com.aep.cx.preferences.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.aep.cx.macss.customer.subscriptions.MACSSLayout;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.aep.cx.utils.alerts.common.data.AlertsNotificationData;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class CustomerPreferencesDaoTest {

	public ArrayList<AlertsNotificationData> alertsData = new ArrayList<AlertsNotificationData>();
	public HashMap<String, ArrayList<CustomerPreferences>> cpList = new HashMap<String, ArrayList<CustomerPreferences>>();
	ArrayList<CustomerPreferences> cpList1 = new ArrayList<CustomerPreferences>();
	public final String MQMessage = "MS                                            PAYSEC01SUBS-ALL        |02019151915|YATH0001                                                                                                                                                                                                                                                                                                                        |020191519|A|    451|                                                                                                                                                                                                                                                                                                                                |          |O|GHENT               |WV|25843     |OUTAGE          B~BILLINGPAYMENT  B~PSOVPP          N~POWERPAY        N~ORDER           N~                 ~                 ~                 ~                 ~                 ~";
	

	@Before
	public void setUp() throws Exception {
		AlertsNotificationData ad1 = new AlertsNotificationData();
		ad1.setPremiseNumber("979999999");
		alertsData.add(ad1);
		AlertsNotificationData ad2 = new AlertsNotificationData();
		ad2.setPremiseNumber("979999998");
		alertsData.add(ad2);
	}

	@Test
	public void test() {
		
		//MACSSLayout ml = new MACSSLayout(MQMessage);
		//System.out.println();
		CustomerPreferencesDao cp = new CustomerPreferencesDao();
		cpList = cp.getPreferencesByPremise(alertsData);
		System.out.println("Returned Preferences="+cpList);

	}

	@Test
	public void createPreference() {
		MACSSLayout ml = new MACSSLayout(MQMessage);
		CustomerPreferencesDao cp = new CustomerPreferencesDao();
		cp.updateCustomerPreference(ml.getCustomerPreferences());
	}
	 
}
