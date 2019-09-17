package PerformanceTest;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.junit.Test;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.outage.alerts.enums.ValueAddAlertType;
import com.aep.cx.outage.alerts.enums.ValueAddProcessingReasonType;
import com.aep.cx.outage.business.CustomerPreferencesService;
import com.aep.cx.preferences.dao.CustomerPreferences;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.aep.cx.utils.enums.NotificationTemplateType;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class GetPreferencesTest {

	@Test
	public void test() {
		CustomerPreferencesService customerPreferencesService = new CustomerPreferencesService();
		OutageEvent outageEvent = new OutageEvent();
		OutageEvent outageEvent1 = new OutageEvent();
		outageEvent.setPremiseNumber("979999998");
		outageEvent.setOutageAsOfTime(DateTime.now());
		outageEvent.setOutageCreationTime(DateTime.now());
		outageEvent.setOutageETR(DateTime.now());
		outageEvent.setOutageRestorationTime(DateTime.now());
		outageEvent.setPremisePowerStatus("off");
		outageEvent.setValueAddAlertName("predicted");
		outageEvent.setValueAddAlertType(ValueAddAlertType.PREDICTED);
		outageEvent.setValueAddProcessingReasonType(ValueAddProcessingReasonType.OutageFirstNotificationWithETR);
		outageEvent.setOutageNumber("1234567");
		
        HashMap<String, ArrayList<CustomerPreferences>> prefs = customerPreferencesService
                .getCustomerPreferences(outageEvent1);

        if (prefs == null || prefs.isEmpty()) {
            System.out.println("Retrieved customer preferences - account number: ");
        }
	}

}
