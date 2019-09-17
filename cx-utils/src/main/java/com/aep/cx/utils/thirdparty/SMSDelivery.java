package com.aep.cx.utils.thirdparty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.client.methods.RequestBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import com.aep.cx.alerts.aws.services.AWSCredentials;
import com.amazonaws.Request;

public class SMSDelivery {
	static // http://localhost:8080/RESTfulExample/json/product/post

	String i2sms = null;

	public static String CallI2SMS(ArrayList<String> textMessagesList) {

		try {
			
			HashMap<String, String> secrets = AWSCredentials.getSecret("alerts/i2sms", "us-east-1");
			System.out.println("Entered SMS Delivery:" + textMessagesList.size());
			HttpAuthenticationFeature fature = HttpAuthenticationFeature.basic(secrets.get("user"), secrets.get("pass"));
			// I2SMSRequest request = new
			// I2SMSRequest("f87efaef4d092d5286f47f0e81ef15dc68491f6b", "12345",
			// "16145981329||Update: Estimated restoration of Oct 16 @11:30 PM for outage in
			// area of 123 M***. PSO will send any updates.~~");
			Client client = ClientBuilder.newClient();
			client.register(fature);

			WebTarget webTarget = client.target(secrets.get("url"));
			WebTarget employeeWebTarget = webTarget.path(secrets.get("targetPath"));
			Invocation.Builder invocationBuilder = employeeWebTarget.request(MediaType.APPLICATION_FORM_URLENCODED);

			Form form = new Form();
			form.param("channel", secrets.get("channel"));
			form.param("session", secrets.get("session"));
			// form.param("batch", "16145981329||Update: Estimated restoration of Oct 16
			// @11:30 PM for outage in area of 123 M***. PSO will send any updates.~~");
			StringBuilder textMessage = new StringBuilder();
			for (String string : textMessagesList) {
				textMessage.append(string.replaceAll("~~~~", "~~"));
			}
			form.param("batch", textMessage.toString());

			Response response1 = invocationBuilder
					.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
			i2sms = response1.readEntity(String.class);

			System.out.println(i2sms);
		} catch (Exception e) {
			i2sms = "error occured" + e.getMessage();
		} finally {
			return i2sms;
		}

	}

}
