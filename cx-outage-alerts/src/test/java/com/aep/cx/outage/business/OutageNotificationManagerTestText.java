/*
 * package com.aep.cx.outage.business;
 * 
 * import static org.junit.Assert.*;
 * 
 * import java.util.ArrayList;
 * 
 * import org.apache.commons.lang3.ObjectUtils.Null; import
 * org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
 * import org.joda.time.DateTime; import org.junit.Before; //import
 * org.junit.BeforeClass; import org.junit.Test;
 * 
 * import com.aep.cx.outage.alerts.domains.OutageEvent; import
 * com.aep.cx.preferences.dao.CustomerPreference; import
 * com.aep.cx.utils.opco.OperatingCompany; //import
 * com.aep.cx.outage.alerts.domains.CustomerOutageNotificationState; //import
 * com.aep.cx.outage.alerts.enums.ValueAddProcessingReasonType; import
 * com.aep.cx.outage.alerts.enums.ValueAddAlertType;
 * 
 * public class OutageNotificationManagerTestText {
 * 
 * public ArrayList<OutageEvent> outageData = new ArrayList<OutageEvent>();
 * OutageEvent AlertTypePredicted; OutageEvent AlertTypePredictedNOETR;
 * OutageEvent AlertTypeETR; OutageEvent AlertTypeRestored; OutageEvent
 * AlertTypeRestoredCancelled; OutageEvent MomentaryWait; OutageEvent
 * oldOutageEvent; OutageEvent NoPreviousOutage = null;
 * 
 * public ArrayList<CustomerPreference> CustData = new
 * ArrayList<CustomerPreference>(); CustomerPreference
 * AlertTypePredictedCustInfo; CustomerPreference
 * AlertTypePredictedNOETRCustInfo; CustomerPreference AlertTypeETRCustInfo;
 * CustomerPreference AlertTypeRestoredCustInfo; CustomerPreference
 * AlertTypeRestoredCancelledCustInfo; CustomerPreference MomentaryWaitCustInfo;
 * CustomerPreference OldOutageCustInfo;
 * 
 * public ArrayList<OperatingCompany> OpCoData = new
 * ArrayList<OperatingCompany>(); OperatingCompany AlterTypePredictedOpCo;
 * OperatingCompany AlterTypePredictedNOETROpCo; OperatingCompany
 * AlertTypeETROpCo; OperatingCompany AlertTypeRestoredOpCo; OperatingCompany
 * AlertTypeRestoredCancelledOpCo; OperatingCompany MomentaryWaitOpCo;
 * 
 * static final Logger logger =
 * LogManager.getLogger(OutageNotificationManagerTestText.class);
 * 
 * @Before public void setUp() throws Exception {
 * 
 * logger.info("Setting up test cases...");
 * 
 * // Build test case MomentaryWait current event object MomentaryWait = new
 * OutageEvent(); MomentaryWait.setPremiseNumber("953456669");
 * MomentaryWait.setPremisePowerStatus("OFF");
 * MomentaryWait.setOutageCreationTime(DateTime.now());
 * MomentaryWait.setOutageETR(DateTime.now().plusDays(1));
 * MomentaryWait.setOutageETRType("g");
 * MomentaryWait.setOutageType("predicted");
 * MomentaryWait.setMomentaryWaitThresholdInMinutes(10);
 * MomentaryWait.getVauleAddAlertType();
 * 
 * // Build test case Alert Type Predicted For CustomerOutageNotificationState
 * AlertTypePredicted = new OutageEvent(); AlertTypePredictedCustInfo = new
 * CustomerPreference(); AlertTypePredicted.setPremiseNumber("953456666");
 * AlertTypePredicted.setPremisePowerStatus("OFF");
 * AlertTypePredicted.setOutageCreationTime(DateTime.now().minusMinutes(15));
 * AlertTypePredicted.setOutageETR(DateTime.now().plusDays(1));
 * AlertTypePredicted.setOutageETRType("g");
 * AlertTypePredicted.setOutageType("predicted");
 * AlertTypePredicted.setMomentaryWaitThresholdInMinutes(10);
 * AlertTypePredicted.getVauleAddAlertType(); // Add Customer and OpCo info for
 * Text Build
 * AlertTypePredictedCustInfo.getCustomerInfo().setAccountNumber("123456789");
 * AlterTypePredictedOpCo.setAbbreviatedName("AEPOhio");
 * AlertTypePredictedCustInfo.getCustomerInfo().
 * setStreetAddress("12345 Happy Trails");
 * AlterTypePredictedOpCo.setOpcoSite("http://aepohio.com");
 * AlertTypePredicted.setOutageSimpleCause("accident");
 * AlertTypePredictedCustInfo.getCustomerInfo().setName("MrsHappy"); //
 * AlertTypePredictedCustInfo.getCustomerChannels()
 * 
 * // Build test case Alert Type No Predicted ETR
 * CustomerOutageNotificationState AlertTypePredictedNOETR = new OutageEvent();
 * AlertTypePredictedNOETRCustInfo = new CustomerPreference();
 * AlertTypePredictedNOETR.setPremiseNumber("953456788");
 * AlertTypePredictedNOETR.setPremisePowerStatus("OFF");
 * AlertTypePredictedNOETR.setOutageETR(DateTime.now());
 * AlertTypePredictedNOETR.setOutageAsOfTime(DateTime.now());
 * AlertTypePredictedNOETR.setOutageETRType("u");
 * AlertTypePredictedNOETR.setOutageCreationTime(DateTime.now().minusMinutes(15)
 * ); AlertTypePredictedNOETR.setOutageType("predicted");
 * AlertTypePredictedNOETR.setMomentaryWaitThresholdInMinutes(10);
 * AlertTypePredictedNOETR.getVauleAddAlertType();
 * AlertTypePredictedNOETR.setOutageSimpleCause("accident"); // Add Opco and
 * CustomerInfo
 * AlertTypePredictedNOETRCustInfo.getCustomerInfo().setAccountNumber(
 * "123456789"); AlertTypePredictedNOETRCustInfo.getCustomerInfo().
 * setStreetAddress("12345 Happy Trails");
 * AlertTypePredictedNOETRCustInfo.getCustomerInfo().setName("MrsHappy");
 * AlterTypePredictedNOETROpCo.setOpcoSite("http://aepohio.com");
 * AlterTypePredictedNOETROpCo.setAbbreviatedName("AEPOhio");
 * 
 * // Build test case Alert Type ETR CustomerOutageNotificationState
 * AlertTypeETR = new OutageEvent(); AlertTypeETRCustInfo = new
 * CustomerPreference(); AlertTypeETR.setPremiseNumber("953456788");
 * AlertTypeETR.setPremisePowerStatus("OFF");
 * AlertTypeETR.setOutageAsOfTime(DateTime.now().plusMinutes(20));
 * AlertTypeETR.setOutageETR(DateTime.now().minusDays(2));
 * AlertTypeETR.setOutageCreationTime(DateTime.now().minusMinutes(15));
 * AlertTypeETR.setOutageType("predicted"); AlertTypeETR.setOutageETRType("g");
 * AlertTypeETR.setMomentaryWaitThresholdInMinutes(10);
 * AlertTypeETR.getVauleAddAlertType();
 * AlertTypeETR.setOutageSimpleCause("accident"); // Add Customer and OpCo info
 * for Text Build
 * AlertTypeETRCustInfo.getCustomerInfo().setAccountNumber("123456789");
 * AlertTypeETRCustInfo.getCustomerInfo().setStreetAddress("12345 Happy Trails"
 * ); AlertTypeETRCustInfo.getCustomerInfo().setName("MrsHappy");
 * AlertTypeETROpCo.setOpcoSite("http://aepohio.com");
 * AlertTypeETROpCo.setAbbreviatedName("AEPOhio");
 * 
 * // Build test case Alert Type Alert Type Restored //
 * CustomerOutageNotificationState AlertTypeRestored = new OutageEvent();
 * AlertTypeRestoredCustInfo = new CustomerPreference();
 * AlertTypeRestored.setPremiseNumber("953456799");
 * AlertTypeRestored.setPremisePowerStatus("ON");
 * AlertTypeRestored.setOutageRestorationTime(DateTime.now().plusDays(1));
 * AlertTypeRestored.setOutageCreationTime(DateTime.now().minusMinutes(15));
 * AlertTypeRestored.setOutageType("restored");
 * AlertTypeRestored.setOutageETRType("g");
 * AlertTypeRestored.setMomentaryWaitThresholdInMinutes(1);
 * AlertTypeRestored.setOutageETR(DateTime.now().minusDays(2));
 * AlertTypeRestored.getVauleAddAlertType();
 * AlertTypeRestored.setOutageSimpleCause("restored"); // Add Customer and OpCo
 * info for Text Build
 * AlertTypeRestoredCustInfo.getCustomerInfo().setAccountNumber("123456789");
 * AlertTypeRestoredCustInfo.getCustomerInfo().
 * setStreetAddress("12345 Happy Trails");
 * AlertTypeRestoredCustInfo.getCustomerInfo().setName("MrsHappy");
 * AlertTypeRestoredOpCo.setOpcoSite("http://aepohio.com");
 * AlertTypeRestoredOpCo.setAbbreviatedName("AEPOhio");
 * 
 * // Build test case Alert Type AlertTypeRestoredCancelled current event object
 * AlertTypeRestoredCancelled = new OutageEvent();
 * AlertTypeRestoredCancelled.setPremiseNumber("953456722");
 * AlertTypeRestoredCancelled.setPremisePowerStatus("ON");
 * AlertTypeRestoredCancelled.setOutageRestorationTime(DateTime.now().minusDays(
 * 3));
 * AlertTypeRestoredCancelled.setOutageCreationTime(DateTime.now().minusMinutes(
 * 15)); AlertTypeRestoredCancelled.setOutageType("restored");
 * AlertTypeRestoredCancelled.setOutageETRType("g");
 * AlertTypeRestoredCancelled.setMomentaryWaitThresholdInMinutes(1);
 * AlertTypeRestoredCancelled.setOutageETR(DateTime.now().minusDays(2));
 * AlertTypeRestoredCancelled.getVauleAddAlertType();
 * AlertTypeRestoredCancelled.setOutageSimpleCause("canceled"); // Add Customer
 * and OpCo info for Text Build
 * AlertTypeRestoredCancelledCustInfo.getCustomerInfo().setAccountNumber(
 * "123456789"); AlertTypeRestoredCancelledCustInfo.getCustomerInfo().
 * setStreetAddress("12345 Happy Trails");
 * AlertTypeRestoredCancelledCustInfo.getCustomerInfo().setName("MrsHappy");
 * AlertTypeRestoredCancelledOpCo.setOpcoSite("http://aepohio.com");
 * AlertTypeRestoredCancelledOpCo.setAbbreviatedName("AEPOhio");
 * 
 * }
 * 
 * @Test public void testCustomerNotificationState_AlertType_PREDICTED() {
 * 
 * logger.
 * info("Running Test Case AlertType Predicted Customer Notification State...");
 * 
 * OutageManager manager = new OutageManager();
 * 
 * OutageEvent resultOutageEventDemo =
 * manager.ProcessRules(AlertTypePredicted, NoPreviousOutage);
 * logger.info("Results of Test Case AlertTypePredictedText:\n" +
 * resultOutageEventDemo.getVauleAddAlertType().name());
 * 
 * }
 * 
 * @Test public void testCustomerNotificationState_AlertType_PREDICTEDNOETR() {
 * 
 * logger.
 * info("Running Test Case AlertType Predicted Customer Notification State...");
 * 
 * OutageManager manager = new OutageManager();
 * 
 * OutageEvent resultOutageEventDemo =
 * manager.ProcessRules(AlertTypePredicted, NoPreviousOutage);
 * logger.info("Results of Test Case AlertType PREDICTED NO ETR:\n" +
 * resultOutageEventDemo.getVauleAddAlertType().name());
 * 
 * }
 * 
 * @Test public void testCustomerNotificationState_AlertType_ETR() {
 * 
 * logger.info("Running Test Case AlertType ETR Customer Notification State..."
 * );
 * 
 * OutageManager manager = new OutageManager();
 * 
 * OutageEvent resultOutageEventDemo =
 * manager.ProcessRules(AlertTypeETR, AlertTypePredictedNOETR);
 * logger.info("Results of Test Case AlertType ETR:\n" +
 * resultOutageEventDemo.getVauleAddAlertType().name());
 * 
 * }
 * 
 * }
 */
