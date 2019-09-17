package PerformanceTest;

import java.util.ArrayList;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.aep.cx.outage.alerts.domains.OutageData;
import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;

public class PerfomanceOutageTest {
	ObjectMetadata metadata;
	AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();

	//@Disabled("do not need to run")
	//@Test
	public void performanceOutagetest() throws JsonProcessingException, InterruptedException {
		
		ArrayList<OutageData> odList = new ArrayList<OutageData>();
		OutageData od = new OutageData();
		ArrayList<OutageEvent> oeList = new ArrayList<OutageEvent>();
		DateTimeFormatter dtFormatter = DateTimeFormat.forPattern("yyyyMMddHHmm");
		DateTime asOfDate =  DateTime.parse("201909101511", dtFormatter);//DateTime.now();
		int premiseNumber = Integer.parseInt("100000001");
		OutageEvent oe = null;
		int loopCount = 0;
		int recCount = 0;
		
		LoadData2S3 load2S3 = new LoadData2S3();
		int size = 1000;
		int total = 2000;

		for (int batch = 0; batch < 10; batch++) {
			
			for (int i = 0; i < total; i++) {
				oe = new OutageEvent();
				oe.setPremiseNumber(Integer.toString(premiseNumber));
				oe.setOutageNumber("3029444");
				oe.setOutageStatus("new");
				oe.setOutageType("predicted");
				if(batch > 8) {
					oe.setPremisePowerStatus("on");
				}else {
					oe.setPremisePowerStatus("off");
				}
				oe.setOutageCause("accident");
				oe.setOutageSimpleCause("accident");
				oe.setOutageETRType("g");
				oe.setOutageArea("1234");
				oe.setOutageAsOfTime(asOfDate.plusMinutes(batch * 1 ));
				oe.setOutageCreationTime(asOfDate.minusMinutes(15));
				oe.setOutageETR(asOfDate.plusHours(4).plusMinutes(16 * (batch+1)));
				oe.setOutageRestorationTime(asOfDate.plusDays(1));
				oe.setOutageCustomerCount(200);
				oe.setOutageCustomerMAXCount(28000);
				oe.setOutageOverideFlag("n");
				oe.setOutageTicketCount(1200);
				oeList.add(oe);
				premiseNumber++;
				recCount ++;
				if (recCount == size || recCount == total) {		
					od.setOutageData(oeList);
					odList.add(od);
					String key = "PerformanceTest_" + batch + "/" + DateTime.now().toString(DateTimeFormat.forPattern("yyyyMMddHHmmssSSS"))+"_"+UUID.randomUUID().toString().replaceAll("-", "");
					
					load2S3.loadData("dev-alerts-outage-raw-e1", key, odList);
					
					loopCount++;
					System.out.println("KEY *** " + key);
					System.out.println("Current Time " + new DateTime().toString(dtFormatter) + " loop Number = " + loopCount + " AsOfTime :- " +  oe.getOutageAsOfTime().toString(dtFormatter));
					oeList = new ArrayList<OutageEvent>();
					odList = new ArrayList<OutageData>();
					recCount = 0;
				}
			}
			premiseNumber = Integer.parseInt("100000001");
			Thread.sleep(1000);
			
			System.out.println("Done Batch :- " + batch);
		}
		System.out.println("End of Creating Data");
	}
}
