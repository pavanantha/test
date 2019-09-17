package PerformanceTest;

import java.util.ArrayList;

import com.aep.cx.preferences.dao.CustomerContacts;
import com.aep.cx.preferences.dao.CustomerInfo;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.preferences.dao.CustomerPreferencesDao;

public class CreatePreferences {

	public static void main(String[] args) {
		int premiseNumber = Integer.parseInt("100000001");
		ArrayList<CustomerPreferences> cpList = new ArrayList<CustomerPreferences>();
		ArrayList<CustomerContacts> ccList = new ArrayList<CustomerContacts>();
		CustomerInfo cInfo = new CustomerInfo();
		cInfo.setCity("Columnbus");
		cInfo.setName("moba test");
		cInfo.setState("OH");
		cInfo.setStreetAddress("7765 M***");
		cInfo.setZipCode("43215");
		
		CustomerContacts cc = new CustomerContacts();
		cc.setEndPoint("moba@aep.com");
		cc.setWebId("moba@aep.com");
		ccList.add(cc);
		cc = new CustomerContacts();
		cc.setEndPoint("6145981329");
		cc.setWebId("moba@aep.com");
		ccList.add(cc);
		
		CustomerPreferences cp = new CustomerPreferences();
		CustomerPreferencesDao dao = new CustomerPreferencesDao();

		for (int i = 0; i < 3001; i++) {


			cInfo.setAccountNumber(Integer.toString(premiseNumber)+"11");
			cInfo.setPremiseNumber(Integer.toString(premiseNumber));
			cp.setCustomerContacts(ccList);
			cp.setCustomerInfo(cInfo);
			cpList.add(cp);
			dao.updateCustomerPreference(cp);
			premiseNumber++;
		}

	}

}
