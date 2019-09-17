package com.aep.cx.alerts.aws.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.amazonaws.services.secretsmanager.*;
import com.amazonaws.services.secretsmanager.model.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTime;

public class AWSCredentials {

	public static void main(String[] args) {
		//String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
		//String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
		String secretKey = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY";
		
		try {
			
			HashMap<String, String> map = getSecret("alerts/shadow","us-east-1");
			//DateTime.now().toString("yyyyMMdd")
			byte[] s = getSignatureKey(secretKey,"20120215", "us-east-1", "api gateway");
			
			System.out.println(Hex.encodeHex(s));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static byte[] HmacSHA256(String data, byte[] key) throws Exception {
	    String algorithm="HmacSHA256";
	    Mac mac = Mac.getInstance(algorithm);
	    mac.init(new SecretKeySpec(key, algorithm));
	    return mac.doFinal(data.getBytes("UTF-8"));
	}

	public static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
	    byte[] kSecret = ("AWS4" + key).getBytes("UTF-8");
	    //System.out.println(kSecret);
	    byte[] kDate = HmacSHA256(dateStamp, kSecret);
	    byte[] kRegion = HmacSHA256(regionName, kDate);
	    byte[] kService = HmacSHA256(serviceName, kRegion);
	    byte[] kSigning = HmacSHA256("aws4_request", kService);
	    return kSigning;
	}
	
	public static HashMap<String, String> getSecret(String secretName,String region){
		HashMap<String, String> map = null;
		ObjectMapper mapper = new ObjectMapper();
	    //secretName = "alerts/shadow";
	    //String region = "us-east-1";
	    AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard().withRegion(region).build();
	    
	    String secret, decodedBinarySecret;
	    GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
	                    .withSecretId(secretName);
	    GetSecretValueResult getSecretValueResult = null;

	    try {
	        getSecretValueResult = client.getSecretValue(getSecretValueRequest);
	        map = mapper.readValue(getSecretValueResult.getSecretString(), HashMap.class);
	    } catch (DecryptionFailureException e) {
	    	
	    } catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return map;
	}

}
