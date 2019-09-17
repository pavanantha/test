package PerformanceTest;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.print.attribute.standard.MediaSize.Other;

import org.junit.Test;

import com.aep.cx.outage.alerts.domains.OutageData;
import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.outage.business.CustomerPreferencesService;
import com.aep.cx.outage.business.OutageNotificationService;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProcessSustainedTest {

	@Test
	public void test() {
		FileReader rd;
		try {
			
            CustomerPreferencesService customerPreferencesService = new CustomerPreferencesService();
            OutageNotificationService outageNotificationService = new OutageNotificationService();
			rd = new FileReader("C:\\Users\\s007063\\Downloads\\023627120.json");
			BufferedReader br = new BufferedReader(rd);
			
			String s = br.readLine();
			
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<OutageEvent> data = mapper.readValue(s, new TypeReference<ArrayList<OutageEvent>>() {});
			
            HashMap<String, ArrayList<CustomerPreferences>> prefs = customerPreferencesService
                    .getCustomerPreferences(data.get(0));
            
            if (prefs == null) {
                System.out.println("Retrieved customer preferences - account number: ");
            }
            
            
            if (prefs.get(data.get(0).getPremiseNumber()).size() > 0) {

            	System.out.println("Retrieved customer preferences - account number: "
                    + prefs.get(data.get(0).getPremiseNumber()).get(0).getCustomerInfo().getAccountNumber());
            }
            
            outageNotificationService.buildEventNotifications(data.get(0), data.get(0).getPremiseNumber(), "dev-alerts-outage-sustained-e1", prefs);
			System.out.println();
			
			rd.close();
			br.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
