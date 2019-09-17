// package com.aep.cx.utils.alerts.aws.request.handlers;

// import static org.junit.Assert.*;

// import java.util.ArrayList;
// import java.util.HashMap;

// import org.joda.time.DateTime;
// import org.junit.Before;
// import org.junit.Test;

// import com.aep.cx.preferences.dao.CustomerPreferences;
// import com.aep.cx.preferences.dao.CustomerPreferencesDao;
// import com.aep.cx.utils.alerts.common.data.AlertsNotificationData;
// import com.aep.cx.utils.alerts.notification.msessages.BuildEmail;
// import com.aep.cx.utils.alerts.notification.msessages.BuildSMS;

// public class EmailSmsNotificationsHandlerTest {

// public ArrayList<AlertsNotificationData> alertsData = new
// ArrayList<AlertsNotificationData>();
// public HashMap<String, ArrayList<CustomerPreferences>> cpList = new
// HashMap<String, ArrayList<CustomerPreferences>>();

// @Before
// public void setUp() throws Exception {
// AlertsNotificationData ad1 = new AlertsNotificationData();
// ad1.setPremiseNumber("019999999");
// ad1.setAlertName("predicted");
// ad1.setOutageSimpleCause("unknown");
// ad1.setEtrType("F");
// ad1.setOutageEtrTime(DateTime.now().plusDays(3));
// alertsData.add(ad1);
// AlertsNotificationData ad2 = new AlertsNotificationData();
// ad2.setPremiseNumber("019999998");
// ad2.setAlertName("predicted");
// ad2.setOutageSimpleCause("unknown");
// ad2.setEtrType("F");
// ad2.setOutageEtrTime(DateTime.now().plusDays(3));
// alertsData.add(ad2);
// }

// @Test
// public void test() {
// CustomerPreferencesDao cp = new CustomerPreferencesDao();
// cpList = cp.getPreferencesByPremise(alertsData);
// BuildEmail email = new BuildEmail(alertsData, cpList);
// BuildSMS sms = new BuildSMS(alertsData, cpList);
// //fail("Not yet implemented");
// }

// }
