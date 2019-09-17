package com.aep.cx.utils.thirdparty;

import java.io.BufferedReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import com.aep.cx.alerts.aws.services.AWSCredentials;
import com.aep.cx.utils.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import expect4j.Expect4j;

public class EmailProvider {
	
   
    //private final StringBuilder shellBuffer = new StringBuilder();
	static String host = null;
	static String user = null;
	static String pass = null;
	static String header = null;
	static String remoteFile = null;
    

	public static String Call2ExactTarget(String emailDeliveryType,ArrayList<String> emailList) {
		
		HashMap<String, String> secrets = AWSCredentials.getSecret("alerts/emailprovider", "us-east-1");
		host = secrets.get("ExactTargetUrl");
		user = secrets.get("ExactTargetUser");
		pass = secrets.get("ExactTargetPass");
		header = null;
		remoteFile = null;
		
		
		if (emailDeliveryType.contentEquals(MessageType.MCSXAT.toString())) {
			header = secrets.get("OutageHeader");
			remoteFile = secrets.get("OutageDir")+"/alerts_outage_elist_"+DateTime.now().toString("yyyyMMddHHmmss")+".txt"; 
		}
		
		try {
	       	JSch jsch = new JSch();
	       	Session session = jsch.getSession(user, host, 22);
	        session.setPassword(pass);
	        Hashtable<String, String> config = new Hashtable<String, String>();
	        config.put("StrictHostKeyChecking", "no");
	        session.setConfig(config);
	        session.connect(60000);
	        
	        Channel channel = session.openChannel("sftp");
	        ChannelSftp sftp = (ChannelSftp) channel;
	        Expect4j expect = new Expect4j(channel.getInputStream(), channel.getOutputStream());
	        channel.connect();
	        
	        
	        System.out.println("SFTP Channel created.");
	        InputStream in= null;
	        StringBuilder sb = new StringBuilder();
	        sb.append(header);
	        //out = channel.get("/import/test/sftp.txt");
			/*
			 * ArrayList<String> s = new ArrayList<String>(); s.add("hello asadaljdlahl");
			 * s.add("hello asadaljdlahl_1"); s.add("hello asadaljdlahl_2");
			 * 
			 * for (String string : s) { sb.append(string +
			 * System.getProperty("line.separator")); }
			 */
	        for (String string : emailList) {
				sb.append(string + System.getProperty("line.separator"));
			}
            in = new ByteArrayInputStream(String.valueOf(sb).getBytes("UTF-8"));
            sftp.put(in, remoteFile);
            //channel.start();
	        channel.disconnect();
	        session.disconnect();
		}
		
		catch (IOException i) {
			System.out.println("error io :"+i.getStackTrace()+i.getMessage());
			return "Failed";
		}
		
		catch (JSchException e) {
			System.out.println("error io :"+e.getStackTrace()+e.getMessage()+e.getLocalizedMessage());
			return "Failed";
		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println("error io :"+e.getStackTrace()+e.getMessage());
			return "Failed";
		}
		
		return "SuccessFull";
	}

}
