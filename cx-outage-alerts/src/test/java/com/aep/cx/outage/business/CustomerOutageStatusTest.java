package com.aep.cx.outage.business;

import java.util.Map;

import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.outage.alerts.enums.ValueAddAlertType;
import com.aep.cx.utils.opco.OperatingCompanyManager;
import com.aep.cx.utils.opco.OperatingCompanyV2;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;

public class CustomerOutageStatusTest {
    @Disabled
    @Test
    public void Test()
    {
        String premiseNumber = "03000010";
        OperatingCompanyManager operatingCompanyManager = new OperatingCompanyManager();
        Map<String, OperatingCompanyV2> operatingCompanyMap = operatingCompanyManager.BuildOperatingCompanyMap();

        // OperatingCompanyV2 outageOpco = operatingCompanyMap.get(premiseNumber.substring(0, 2));
        CustomerOutageStatus test = new CustomerOutageStatus();
        DateTime asOfDate = DateTime.now().minusHours(1);
        OutageEvent oe = new OutageEvent();
        oe.setPremiseNumber(premiseNumber);
		oe.setOutageNumber("3029444");
		oe.setOutageStatus("new");
		oe.setOutageType("predicted");
		oe.setPremisePowerStatus("OFF");
		oe.setOutageCause("accident");
		oe.setOutageSimpleCause("accident");
		oe.setOutageETRType("g");
		oe.setOutageArea("1234");
		oe.setOutageAsOfTime(asOfDate);
		oe.setOutageCreationTime(asOfDate.minusMinutes(75));
		oe.setOutageETR(asOfDate.plusHours(6).plusMinutes(8));
        oe.setOutageRestorationTime(asOfDate.minusDays(999));
        //oe.setOutageRestorationTime(DateTime.now());
		oe.setOutageCustomerCount(200);
		oe.setOutageCustomerMAXCount(28000);
		oe.setOutageOverideFlag("n");
		oe.setOutageTicketCount(1200);
        OutageEvent resultingOutageEvent1 = test.setCustomeroutageStatus(oe);
        Assert.assertEquals(ValueAddAlertType.PREDICTED, resultingOutageEvent1.getValueAddAlertType());
        // Assert.assertEquals();

        try {
        java.util.concurrent.TimeUnit.SECONDS.sleep(1);
        } catch (Exception e)
        {

        }

        oe.setOutageType("restore");
        oe.setPremisePowerStatus("ON");
        oe.setOutageCreationTime(asOfDate.minusMinutes(75));
		oe.setOutageETR(asOfDate.plusHours(8).plusMinutes(8));
        oe.setOutageRestorationTime(asOfDate.plusDays(1));
        resultingOutageEvent1 = test.setCustomeroutageStatus(oe);
        Assert.assertEquals(ValueAddAlertType.RESTORED, resultingOutageEvent1.getValueAddAlertType());

        try {
            java.util.concurrent.TimeUnit.SECONDS.sleep(1);
            } catch (Exception e)
            {
    
            }
    
            oe.setOutageType("cancelled");
            oe.setPremisePowerStatus("ON");
            oe.setOutageCreationTime(asOfDate.minusMinutes(75));
            oe.setOutageETR(asOfDate.plusHours(8).plusMinutes(8));
            oe.setOutageRestorationTime(asOfDate.minusDays(999));
            resultingOutageEvent1 = test.setCustomeroutageStatus(oe);
            Assert.assertEquals(ValueAddAlertType.NONE, resultingOutageEvent1.getValueAddAlertType());

            try {
                java.util.concurrent.TimeUnit.SECONDS.sleep(1);
                } catch (Exception e)
                {
        
                }

            // premiseNumber = "0300021";
            // oe.setPremiseNumber(premiseNumber);
            // oe.setOutageType("predicted");
            // oe.setPremisePowerStatus("OFF");
            // oe.setOutageCreationTime(asOfDate.minusMinutes(75));
            // oe.setOutageETR(asOfDate.plusHours(7).plusMinutes(8));
            // oe.setOutageRestorationTime(asOfDate.minusDays(999));
            // resultingOutageEvent1 = test.setCustomeroutageStatus(oe);
            // Assert.assertEquals(ValueAddAlertType.PREDICTED, resultingOutageEvent1.getValueAddAlertType());

            // oe.setPremiseNumber(premiseNumber);
            // oe.setOutageType("cancelled");
            // oe.setPremisePowerStatus("ON");
            // oe.setOutageCreationTime(asOfDate.minusMinutes(75));
            // oe.setOutageETR(asOfDate.plusHours(7).plusMinutes(8));
            // oe.setOutageRestorationTime(asOfDate.minusDays(999));
            // resultingOutageEvent1 = test.setCustomeroutageStatus(oe);
            // Assert.assertEquals(ValueAddAlertType.CANCELLED, resultingOutageEvent1.getValueAddAlertType());


            // premiseNumber = "0300022";

            // oe.setPremiseNumber(premiseNumber);
            // oe.setOutageType("predicted");
            // oe.setPremisePowerStatus("OFF");
            // oe.setOutageCreationTime(asOfDate.minusMinutes(75));
            // oe.setOutageETR(asOfDate.minusHours(7));
            // oe.setOutageRestorationTime(asOfDate.minusDays(999));
            // resultingOutageEvent1 = test.setCustomeroutageStatus(oe);
            // Assert.assertEquals(ValueAddAlertType.PREDICTEDNOETR, resultingOutageEvent1.getValueAddAlertType());

            // oe.setPremiseNumber(premiseNumber);
            // oe.setOutageType("cancelled");
            // oe.setPremisePowerStatus("ON");
            // oe.setOutageCreationTime(asOfDate.minusMinutes(75));
            // oe.setOutageETR(asOfDate.plusHours(7).plusMinutes(8));
            // oe.setOutageRestorationTime(asOfDate.minusDays(999));
            // resultingOutageEvent1 = test.setCustomeroutageStatus(oe);
            // Assert.assertEquals(ValueAddAlertType.CANCELLED, resultingOutageEvent1.getValueAddAlertType());

            // premiseNumber = "0300025";

            // oe.setPremiseNumber(premiseNumber);
            // oe.setOutageType("predicted");
            // oe.setPremisePowerStatus("OFF");
            // oe.setOutageCreationTime(asOfDate.minusMinutes(75));
            // oe.setOutageETR(asOfDate.minusHours(7));
            // oe.setOutageRestorationTime(asOfDate.minusDays(999));
            // resultingOutageEvent1 = test.setCustomeroutageStatus(oe);
            // Assert.assertEquals(ValueAddAlertType.PREDICTEDNOETR, resultingOutageEvent1.getValueAddAlertType());

            // oe.setPremiseNumber(premiseNumber);
            // oe.setOutageType("cancelled");
            // oe.setPremisePowerStatus("ON");
            // oe.setOutageCreationTime(asOfDate.minusMinutes(75));
            // oe.setOutageETR(asOfDate.plusHours(7).plusMinutes(8));
            // oe.setOutageRestorationTime(asOfDate.minusDays(999));
            // resultingOutageEvent1 = test.setCustomeroutageStatus(oe);
            // Assert.assertEquals(ValueAddAlertType.CANCELLED, resultingOutageEvent1.getValueAddAlertType());









    }



}