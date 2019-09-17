package com.aep.cx.preferences.dao;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.aep.cx.macss.customer.subscriptions.MACSSLayout;
import com.aep.cx.utils.enums.PreferencesTypes;

public class BillingDAOTest {

	public static void main(String[] args) {
		
		// Outage is on, BillPay is on , PowerPay is off and Order is off
		String MQMessage="MSXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXEXPALERTSUBS-ALL        |04014530234|dlunsford1956                                                                                                                                                                                                                                                                                                                   |011002160|A|    453|moba@aep.com                                                                                                                                                                                                                                                                                                       |6145981329|E|KINGSPORT           |TN|37660-6403|Trish|Tierney|OUTAGE          B~BILLINGPAYMENT  B~PSOVPP          N~POWERPAY        N~ORDER           N~                 ~                 ~                 ~                 ~                 ~";
		
		// Outage is on, BillPay is off , PowerPay is on and Order is OFF
		//MQMessage="MSXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXEXPALERTSUBS-ALL        |01100216009|dlunsford1956                                                                                                                                                                                                                                                                                                                   |011002160|A|    453|dlunsford1956@charter.net                                                                                                                                                                                                                                                                                                       |4235780259|E|KINGSPORT           |TN|37660-6403|Trish|Tierney|OUTAGE          B~BILLINGPAYMENT  N~PSOVPP          N~POWERPAY        B~ORDER           N~                 ~                 ~                 ~                 ~                 ~";
		MACSSLayout ml = new MACSSLayout(MQMessage);
		
		CustomerPreferencesDao cp = new CustomerPreferencesDao();
		cp.updateCustomerPreference(ml.getCustomerPreferences());
		
		BillingPreferencesDAO bp = new BillingPreferencesDAO();
		bp.updateCustomerPreference(ml.getBillPayPreferences(), PreferencesTypes.BILLINGPAYMENT.toString());
		
		bp.updateCustomerPreference(ml.getPowerPayPreferences(), PreferencesTypes.POWERPAY.toString());
	}
	
	public static void InsertOrder(String billAccountNumber) {
		CustomerPreferences cp = new CustomerPreferences();
		CustomerInfo ci = new CustomerInfo();
		ci.setAccountNumber(billAccountNumber);
		ci.setCity("TestCity");
		ci.setState("TT");
		ci.setName("test Account");
		ci.setStreetAddress("T***");
		ArrayList<CustomerContacts> ccList = new ArrayList<CustomerContacts>();
		CustomerContacts cc = new CustomerContacts();
		cc.setEndPoint("6145981329");
		cc.setWebId("moba@aep.com");
		ccList.add(cc);
		cc = new CustomerContacts();
		cc.setEndPoint("moba@aep.com");
		cc.setWebId("moba@aep.com");
		cp.setCustomerContacts(ccList);
		cp.setCustomerInfo(ci);
		BillingPreferencesDAO bp = new BillingPreferencesDAO();
		bp.updateCustomerPreference(cp, PreferencesTypes.ORDER.toString());
	}

}
