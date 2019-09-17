package com.aep.cx.convert.macss.ascii;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System;
import java.util.ArrayList;

import com.aep.cx.macss.customer.subscriptions.MACSSIntegrationWrapper;
import com.google.gson.Gson;

public class MACSSOneTime {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		String fileName  = "C:\\MobileAlerts_AWS\\Testing\\OneTime2AWS Prod\\06\\preferences"; 
		
		FileReader rd = new FileReader(fileName+".txt");
		BufferedReader br = new BufferedReader(rd);
		Gson gson = new Gson();
		
		String localFile1 = fileName+"_fmt.txt";
		FileWriter wr = new FileWriter(localFile1);
		BufferedWriter wr1 = new BufferedWriter(wr);

        String line = br.readLine();
        String previousRecord = null;
        ArrayList<String> s = new ArrayList<String>();
        
        MACSSIntegrationWrapper iw=null;

        int writeCount = 0;
        int fileCount = 0;
        int totalRecCount=0;
        while(line != null)
        {
        	if (writeCount == 600) {
                wr = new FileWriter(fileName + "_"+fileCount+"_fmt.txt");
        		wr1 = new BufferedWriter(wr);
                wr1.write(s.toString());
                wr1.flush();
                wr1.close();
                wr.close();
                s = new ArrayList<String>();
                fileCount ++;
        		writeCount = 0;
        	}
        	
        	iw = new MACSSIntegrationWrapper();
        	iw.setMessageString(line);
        	gson.toJson(iw);
        	s.add(gson.toJson(iw));
        	line = br.readLine();
        	writeCount ++;
        	totalRecCount++;
        }
        
        System.out.println("total count of records=" + totalRecCount);
        System.out.println("number of files written ="+fileCount);
        
        wr1.write(s.toString());
        wr1.flush();

       br.close();
       rd.close();
       wr1.close();
       wr.close();
	}

}
