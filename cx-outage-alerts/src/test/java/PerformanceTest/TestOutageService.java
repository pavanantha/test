package PerformanceTest;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import com.aep.cx.outage.alerts.aws.request.handlers.TestUtils;
import com.aep.cx.outage.alerts.domains.OutageData;
import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.outage.business.OutageService;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestOutageService {

	@Test
	public void test() {
		try {
			
			
			FileReader rd = new
			FileReader("C:\\Users\\s007063\\Downloads\\021288666.json"); 
			BufferedReader br = new BufferedReader(rd);
			  
			String s = br.readLine();
			  
			
			ObjectMapper mapper = new ObjectMapper(); 
			//ArrayList<OutageData> dataList = mapper.readValue(s, new TypeReference<ArrayList<OutageData>>() {});
			ArrayList<OutageData> dataList = mapper.readValue(s, new TypeReference<ArrayList<OutageEvent>>() {});
			 
			
			ArrayList<String> recordKeys = new ArrayList<String>();
			recordKeys.add("021288666.json");
			OutageService service = new OutageService();
			service.processMomentaryEvents(recordKeys);
			//service.processBatch(dataList.get(0).getOutageData(), "sustainedmomentarydelete");
			System.out.println();
			
			br.close();
			rd.close();

			//OutageData[] event = TestUtils.parse("/TestOutage.json", OutageData.class);
			//System.out.println("successful"+event.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
