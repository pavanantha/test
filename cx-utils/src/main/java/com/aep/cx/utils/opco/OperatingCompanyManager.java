package com.aep.cx.utils.opco;

import com.aep.cx.utils.opco.OperatingCompanyV2;
import com.aep.cx.utils.enums.OperatingCompanyType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OperatingCompanyManager {

    // static final Logger logger = LogManager.getLogger();

    public Map<String, OperatingCompanyV2> BuildOperatingCompanyMap() {

        OperatingCompanyV2 templateOperatingCompany = new OperatingCompanyV2();

        templateOperatingCompany.setLearnMoreLinkUrl("");
        templateOperatingCompany.setManageAlertUrl("/alerts");
        templateOperatingCompany.setReportOutageUrl("/out");
        templateOperatingCompany.setPayMyBillUrl("/pay/");
        templateOperatingCompany.setUnsubscribeUrl("/alerts/unsubscribe");
        templateOperatingCompany.setSMSHelpUrl("/help");
        templateOperatingCompany.setOutageStatusUrl("/status");
        templateOperatingCompany.setAssistAgencyLink("/bills/assistance");
        templateOperatingCompany.setBillAccountURL("/account/bills/");

        Map<String, OperatingCompanyV2> operatingCompaniesMap = new HashMap<String, OperatingCompanyV2>();

        // 01 Appalachian Power
        // Use Template to overwrite each OpCode Defaults
        OperatingCompanyV2 operatingCompany01 = new OperatingCompanyV2(templateOperatingCompany);

        operatingCompany01.setCompanyCode("01");
        operatingCompany01.setChannelID("150036");
        operatingCompany01.setFullName("Appalachian Power");
        operatingCompany01.setAbbreviatedName("APCo");
        operatingCompany01.setOpcoSite("http://apcopwr.com");
        operatingCompany01.setOperatingCompanyType(OperatingCompanyType.AppalachianPower);
        operatingCompany01.setTimeZone("America/New_York");

        operatingCompaniesMap.put(operatingCompany01.getCompanyCode(), operatingCompany01);

        // 02 Appalachian Power
        // Use Template to overwrite each OpCode Defaults
        OperatingCompanyV2 operatingCompany02 = new OperatingCompanyV2(templateOperatingCompany);

        operatingCompany02.setCompanyCode("02");
        operatingCompany02.setChannelID("150036");
        operatingCompany02.setFullName("Appalachian Power");
        operatingCompany02.setAbbreviatedName("APCO");
        operatingCompany02.setOpcoSite("http://apcopwr.com");
        operatingCompany02.setOperatingCompanyType(OperatingCompanyType.AppalachianPower);
        operatingCompany02.setTimeZone("America/New_York");

        operatingCompaniesMap.put(operatingCompany02.getCompanyCode(), operatingCompany02);

        // 06 Appalachian Power
        // Use Template to overwrite each OpCode Defaults
        OperatingCompanyV2 operatingCompany06 = new OperatingCompanyV2(templateOperatingCompany);

        operatingCompany06.setCompanyCode("06");
        operatingCompany06.setChannelID("150036");
        operatingCompany06.setFullName("Appalachian Power");
        operatingCompany06.setAbbreviatedName("APCO");
        operatingCompany06.setOpcoSite("http://apcopwr.com");
        operatingCompany06.setOperatingCompanyType(OperatingCompanyType.AppalachianPower);
        operatingCompany06.setTimeZone("America/New_York");

        operatingCompaniesMap.put(operatingCompany06.getCompanyCode(), operatingCompany06);

        // 03 Kentucky Power
        // Use Template to overwrite each OpCode Defaults
        OperatingCompanyV2 operatingCompany03 = new OperatingCompanyV2(templateOperatingCompany);

        operatingCompany03.setCompanyCode("03");
        operatingCompany03.setChannelID("150039");
        operatingCompany03.setFullName("Kentucky Power");
        operatingCompany03.setAbbreviatedName("KY Pwr");
        operatingCompany03.setOpcoSite("http://kypco.com");
        operatingCompany03.setOperatingCompanyType(OperatingCompanyType.KentuckyPower);
        operatingCompany03.setTimeZone("America/New_York");

        operatingCompaniesMap.put(operatingCompany03.getCompanyCode(), operatingCompany03);

        // 04 Indiana Michigan Power
        // Use Template to overwrite each OpCode Defaults
        OperatingCompanyV2 operatingCompany04 = new OperatingCompanyV2(templateOperatingCompany);

        operatingCompany04.setCompanyCode("04");
        operatingCompany04.setChannelID("150038");
        operatingCompany04.setFullName("Indiana Michigan Power");
        operatingCompany04.setAbbreviatedName("I%26M");
        operatingCompany04.setOpcoSite("http://iandmpwr.com");
        operatingCompany04.setOperatingCompanyType(OperatingCompanyType.IndianaMichiganPower);
        operatingCompany04.setTimeZone("America/New_York");

        operatingCompaniesMap.put(operatingCompany04.getCompanyCode(), operatingCompany04);

        // 07 AEP Ohio
        // Use Template to overwrite each OpCode Defaults
        OperatingCompanyV2 operatingCompany07 = new OperatingCompanyV2(templateOperatingCompany);

        operatingCompany07.setCompanyCode("07");
        operatingCompany07.setChannelID("150034");
        operatingCompany07.setFullName("AEP Ohio");
        operatingCompany07.setAbbreviatedName("AEP Ohio");
        operatingCompany07.setOpcoSite("http://aepohio.com");
        operatingCompany07.setOperatingCompanyType(OperatingCompanyType.AEPOhio);
        operatingCompany07.setTimeZone("America/New_York");

        operatingCompaniesMap.put(operatingCompany07.getCompanyCode(), operatingCompany07);

        // 10 AEP Ohio
        // Use Template to overwrite each OpCode Defaults
        OperatingCompanyV2 operatingCompany10 = new OperatingCompanyV2(templateOperatingCompany);

        operatingCompany10.setCompanyCode("10");
        operatingCompany10.setChannelID("150034");
        operatingCompany10.setFullName("AEP Ohio");
        operatingCompany10.setAbbreviatedName("AEP Ohio");
        operatingCompany10.setOpcoSite("http://aepohio.com");
        operatingCompany10.setOperatingCompanyType(OperatingCompanyType.AEPOhio);
        operatingCompany10.setTimeZone("America/New_York");

        operatingCompaniesMap.put(operatingCompany10.getCompanyCode(), operatingCompany10);

        // 95 Public Service Company of Oklahoma
        // Use Template to overwrite each OpCode Defaults
        OperatingCompanyV2 operatingCompany95 = new OperatingCompanyV2(templateOperatingCompany);

        operatingCompany95.setCompanyCode("95");
        operatingCompany95.setChannelID("150040");
        operatingCompany95.setFullName("Public Service Company of Oklahoma");
        operatingCompany95.setAbbreviatedName("PSO");
        operatingCompany95.setOpcoSite("http://psoklahoma.com");
        operatingCompany95.setOperatingCompanyType(OperatingCompanyType.PublicServiceCompanyOklahoma);
        operatingCompany95.setTimeZone("America/Chicago");

        operatingCompaniesMap.put(operatingCompany95.getCompanyCode(), operatingCompany95);

        // 96 Southwestern Electric Power Company
        // Use Template to overwrite each OpCode Defaults
        OperatingCompanyV2 operatingCompany96 = new OperatingCompanyV2(templateOperatingCompany);

        operatingCompany96.setCompanyCode("96");
        operatingCompany96.setChannelID("118723");
        operatingCompany96.setFullName("Southwestern Electric Power Company");
        operatingCompany96.setAbbreviatedName("SWEPCO");
        operatingCompany96.setOpcoSite("http://swepco.com");
        operatingCompany96.setOperatingCompanyType(OperatingCompanyType.SouthwesternElectricPowerCompany);
        operatingCompany96.setTimeZone("America/Chicago");

        operatingCompaniesMap.put(operatingCompany96.getCompanyCode(), operatingCompany96);

        // 94 AEP Texas
        // Use Template to overwrite each OpCode Defaults
        OperatingCompanyV2 operatingCompany94 = new OperatingCompanyV2(templateOperatingCompany);

        operatingCompany94.setCompanyCode("94");
        operatingCompany94.setChannelID("150041");
        operatingCompany94.setFullName("AEP Texas");
        operatingCompany94.setAbbreviatedName("AEP Texas");
        operatingCompany94.setOpcoSite("http://aeptexas.com");
        operatingCompany94.setOperatingCompanyType(OperatingCompanyType.AEPTexas);
        operatingCompany94.setTimeZone("America/Chicago");

        operatingCompaniesMap.put(operatingCompany94.getCompanyCode(), operatingCompany94);

        // 97 AEP Texas
        // Use Template to overwrite each OpCode Defaults
        OperatingCompanyV2 operatingCompany97 = new OperatingCompanyV2(templateOperatingCompany);

        operatingCompany97.setCompanyCode("97");
        operatingCompany97.setChannelID("150041");
        operatingCompany97.setFullName("AEP Texas");
        operatingCompany97.setAbbreviatedName("AEP Texas");
        operatingCompany97.setOpcoSite("http://aeptexas.com");
        operatingCompany97.setOperatingCompanyType(OperatingCompanyType.AEPTexas);
        operatingCompany97.setTimeZone("America/Chicago");

        operatingCompaniesMap.put(operatingCompany97.getCompanyCode(), operatingCompany97);

        return operatingCompaniesMap;
    }
}
