package com.aep.cx.utils.opco;

import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

//junit 4
// import static org.junit.Assert.*;
// import org.junit.Before;
// import org.junit.BeforeClass;
// import org.junit.Test;
//junit 5
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class OperatingCompanyManagerTest {

    OperatingCompanyManager operatingCompanyManager;
    Map<String, OperatingCompanyV2> operatingCompanyMap;

    static final Logger logger = LogManager.getLogger(OperatingCompanyManagerTest.class);

    @BeforeEach
    public void setUp() throws Exception {

            logger.info("Setting up test cases...");
            operatingCompanyManager = new OperatingCompanyManager();
            operatingCompanyMap = operatingCompanyManager.BuildOperatingCompanyMap();
    }

    @Test
    public void testTimeZoneEST() {
        logger.info("Running Test Case Time Zone EST...");

        OperatingCompanyV2 opcoEST = operatingCompanyMap.get("01");
        logger.info("COMPANY CODE: " + opcoEST.getCompanyCode());
        assertEquals("America/New_York", opcoEST.getTimeZone());
    }

    @Test
    public void testTimeZoneCST() {

        logger.info("Running Test Case Time Zone CST...");

        OperatingCompanyV2 opcoCST = operatingCompanyMap.get("95");
        logger.info("COMPANY CODE: " + opcoCST.getCompanyCode());
        assertEquals("America/Chicago", opcoCST.getTimeZone());
    }
}
