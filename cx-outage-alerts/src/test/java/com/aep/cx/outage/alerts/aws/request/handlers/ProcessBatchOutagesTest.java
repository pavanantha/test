package com.aep.cx.outage.alerts.aws.request.handlers;

//junit 4
// import static org.junit.Assert.*;
// import org.junit.Test;
//junit 5
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import com.google.gson.Gson;

public class ProcessBatchOutagesTest {

	@Test
	public void test() {
		
		String s = "[\r\n" + 
				"  {\"Data\":\r\n" + 
				"	[{\r\n" + 
				"    \"premiseNumber\": \"020368098\",\r\n" + 
				"    \"outageNumber\": \"1111112\",\r\n" + 
				"    \"outageStatus\": \"new\",\r\n" + 
				"    \"outageType\": \"predicted\",\r\n" + 
				"    \"premisePowerStatus\": \"OFF\",\r\n" + 
				"    \"outageCause\": \"accident\",\r\n" + 
				"    \"outageSimpleCause\": \"accident\",\r\n" + 
				"    \"outageETRType\": \"g\",\r\n" + 
				"    \"outageOverideFlag\": \"n\",\r\n" + 
				"    \"outageETR\": \"201906270702\",\r\n" + 
				"    \"outageCreationTime\": \"201906101002\",\r\n" + 
				"    \"outageRestorationTime\": \"201906101802\",\r\n" + 
				"    \"outageAsOfTime\": \"201906101002\",\r\n" + 
				"    \"outageArea\": \"1234\",\r\n" + 
				"    \"outageCustomerCount\": \"15\",\r\n" + 
				"    \"outageCustomerMAXCount\": \"300\"\r\n" + 
				"	}]\r\n" + 
				"  }\r\n" + 
				"]";
		
		Gson gson = new Gson();
		
		
		//fail("Not yet implemented");
	}

}
