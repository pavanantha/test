package com.aep.cx.utils.alerts.aws.request.handlers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LoadPreferences2S3Handler implements RequestHandler<ArrayList<Object>, String> {
	
	static final Logger logger = LogManager.getLogger(LoadPreferences2S3Handler.class);
	
	private AmazonS3 s3Client = AmazonS3ClientBuilder
										.standard()
										.build();
	ObjectMetadata metadata = new ObjectMetadata();
	
	@Override
	public String handleRequest(ArrayList<Object> inputRecList, Context context) {
		/*LambdaLogger logger = context.getLogger();*/
		logger.debug("***** LoadOutageData2S3Handler.handleRequest ******");
		
		ObjectMapper mapper = new ObjectMapper();
				
		try {
			byte[] bytes = mapper.writeValueAsBytes(inputRecList);
			InputStream is = new ByteArrayInputStream(bytes);
			metadata.setContentLength(bytes.length);     
			metadata.setContentType("application/json");
			DateTime tm = new DateTime(System.currentTimeMillis());
			/*String s3BucketName = ResourceBundle.getBundle("lambda").getString("S3_BUCKET_NAME");
			Properties prop = loadProperties();*/
			String s3BucketName = System.getenv("PREFERENCES_RAW_BUCKET");
			//PutObjectRequest putObjRequest = new PutObjectRequest(s3BucketName, tm.toString("yyyy")+"/"+tm.toString("MMM")+"/CustomerPreferences_"+tm.getDayOfYear()+tm.toString("HHmmss") , is, metadata);
			PutObjectRequest putObjRequest = new PutObjectRequest(s3BucketName, tm.toString("yyyy")+"/"+tm.toString("MMM")+"/"+tm.toString("dd")+"/CustomerPreferences_"+tm.toString("HHmmssSSS")+"_"+UUID.randomUUID().toString().replaceAll("-", "") , is, metadata);
			logger.debug("**** Putting Object into S3 bucket: " + s3BucketName);
			s3Client.putObject(putObjRequest);
		} catch (JsonProcessingException | AmazonS3Exception ex) {
			ex.printStackTrace();
			logger.debug("Exception : " + ex.getMessage());
			return "FAILED";
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.debug("Exception : " + ex.getMessage());
			return "FAILED";
		}
		
		return "SUCCESS";
	}
	
	protected static Properties loadProperties() {
		
		Properties prop = new Properties();
		try {
			ClassLoader classLoader = LoadPreferences2S3Handler.class.getClassLoader();
			InputStream is = classLoader.getResourceAsStream("lambda.properties");
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return prop;
	}

}
