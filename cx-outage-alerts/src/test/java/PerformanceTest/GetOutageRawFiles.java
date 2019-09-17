package PerformanceTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.InflaterInputStream;

import org.apache.logging.log4j.core.util.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.aep.cx.alerts.aws.services.AWSCredentials;
import com.aep.cx.macss.customer.subscriptions.MACSSIntegrationWrapper;
import com.aep.cx.outage.alerts.domains.OutageData;
import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

public class GetOutageRawFiles {

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		
		ProfileCredentialsProvider profile = new ProfileCredentialsProvider("customerprod");
		ProfileCredentialsProvider profiledev = new ProfileCredentialsProvider("default");
		System.out.println("creds :"+ profile.getCredentials());

		ClientConfiguration cc = new ClientConfiguration();
		cc.setConnectionMaxIdleMillis(10000);
		//cc.setClientExecutionTimeout(50 * 1000);
		cc.setMaxConnections(2500);
		cc.setMaxErrorRetry(10);
		cc.setConnectionTimeout(250000);
		//cc.setProxyHost("http://wsawest.aepsc.com");
		//cc.setProxyPort(8080);
		
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
				.withClientConfiguration(cc)
				.withCredentials(profile)
				.withRegion(Regions.US_EAST_1)
				.build();
		
		AmazonS3 s3ClientDev = AmazonS3ClientBuilder.standard()
				.withClientConfiguration(cc)
				.withCredentials(profiledev)
				.withRegion(Regions.US_EAST_1)
				.build();

		ObjectMetadata metadata;
		ObjectMapper mapper = null;

		String dir = "C:\\MobileAlerts_AWS\\Testing\\HotFixes\\OutageRawFiles";

		String localFile = dir + "\\raw" + ".json";
		
		ObjectMetadata objectMetadata = null;
		Date lastModified = null;
		mapper = new ObjectMapper();
		
		ArrayList<OutageData> outageDataList = new ArrayList<OutageData>();

		try {
			FileWriter wr = new FileWriter(localFile);
			BufferedWriter wr1 = new BufferedWriter(wr);
			DateTimeFormatter formatter1 = DateTimeFormat.forPattern("yyyy-MMM-ddHHmmss");
			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmm");
			DateTime lowDate =  formatter.parseDateTime("201907210935");
			DateTime highDate = formatter.parseDateTime("201907211830");
			
			
			//DateTimeFormatter isoformat = ISODateTimeFormat.dateTimeParser().forPattern("yyyyMMddHHmm");
			String prefix = "2019-Jul-21";
			//DateTime prefixDate = formatter1.parseDateTime(prefix);
			//System.out.println("prefix date:" + prefixDate);
			//System.out.println("prefix date:" + prefixDate.minusHours(4));
			//System.out.println("prefix date:" + prefixDate.minusHours(4).toInstant());

			ArrayList<String> fileList = LoadData2S3.getKeyList("prod-alerts-outage-raw-e1",prefix,s3Client);
			
			ArrayList<String> fileList1 = new ArrayList<String>();
			
			ArrayList<ArrayList<OutageData>> devList = new ArrayList<ArrayList<OutageData>>();
			
			System.out.println("Original key list size" + fileList.size() );
			//String key1 = null;
			for (int i=0;i<fileList.size();i++) {
			//for (String string : fileList) {
				System.out.println(fileList.get(i));
				//key1 = prefix+string.substring(26, 32);
				DateTime prefixDate = formatter1.parseDateTime(prefix+fileList.get(i).substring(26, 32)).minusHours(4);
				if (prefixDate.toInstant().getMillis() > lowDate.plusHours(4).toInstant().getMillis() &&
					prefixDate.toInstant().getMillis() < highDate.plusHours(4).toInstant().getMillis()) {
					System.out.println(prefixDate);
					fileList1.add(fileList.get(i));
				}
			}

			System.out.println("Modified key list size" + fileList1.size() );
			
			ArrayList<OutageData> macssPayload;
			S3Object macssPayloadObject;
			InputStream macssPayloadObjectData;
			String premNumber = "029089998";
			
			for (String key : fileList1) {

				
				try {
					//Thread.sleep(10);
					macssPayloadObject = s3Client.getObject(new GetObjectRequest("prod-alerts-outage-raw-e1", key));
					objectMetadata = macssPayloadObject.getObjectMetadata();
					lastModified = objectMetadata.getLastModified();
					
					System.out.println("last modifed utc="+lastModified.toInstant());
					System.out.println("high date utc="+highDate.toDate().toInstant());
					
					if (lastModified.toInstant().getEpochSecond() > highDate.toDate().toInstant().getEpochSecond()) {
						break;
					}
				
					if ((lastModified.toInstant().getEpochSecond() > lowDate.toDate().toInstant().getEpochSecond()) &&
						(lastModified.toInstant().getEpochSecond() < highDate.toDate().toInstant().getEpochSecond())) {
						macssPayloadObjectData = macssPayloadObject.getObjectContent();
						System.err.println(macssPayloadObjectData);
						
						outageDataList = mapper.readValue(macssPayloadObjectData, new TypeReference<ArrayList<OutageData>>() {});
						for (OutageData outageData : outageDataList) {
							for (OutageEvent outageEvent : outageData.getOutageData()) {
								
								if (outageEvent.getPremiseNumber().contentEquals(premNumber)) {
									wr = new FileWriter(dir+"\\"+key+outageEvent.getOutageAsOfTime().toString("yyyyMMddHHmm")+ "_"+outageEvent.getPremiseNumber()+".json");
									wr1 = new BufferedWriter(wr);
									wr1.write(mapper.writeValueAsString(outageEvent));
									System.out.println(outageEvent.getPremiseNumber());
									wr1.flush();
									wr.flush();
									wr1.close();
									wr.close();
									devList.add(outageDataList);
								}
								
							}
							//System.out.println(outageData.getOutageData().toString());
						}
						
						
						/*
						 * InputStreamReader reader = new InputStreamReader(macssPayloadObjectData);
						 * System.out.println("before buffered reader"); BufferedReader br = new
						 * BufferedReader(reader); System.out.println("buffered reader read a line");
						 * String line = br.readLine(); System.out.println("before writing"); while
						 * (null != line) { wr = new FileWriter(dir+"\\"+key+".json"); wr1 = new
						 * BufferedWriter(wr); wr1.write(line); wr1.flush(); wr1.close(); wr.close(); }
						 * reader.close();
						 */
						System.out.println("after writing");
				
						//wr1.flush();
						System.out.println("File written : " + key + "## Last Modofied Date##" + lastModified.toString());
					} 
					else {
						System.out.println("File skipped : " + key + "## Last Modofied Date##" + lastModified.toString());
					}
				}
				catch (SdkClientException e) {
					System.out.println("Failed on file  : " + key );
					continue;
				}
			}
			metadata = new ObjectMetadata();
			for (ArrayList<OutageData> outageData : devList) {
				byte[] bytes = mapper.writeValueAsBytes(outageData);
				InputStream is = new ByteArrayInputStream(bytes);
				metadata.setContentLength(bytes.length);     
				metadata.setContentType("application/json");
				DateTime tm = new DateTime(System.currentTimeMillis());
				String s3BucketName = "dev-alerts-outage-raw-e1";
				PutObjectRequest putObjRequest = new PutObjectRequest(s3BucketName, premNumber+"/"+DateTime.now().toString("yyyyMMddHHmmss") , is, metadata);
				s3ClientDev.putObject(putObjRequest);
				Thread.sleep(1000 * 60 * 2);
			}
		} catch (IOException e) {
			System.out.println("error :" + e.getMessage()+e.getStackTrace());

		}
	}
}
