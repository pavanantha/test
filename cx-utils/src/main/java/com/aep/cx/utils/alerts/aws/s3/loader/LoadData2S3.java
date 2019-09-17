package com.aep.cx.utils.alerts.aws.s3.loader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.aep.cx.preferences.dao.CustomerPreferences;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LoadData2S3 {

	static final Logger logger = LogManager.getLogger(LoadData2S3.class);

	public LoadData2S3() {
	}

	public String loadData(String bucketName, String objKeyName, Object input) {
		try {
			ProfileCredentialsProvider profile = new ProfileCredentialsProvider("customer_dev");
			
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

			ObjectMetadata metadata = new ObjectMetadata();
			ObjectMapper mapper = new ObjectMapper();

			byte[] bytes = mapper.writeValueAsBytes(input);
			InputStream is = new ByteArrayInputStream(bytes);
			metadata.setContentLength(bytes.length);
			metadata.setContentType("application/json");
			metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);

			/*logger.info("Bucket Name: " + bucketName);
			logger.debug("**** Loading Object " + objKeyName + " into S3 bucket: " + bucketName);*/
			s3Client.putObject(bucketName, objKeyName, is, metadata);
			
		} catch (JsonProcessingException | AmazonServiceException e) {
			e.printStackTrace();
			logger.error("Exception LoadData2S3.loadData() : " + e.getMessage());
			System.out.println("Error 22: " + e.getMessage());
			return "FAILED";
		} catch (SdkClientException e) {
			// Amazon S3 couldn't be contacted for a response, or the client
			// couldn't parse the response from Amazon S3.
			logger.error("Exception LoadData2S3.loadData() : " + e.getMessage());
			System.out.println("Error 23: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Exception LoadData2S3.loadData() : " + ex.getMessage());
			System.out.println("Error 24: " + ex.getMessage());
			return "FAILED";
		}
		return "SUCCESS";
	}
	
	
	public static String deleteData(String bucketName, String objKeyName) {
		 try {
	            AmazonS3 s3Client = AmazonS3ClientBuilder
	            		.standard()	                    
	                    .build();
	            s3Client.deleteObject(bucketName, objKeyName);
	            
				System.out.println("**** Loading Object into S3 bucket: " + bucketName);
				logger.debug("**** deleting Object " +  objKeyName + " from S3 bucket: " + bucketName );

	        } catch(AmazonServiceException e) {
	        	e.printStackTrace();
				logger.error("Exception LoadData2S3.deleteData() : " + e.getMessage());
				return "FAILED";
	        } catch(SdkClientException e) {
	            // Amazon S3 couldn't be contacted for a response, or the client
	            // couldn't parse the response from Amazon S3.
	        	logger.error("Exception LoadData2S3.deleteData() : " + e.getMessage());
	            e.printStackTrace();
	        }catch (Exception ex) {
				ex.printStackTrace();
				logger.error("Exception LoadData2S3.deleteData() : " + ex.getMessage());
				return "FAILED";
			}
		 return "SUCCESS";
	}
	
	public static ArrayList<String> getKeyList(String bucketName) {
		 ArrayList<String> keyList = new ArrayList<String>();
		 try {
			    System.out.println("Bucket Name=" + bucketName);
		        AmazonS3 s3Client = AmazonS3ClientBuilder
		        		.standard()	                    
		                .build();
		        
		        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(1000);
		        ListObjectsV2Result result;
		        
		        System.out.println("Start Time Load Keys="+DateTime.now());
		        
		        do {
		            result = s3Client.listObjectsV2(req);

		            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
		                keyList.add(objectSummary.getKey());
		            }
		            // If there are more than maxKeys keys in the bucket, get a continuation token
		            // and list the next objects.
		            String token = result.getNextContinuationToken();
		            req.setContinuationToken(token);
		        } while (result.isTruncated());
		        
		        logger.info("Size of Key List ="+keyList.size());
		        logger.info("End Time Load Keys="+DateTime.now());
		        

	        } catch(AmazonServiceException e) {
	        	e.printStackTrace();
				logger.error("Exception LoadData2S3.deleteData() : " + e.getMessage());
	        } catch(SdkClientException e) {
	            // Amazon S3 couldn't be contacted for a response, or the client
	            // couldn't parse the response from Amazon S3.
	        	logger.error("Exception LoadData2S3.deleteData() : " + e.getMessage());
	            e.printStackTrace();
	        }catch (Exception ex) {
				ex.printStackTrace();
				logger.error("Exception LoadData2S3.deleteData() : " + ex.getMessage());
			}
		 finally {
			return keyList;
		}
	}
	
	public static ArrayList<String> getKeyList(String bucketName, String prefix,AmazonS3 s3Client) {
		 ArrayList<String> keyList = new ArrayList<String>();
		 try {
			    System.out.println("Bucket Name=" + bucketName);
		        //AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
		        
		        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(prefix).withMaxKeys(1000);
		        ListObjectsV2Result result;
		        
		        System.out.println("Start Time Load Keys="+DateTime.now());
		        
		        do {
		            result = s3Client.listObjectsV2(req);

		            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
		                keyList.add(objectSummary.getKey());
		            }
		            // If there are more than maxKeys keys in the bucket, get a continuation token
		            // and list the next objects.
		            String token = result.getNextContinuationToken();
		            req.setContinuationToken(token);
		        } while (result.isTruncated());
		        
		        logger.info("Size of Key List ="+keyList.size());
		        logger.info("End Time Load Keys="+DateTime.now());
		        

	        } catch(AmazonServiceException e) {
	        	e.printStackTrace();
				logger.error("Exception LoadData2S3.deleteData() : " + e.getMessage());
	        } catch(SdkClientException e) {
	            // Amazon S3 couldn't be contacted for a response, or the client
	            // couldn't parse the response from Amazon S3.
	        	logger.error("Exception LoadData2S3.deleteData() : " + e.getMessage());
	            e.printStackTrace();
	        }catch (Exception ex) {
				ex.printStackTrace();
				logger.error("Exception LoadData2S3.deleteData() : " + ex.getMessage());
			}
		 finally {
			return keyList;
		}
	}
	
	
	public static InputStream getObject(String bucketName, String prefix, String key, AmazonS3 s3Client) {
		 ArrayList<String> keyList = new ArrayList<String>();
		 try {
			    System.out.println("Bucket Name=" + bucketName);
			    if (null == s3Client) {
			    	s3Client = AmazonS3ClientBuilder.standard().build();
			    }
		        
				S3Object s3obj = s3Client.getObject(bucketName, prefix + "/" + key);
				return s3obj.getObjectContent();
				//pref = mapper.readValue(is,new TypeReference<CustomerPreferences>() {});     

	        } catch(AmazonServiceException e) {
	        	e.printStackTrace();
				logger.error("Exception LoadData2S3.deleteData() : " + e.getMessage());
				return null;
	        } catch(SdkClientException e) {
	            // Amazon S3 couldn't be contacted for a response, or the client
	            // couldn't parse the response from Amazon S3.
	        	logger.error("Exception LoadData2S3.deleteData() : " + e.getMessage());
	            e.printStackTrace();
	            return null;
	        }catch (Exception ex) {
				ex.printStackTrace();
				logger.error("Exception LoadData2S3.deleteData() : " + ex.getMessage());
				return null;
			}
	}

}
