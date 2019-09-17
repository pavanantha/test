package com.aep.cx.outage.business;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.aep.cx.outage.alerts.dao.NotifiedOutageAlertsState;
import com.aep.cx.outage.alerts.dao.NotifiedOutageAlertsStateDao;
import com.aep.cx.outage.alerts.domains.OutageBatchStatistic;
import com.aep.cx.outage.alerts.domains.OutageData;
import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.outage.alerts.domains.OutageStatisticsMonitor;
import com.aep.cx.outage.alerts.enums.ValueAddAlertType;
import com.aep.cx.outage.alerts.enums.ValueAddProcessingReasonType;
import com.aep.cx.utils.opco.OperatingCompanyManager;
import com.aep.cx.utils.opco.OperatingCompanyV2;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class OutageService {

	final Logger logger = LogManager.getLogger(OutageService.class);
	AmazonS3 s3Client;
	final OutageManager outageManager;
	final ObjectMapper mapper;
	NotifiedOutageAlertsStateDao notifiedOutageAlertsStateDao;

	// ProfileCredentialsProvider profile = new
	// ProfileCredentialsProvider("customer_qa");
	// AmazonS3 s3Client =
	// AmazonS3ClientBuilder.standard().withCredentials(profile).build();

	ObjectMetadata metadata = new ObjectMetadata();

	public OutageService() {
		this(AmazonS3ClientBuilder.standard().build(), new OutageManager(), new ObjectMapper());
	}

	OutageService(AmazonS3 s3Client, OutageManager outageManager, ObjectMapper mapper) {
		this.s3Client = s3Client;
		this.outageManager = outageManager;
		this.mapper = mapper;
		this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	public void printStateOfOutageEvent(OutageEvent outageEvent) {

		logger.debug("Premise Number: " + outageEvent.getPremiseNumber());

		logger.debug("ValueAddOutageEstimatedTimeToRestorationType: "
				+ outageEvent.getValueAddOutageEstimatedTimeToRestorationType());

		logger.debug("IsMomentary: " + outageEvent.getIsInMomentaryWait());

		logger.debug("ValueAddOutageType: " + outageEvent.getValueAddOutageType().name());
		logger.debug("Outage Event: " + outageEvent.getIsPremisePowerOn());

		if (outageEvent.getValueAddOperatingCompanyTimeZone() != null) {
			logger.debug(
					"Date: getValueAddOperatingCompanyTimeZone: " + outageEvent.getValueAddOperatingCompanyTimeZone());
		}

		logger.debug("Date: OutageRestorationTime: " + outageEvent.getOutageRestorationTime());
		logger.debug("Date: OutageETR: " + outageEvent.getOutageETR());
		logger.debug("Date: Outage Event Creation Time: " + outageEvent.getOutageCreationTime());

		logger.debug("Date: Outage Event Creation Time: in Millis: " + outageEvent.getOutageCreationTime().getMillis());
		if (outageEvent.getValueAddCurrentDateTime() != null) {
			logger.debug("Date: ValueAddCurrentDateTime: " + outageEvent.getValueAddCurrentDateTime());

			logger.debug(
					"Date: ValueAddCurrentDateTime: in Millis " + outageEvent.getValueAddCurrentDateTime().getMillis());

			logger.debug("Milli Differences: " + (outageEvent.getOutageCreationTime().getMillis()
					- outageEvent.getValueAddCurrentDateTime().getMillis()));
		}
	}

	public String processBatch(ArrayList<OutageEvent> batch, String batchKey) {
		ObjectMetadata metadata;
		S3Object refinedObject = null;
		S3Object momentaryObject = null;
		String targetRefinedBucket = null;
		ArrayList<OutageEvent> outageEventList = new ArrayList<OutageEvent>();
		ArrayList<OutageEvent> previousOutageEventList = new ArrayList<OutageEvent>();
		ArrayList<OutageEvent> previousOutageEvent = null;
		ArrayList<OutageEvent> previousMomentaryEvent = null;
		InputStream refinedObjectData = null;
		InputStream momentaryObjectData = null;

		// Create Operating Company Manager used to set the timezone of each event
		OperatingCompanyManager operatingCompanyManager = new OperatingCompanyManager();
		Map<String, OperatingCompanyV2> operatingCompanyMap = operatingCompanyManager.BuildOperatingCompanyMap();

		OutageStatisticsMonitor statsMonitor = new OutageStatisticsMonitor();

		logger.info("Starting Processing Batch:");
		// Batch Loop
		/* Get previousOutage (if it exists), for each OutageEvent in the batch */
		for (OutageEvent outageEvent : batch) {

			statsMonitor.incrNumberOfEventsInBatch();
			printStateOfOutageEvent(outageEvent);

			OperatingCompanyV2 outageOpco = operatingCompanyMap.get(outageEvent.getPremiseNumber().substring(0, 2));

			if (null == outageOpco) {
				statsMonitor.incrDroppedEventCount();
				continue;
			}

			outageEvent.setValueAddOperatingCompanyTimeZone(outageOpco.getTimeZone());
			outageEvent.setValueAddCurrentDateTime(DateTime.now());

			outageEvent.setMomentaryWaitThresholdInMinutes(
					Integer.parseInt(System.getenv("MOMENTARY_WAIT_THRESHOLD_IN_MINUTES")));

			logger.debug("After Time Update:");
			printStateOfOutageEvent(outageEvent);

			String eventKey = outageEvent.getPremiseNumber();
			boolean isPreviousSustainedEventExists = false;

			try {
				logger.debug("GetObject:" + eventKey);
				refinedObject = s3Client.getObject(System.getenv("OUTAGE_SUSTAINED_BUCKET"), eventKey);

				isPreviousSustainedEventExists = true;
				logger.debug("isPreviousSustainedEventExists:" + isPreviousSustainedEventExists);

			} catch (SdkClientException e) {
				logger.debug("premise number " + eventKey + " does not exist in sustained bucket");
				isPreviousSustainedEventExists = false;
				if (null != refinedObject) {
					try {
						refinedObject.close();
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
				}
			}

			// Store the previous and current outages
			if (isPreviousSustainedEventExists) {

				logger.debug("OutageService.processBatch() -- Key : " + eventKey + " does exist in sustained bucket");

				logger.debug("RefinedObjectData.getObjectContent");
				refinedObjectData = refinedObject.getObjectContent();

				try {
					logger.debug("previousOutageEvent");
					previousOutageEvent = mapper.readValue(refinedObjectData,
							new TypeReference<ArrayList<OutageEvent>>() {
							});
				} catch (Exception e) {
					e.printStackTrace();
					// Write it out to Exception
					continue;
				} finally {
					if (null != refinedObject) {
						try {
							refinedObject.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				logger.info("Add to previousOutageEventList");
				previousOutageEventList.add(previousOutageEvent.get(0));

				logger.info("Add to outageEventList");
				outageEventList.add(outageEvent);
			} else { // Momentary
				// Check if power was restored before OFF notification was sent
				try {
					logger.debug("Getting Momentary Off, check if power was restored before OFF notification was sent"
							+ eventKey);
					momentaryObject = s3Client.getObject(System.getenv("OUTAGE_MOMENTARY_BUCKET"), eventKey);
					logger.debug("Deleting Existing Momentary, so that it won't be picked up by Momentary Wrapper");
					s3Client.deleteObject(System.getenv("OUTAGE_MOMENTARY_BUCKET"), eventKey);
					statsMonitor.incrNumberOfMomentaryEventsDeleted();
				} catch (SdkClientException e) {
				} finally {
					if (null != refinedObject) {
						try {
							refinedObject.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				if (outageEvent.getIsPremisePowerOn()) {
					logger.debug("Ignore the Restored event, since there is no Corresponding Sustain Event.");
					printStateOfOutageEvent(outageEvent);
					statsMonitor.incrDroppedEventCount();
					continue;
				} else {
					OutageEvent nullOutageEvent = new OutageEvent();
					nullOutageEvent.setPremiseNumber(null);

					logger.debug("Set previousOutageEventList to Null: " + eventKey);
					previousOutageEventList.add(nullOutageEvent);

					logger.debug("Add Outage Event to outageEventList : " + eventKey);
					outageEventList.add(outageEvent);
				}
			}

			printStateOfOutageEvent(outageEvent);
		}
		logger.info("Finished Processing Batch:- " + batchKey);

		/*
		 * Pass current and previous outage event lists to OutageManager to be processed
		 * through the business rules.
		 */
		logger.debug("outageManager.ProcessRulesBatch");
		ArrayList<OutageEvent> processedOutageEvents = outageManager.ProcessRulesBatch(outageEventList,
				previousOutageEventList);

		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// Manager Loop
		logger.debug("Send the processed events to either the sustained, momentary, or restored buckets");
		// Send the processed events to either the sustained, momentary, or restored
		// buckets
		for (OutageEvent outageEvent : processedOutageEvents) {

			String eventKey = outageEvent.getPremiseNumber();

			/*
			 * If we get an exception state, do NOT add the event to the sustained or
			 * momentary bucket
			 */
			logger.debug("OutageService.processBatch() -- premise number: " + outageEvent.getPremiseNumber());
			logger.debug("OutageService.processBatch() -- reason type: "
					+ outageEvent.getValueAddProcessingReasonType().name());

			if ((outageEvent
					.getValueAddProcessingReasonType() == ValueAddProcessingReasonType.PreviousOutageEventAsOfDateIsGreater)
					|| (outageEvent.getValueAddProcessingReasonType() == ValueAddProcessingReasonType.None)) {
				logger.info(
						"OutageService.processBatch() -- Skipping to process Event --- Could be an ON Event without a corresponding Previous Event **** We should consider these events to be written out to another S3 to investigate. ");
				statsMonitor.incrDroppedEventCount();
				continue;
			}

			if (!outageEvent.getIsPremisePowerOn()) { // OFF Event
				boolean isPreviousSustainedEventExists = false;

				try {
					refinedObject = s3Client.getObject(System.getenv("OUTAGE_SUSTAINED_BUCKET"), eventKey);
					isPreviousSustainedEventExists = true;
				} catch (SdkClientException e) {
					isPreviousSustainedEventExists = false;
				}

				logger.debug("OutageService.processBatch() --Power OFF Event");
				if (!outageEvent.getIsInMomentaryWait() || isPreviousSustainedEventExists) {

					logger.debug("****** OutageService.processBatch() -- Sustained Event *******");
					targetRefinedBucket = System.getenv("OUTAGE_SUSTAINED_BUCKET");
					statsMonitor.incrNumberOfSustainedEventsToProcess();

				} else {
					logger.debug("****** OutageService.processBatch() -- Momentary Event *******");
					targetRefinedBucket = System.getenv("OUTAGE_MOMENTARY_BUCKET");
					statsMonitor.incrNumberOfMomentaryEventToProcess();
				}
				try {

					ArrayList<OutageEvent> putEvent = new ArrayList<OutageEvent>();
					putEvent.add(outageEvent);
					byte[] bytes = mapper.writeValueAsBytes(putEvent);
					InputStream is = new ByteArrayInputStream(bytes);
					metadata = new ObjectMetadata();
					metadata.setContentLength(bytes.length);
					metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
					metadata.setContentType("application/json");

					logger.debug("OutageService.processBatch() -- Putting Object key : " + eventKey
							+ " into a Bucket : " + targetRefinedBucket);
					s3Client.putObject(targetRefinedBucket, eventKey, is, metadata);

				} catch (JsonProcessingException e) {
					e.printStackTrace();
					logger.error("Error : " + e.getMessage());
					continue;
				} catch (SdkClientException e) {
					e.printStackTrace();
					logger.error("Error : " + e.getMessage());
					continue;
				}
			} else { // ON Event

				logger.debug("OutageService.processBatch() -- Power ON Event");
				try {
					ArrayList<OutageEvent> putEvent = new ArrayList<OutageEvent>();
					putEvent.add(outageEvent);
					byte[] bytes = mapper.writeValueAsBytes(putEvent);
					InputStream is = new ByteArrayInputStream(bytes);
					metadata = new ObjectMetadata();
					metadata.setContentLength(bytes.length);
					metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
					metadata.setContentType("application/json");
					logger.debug(
							"OutageService.processBatch() -- Putting Object key : " + outageEvent.getPremiseNumber()
									+ " into a Bucket : " + System.getenv("OUTAGE_RESTORED_BUCKET"));
					s3Client.putObject(System.getenv("OUTAGE_RESTORED_BUCKET"), outageEvent.getPremiseNumber(), is,
							metadata);
					statsMonitor.incrNumberOfRestoredEventToProcess();
					logger.info("Delete corresponding OFF event from sustained bucket -- key " + eventKey);

					s3Client.deleteObject(System.getenv("OUTAGE_SUSTAINED_BUCKET"), eventKey);
					statsMonitor.incrNumberOfSustainedEventsDeleted();
				} catch (JsonProcessingException | SdkClientException e) {
					e.printStackTrace();
					logger.error("Error : " + e.getMessage());
					continue;
				}
			}

			statsMonitor.incrNumberOfEventToProcess();
			statsMonitor.incrProcessingReasonTypeCount(outageEvent);
		}
		logger.debug("Persist Outage Events Statistics");
		OutageBatchStatistic outageBatchStatistic = statsMonitor.getOutageBatchStatistics(batchKey);
		persistBatchStatistics(outageBatchStatistic);

		return "SUCCESS";
	}

	public ArrayList<OutageEvent> parseBatch(S3Event input) {
		logger.debug(" Entering  OutageService.parseBatch() ");
		for (S3EventNotificationRecord record : input.getRecords()) {

			String s3Bucket = record.getS3().getBucket().getName();

			String s3Key = record.getS3().getObject().getKey();

			logger.debug("OutageService.parseBatch() -- Bucket: " + s3Bucket + " and Key: " + s3Key);

			// retrieve s3 object
			S3Object object = s3Client.getObject(new GetObjectRequest(s3Bucket, s3Key));

			InputStream objectData = object.getObjectContent();

			try {
				if (objectData != null) {

					ArrayList<OutageData> outageDataList = mapper.readValue(objectData,
							new TypeReference<ArrayList<OutageData>>() {
							});

					ArrayList<OutageEvent> outageEventList = outageDataList.get(0).getOutageData();
					object.close();

					logger.debug("OutageService.parseBatch() -- Number of Records : " + outageEventList.size());
					return outageEventList;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public String persistBatch(ArrayList<Object> inputRecList) {
		try {
			byte[] bytes = mapper.writeValueAsBytes(inputRecList);
			InputStream is = new ByteArrayInputStream(bytes);
			metadata.setContentLength(bytes.length);
			metadata.setContentType("application/json");
			metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);

			DateTime tm = new DateTime(System.currentTimeMillis());

			String s3BucketName = System.getenv("OUTAGE_RAW_BUCKET");
			String folder = tm.toString("yyyy") + "-" + tm.toString("MMM") + "-" + tm.getDayOfMonth();
			String key = folder + "/outage_alerts_" + tm.toString("HHmmssSSS");
			PutObjectRequest putObjRequest = new PutObjectRequest(s3BucketName, key, is, metadata);

			logger.debug("OutageService.persistBatch() -- Putting Object " + key + " into S3 bucket: " + s3BucketName);
			s3Client.putObject(putObjRequest);

		} catch (JsonProcessingException | AmazonS3Exception ex) {
			ex.printStackTrace();
			logger.error(" OutageService.persistBatch() -- Exception : " + ex.getMessage());
			return "FAILED";

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("OutageService.persistBatch() -- Exception : " + ex.getMessage());
			return "FAILED";
		}

		return "SUCCESS";
	}

	public String retrieveAsBatch(String bucketNameToFetchFrom, int sizeOfBatch) {

		ListObjectsV2Result listObjectsResult = s3Client.listObjectsV2(System.getenv(bucketNameToFetchFrom));

		List<String> eventKeysList = listObjectsResult.getObjectSummaries().stream().map(S3ObjectSummary::getKey)
				.collect(Collectors.toList());
		ArrayList<OutageEvent> momentaryEventsList = new ArrayList<>();

		ArrayList<Object> dataList = new ArrayList<>();

		logger.debug("Number of Events in Momentary Bucket: " + eventKeysList.size() + " %%%%%%%%%%");
		int batchSize = 0;
		for (String batchKey : eventKeysList) {

			S3Object rawObject = s3Client
					.getObject(new GetObjectRequest(System.getenv(bucketNameToFetchFrom), batchKey));
			InputStream rawObjectData = rawObject.getObjectContent();
			OutageEvent outageEvent;

			try {
				outageEvent = mapper.readValue(rawObjectData, OutageEvent.class);
				rawObject.close();
				momentaryEventsList.add(outageEvent);
			} catch (IOException e) {
				e.printStackTrace();
				return "FAILED";
			}
			if (++batchSize == sizeOfBatch && eventKeysList.size() > batchSize || batchSize == eventKeysList.size()) {
				OutageData dataObj = new OutageData();
				dataObj.setOutageData(momentaryEventsList);
				dataList.add(dataObj);
				persistBatch(dataList);
			}
		}

		return "SUCCESS";
	}

	public String processMomentaryEvents() {

		ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request();
		ListObjectsV2Result listObjectsResult = new ListObjectsV2Result();
		ObjectMetadata metadata;
		boolean batchIsReady = false;
		String IN_PROGRESS_KEY = "INPROGRESS";
		S3Object inProgressObject = null;
		boolean batchingInProgress = false;

		try {
			inProgressObject = s3Client.getObject(System.getenv("OUTAGE_MOMENTARY_BUCKET"), IN_PROGRESS_KEY);
			batchingInProgress = true;
			logger.debug("*** Momentary Lambda already currently running ***");
			inProgressObject.close();
		} catch (SdkClientException e) {
			batchingInProgress = false;
			logger.debug("*** Momentary Lambda not currently running, starting now ***");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (batchingInProgress) {
			return IN_PROGRESS_KEY;
		} else {
			try {
				ObjectMapper mapper = new ObjectMapper();
				byte[] bytes = mapper.writeValueAsBytes(IN_PROGRESS_KEY);
				InputStream is = new ByteArrayInputStream(bytes);
				metadata = new ObjectMetadata();
				metadata.setContentLength(bytes.length);
				metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
				metadata.setContentType("application/json");
				s3Client.putObject(System.getenv("OUTAGE_MOMENTARY_BUCKET"), IN_PROGRESS_KEY, is, metadata);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				logger.error("Error : " + e.getMessage());
				return "FAILED";
			} catch (SdkClientException e) {
				e.printStackTrace();
				logger.error("Error : " + e.getMessage());
				return "FAILED";
			}
		}

		listObjectsRequest.setBucketName(System.getenv("OUTAGE_MOMENTARY_BUCKET"));
		listObjectsResult = s3Client.listObjectsV2(listObjectsRequest);
		// TODO: grab only the events with a specific batchKey. Set the prefix below to
		// that batchKey.
		// listObjectsResult.setPrefix("prefix");
		List<String> recordKeys = listObjectsResult.getObjectSummaries().stream().map(S3ObjectSummary::getKey)
				.collect(Collectors.toList());
		logger.debug("Retrieved first " + recordKeys.size() + " records from the momentary bucket");

		for (int i = 0; i < recordKeys.size(); i++) {
			S3Object momentaryObject = s3Client
					.getObject(new GetObjectRequest(System.getenv("OUTAGE_MOMENTARY_BUCKET"), recordKeys.get(i)));
			InputStream momentaryObjectData = momentaryObject.getObjectContent();
			ArrayList<OutageEvent> outageEvent;
			try {
				outageEvent = mapper.readValue(momentaryObjectData, new TypeReference<ArrayList<OutageEvent>>() {
				});
				momentaryObject.close();
			} catch (IOException e) {
				e.printStackTrace();
				return "FAILED";
			}
			/*
			 * If we do not find a record that is out of the momentary wait threshold within
			 * the first 10 records, we assume most/all of the remaining records in this
			 * batch are still in momentary wait. Then we ignore the rest of the records.
			 */
			logger.debug("Batch is ready: " + batchIsReady);
			logger.debug("Processing record: " + outageEvent.get(0).getPremiseNumber() + " count: " + i);

			if (outageEvent.get(0).getValueAddCurrentDateTime() == null) {
				outageEvent.get(0).setValueAddCurrentDateTime(DateTime.now());
			}

			if (!outageEvent.get(0).getIsInMomentaryWait()) {

				logger.debug("Not in momentary wait: " + outageEvent.get(0).getPremiseNumber());
				/*
				 * try { byte[] bytes = mapper.writeValueAsBytes(outageEvent); InputStream is =
				 * new ByteArrayInputStream(bytes); metadata = new ObjectMetadata();
				 * metadata.setContentLength(bytes.length);
				 * metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
				 * metadata.setContentType("application/json");
				 * 
				 * s3Client.putObject(System.getenv("OUTAGE_SUSTAINED_BUCKET"),
				 * outageEvent.get(0).getPremiseNumber(), is, metadata);
				 * 
				 * } catch (JsonProcessingException e) { e.printStackTrace();
				 * logger.debug("Error : " + e.getMessage()); return "FAILED"; } catch
				 * (SdkClientException e) { e.printStackTrace(); logger.debug("Error : " +
				 * e.getMessage()); return "FAILED"; }
				 */
				s3Client.copyObject(System.getenv("OUTAGE_MOMENTARY_BUCKET"), outageEvent.get(0).getPremiseNumber(),
						System.getenv("OUTAGE_SUSTAINED_BUCKET"), outageEvent.get(0).getPremiseNumber());
				s3Client.deleteObject(System.getenv("OUTAGE_MOMENTARY_BUCKET"), outageEvent.get(0).getPremiseNumber());
				batchIsReady = true;
			}
		}
		s3Client.deleteObject(System.getenv("OUTAGE_MOMENTARY_BUCKET"), IN_PROGRESS_KEY);
		return "Success";
	}

	public String processMomentaryEvents(ArrayList<String> recordKeys) {
	
		ArrayList<OutageEvent> eventList = null;
		for (int i = 0; i < recordKeys.size(); i++) {
			S3Object momentaryObject = null;
			try {
				momentaryObject = s3Client
						.getObject(new GetObjectRequest(System.getenv("OUTAGE_MOMENTARY_BUCKET"), recordKeys.get(i)));
				InputStream momentaryObjectData = momentaryObject.getObjectContent();
				eventList = mapper.readValue(momentaryObjectData, new TypeReference<ArrayList<OutageEvent>>() {
				});
				ProcessOutageStatus po = new ProcessOutageStatus();
				po.processOutageEventsPartialBatch(eventList, true);
				
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			} finally {
				if (null != momentaryObject) {
					try {
						momentaryObject.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return "Successfully Processed Momentary Events";
	}
	
	
	public String processMomentaryEventsOld(ArrayList<String> recordKeys) {
		ObjectMetadata metadata;

		for (int i = 0; i < recordKeys.size(); i++) {
			ArrayList<OutageEvent> eventList;
			S3Object momentaryObject = null;
			try {
				momentaryObject = s3Client
						.getObject(new GetObjectRequest(System.getenv("OUTAGE_MOMENTARY_BUCKET"), recordKeys.get(i)));
				InputStream momentaryObjectData = momentaryObject.getObjectContent();
				eventList = mapper.readValue(momentaryObjectData, new TypeReference<ArrayList<OutageEvent>>() {
				});
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			} finally {
				if (null != momentaryObject) {
					try {
						momentaryObject.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			/*
			 * If we do not find a record that is out of the momentary wait threshold within
			 * the first 10 records, we assume most/all of the remaining records in this
			 * batch are still in momentary wait. Then we ignore the rest of the records.
			 */
			logger.debug("Processing record: " + eventList.get(0).getPremiseNumber() + " count: " + i);
			OutageEvent outageEventCopy = eventList.get(0);

			if (outageEventCopy.getValueAddCurrentDateTime() == null) {
				outageEventCopy.setValueAddCurrentDateTime(DateTime.now());
			}

			if (!eventList.get(0).getIsInMomentaryWait()) {

				Boolean sustainedExist = false;
				ArrayList<OutageEvent> sustainedOutageEvent;
				S3Object sustainedObject = null;
				try {
					sustainedObject = s3Client.getObject(
							new GetObjectRequest(System.getenv("OUTAGE_SUSTAINED_BUCKET"), recordKeys.get(i)));
					InputStream sustainedObjectData = sustainedObject.getObjectContent();
					sustainedOutageEvent = mapper.readValue(sustainedObjectData,
							new TypeReference<ArrayList<OutageEvent>>() {
							});
					sustainedExist = true;
				} catch (SdkClientException e) {
					sustainedOutageEvent = null;
					sustainedExist = false;
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				} finally {
					if (null != sustainedObject) {
						try {
							sustainedObject.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				logger.info("Not in momentary wait: " + outageEventCopy.getPremiseNumber());


				if (!sustainedExist) {
					if (null == outageEventCopy.getOutageETR() || outageEventCopy.getOutageETR().getMillis() <= DateTime
							.now().plusMinutes(15).getMillis()) {
						outageEventCopy.setValueAddProcessingReasonType(
								ValueAddProcessingReasonType.PredictedOutageFirstNotificationWithNoETR);
						outageEventCopy.setValueAddAlertType(ValueAddAlertType.PREDICTEDNOETR);
					} else {

						outageEventCopy.setValueAddProcessingReasonType(ValueAddProcessingReasonType.Predicted);
						outageEventCopy.setValueAddAlertType(ValueAddAlertType.PREDICTED);
					}

					try {
						updateNotifiedOutageAlertsState(outageEventCopy);
						byte[] bytes = mapper.writeValueAsBytes(eventList);
						InputStream is = new ByteArrayInputStream(bytes);
						metadata = new ObjectMetadata();
						metadata.setContentLength(bytes.length);
						metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
						metadata.setContentType("application/json");

						s3Client.putObject(System.getenv("OUTAGE_SUSTAINED_BUCKET"),
								outageEventCopy.getPremiseNumber(), is, metadata);

					} catch (JsonProcessingException | SdkClientException e) {
						e.printStackTrace();
						logger.debug("Error : " + e.getMessage());
						continue;
					}
				}
				s3Client.deleteObject(System.getenv("OUTAGE_MOMENTARY_BUCKET"), outageEventCopy.getPremiseNumber());
			}
		}
		return "Success";
	}

	public void persistBatchStatistics(OutageBatchStatistic outageBatchStatistic) {
		ObjectMetadata metadata;

		try {
			ObjectMapper mapper = new ObjectMapper();

			byte[] bytes = mapper.writeValueAsBytes(outageBatchStatistic);
			InputStream is = new ByteArrayInputStream(bytes);
			metadata = new ObjectMetadata();
			metadata.setContentLength(bytes.length);
			// metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
			metadata.setContentType("application/json");
			DateTime tm = DateTime.now();
			String fileName = outageBatchStatistic.getBatchKey() + "/" + tm.toString("HHmmssSSS");
			s3Client.putObject(System.getenv("OUTAGE_BATCH_STATISTICS_BUCKET"), fileName, is, metadata);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (SdkClientException e) {
			e.printStackTrace();
		}
	}

	public void processOnEvents(OutageEvent currentEvent, OutageStatisticsMonitor statsMonitor) {
		String eventKey = currentEvent.getPremiseNumber();
		logger.debug(" -- Power ON Event " + eventKey);
		updateNotifiedOutageAlertsState(currentEvent);
		try {
			ArrayList<OutageEvent> putEvent = new ArrayList<OutageEvent>();
			putEvent.add(currentEvent);
			byte[] bytes = mapper.writeValueAsBytes(putEvent);
			InputStream is = new ByteArrayInputStream(bytes);
			metadata = new ObjectMetadata();
			metadata.setContentLength(bytes.length);
			metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
			metadata.setContentType("application/json");
			String sustainedBucket = System.getenv("OUTAGE_SUSTAINED_BUCKET");
			String restoredBucket = System.getenv("OUTAGE_RESTORED_BUCKET");
			logger.debug("Placed Restored Event : " + eventKey + " into a Bucket : " + restoredBucket);
			s3Client.putObject(restoredBucket, currentEvent.getPremiseNumber(), is, metadata);
			statsMonitor.incrNumberOfRestoredEventToProcess();
			logger.info("Delete corresponding OFF event from " + sustainedBucket + " bucket for key " + eventKey);

			s3Client.deleteObject(sustainedBucket, eventKey);
			statsMonitor.incrNumberOfSustainedEventsDeleted();
		} catch (JsonProcessingException | SdkClientException e) {
			e.printStackTrace();
			// continue;
		}
	}

	public void processOffEvents(OutageEvent currentEvent, OutageEvent prevEvent,
			OutageStatisticsMonitor statsMonitor) {

		String eventKey = currentEvent.getPremiseNumber();
		String targetRefinedBucket;
		if (!currentEvent.getIsInMomentaryWait() || prevEvent != null) {
			logger.debug("****** -- Sustained Event ******* " + eventKey);
			targetRefinedBucket = System.getenv("OUTAGE_SUSTAINED_BUCKET");
			updateNotifiedOutageAlertsState(currentEvent);
			statsMonitor.incrNumberOfSustainedEventsToProcess();

		} else {
			logger.debug("****** -- Momentary Event ******* " + eventKey);
			targetRefinedBucket = System.getenv("OUTAGE_MOMENTARY_BUCKET");
			statsMonitor.incrNumberOfMomentaryEventToProcess();
		}

		try {
			ArrayList<OutageEvent> putEvent = new ArrayList<OutageEvent>();
			putEvent.add(currentEvent);
			byte[] bytes = mapper.writeValueAsBytes(putEvent);
			InputStream is = new ByteArrayInputStream(bytes);
			metadata = new ObjectMetadata();
			metadata.setContentLength(bytes.length);
			metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
			metadata.setContentType("application/json");

			logger.debug("Placing OFF Event : " + eventKey + " into a Bucket : " + targetRefinedBucket);
			s3Client.putObject(targetRefinedBucket, eventKey, is, metadata);

		} catch (JsonProcessingException | SdkClientException e) {
			e.printStackTrace();
			// continue;
		}
	}

	public String processOutageEventsPartialBatch(ArrayList<Object> batch) {
		ObjectMetadata metadata;
		S3Object refinedObject = null;
		S3Object momentaryObject = null;
		String targetRefinedBucket = null;
		ArrayList<OutageEvent> outageEventList = new ArrayList<OutageEvent>();
		ArrayList<OutageEvent> previousMomentaryEvent = null;
		InputStream refinedObjectData = null;
		InputStream momentaryObjectData = null;

		OutageStatisticsMonitor statsMonitor = new OutageStatisticsMonitor();
		ArrayList<OutageEvent> events = new ArrayList<>();

		try {
			byte[] bytes = mapper.writeValueAsBytes(batch);
			InputStream is = new ByteArrayInputStream(bytes);
			events = mapper.readValue(is, new TypeReference<ArrayList<OutageEvent>>() {
			});
			logger.info("********** partial batch size " + events.size());
		} catch (IOException e) {
			e.printStackTrace();
			return "FAILED";
		}

		String batchKey = events.get(0).getBatchKey();

		logger.info("BathKey: " + batchKey);

		for (OutageEvent outageEvent : events) {
			ArrayList<OutageEvent> previousOutageEventList = null;
			statsMonitor.incrNumberOfEventsInBatch();

			String eventKey = outageEvent.getPremiseNumber();

			if (!isCurrentOutageAlertAsOfTimeGreaterThanPreviousOne(outageEvent)) {
				logger.info("Dropped out --- since Event's AsOfTime is less than the Previous one:" + eventKey);
				statsMonitor.incrDroppedEventCount();
				statsMonitor.incrNumberOfStaleEventsInBatch();
				continue;
			}

			outageEvent.setMomentaryWaitThresholdInMinutes(
					Integer.parseInt(System.getenv("MOMENTARY_WAIT_THRESHOLD_IN_MINUTES")));

			boolean isPreviousSustainedEventExists = false;

			try {
				refinedObject = s3Client.getObject(System.getenv("OUTAGE_SUSTAINED_BUCKET"), eventKey);
				isPreviousSustainedEventExists = true;
				logger.debug("isPreviousSustainedEventExists:" + isPreviousSustainedEventExists);

			} catch (SdkClientException e) {
				logger.debug("premise number " + eventKey + " does not exist in sustained bucket");
				isPreviousSustainedEventExists = false;
				if (null != refinedObject) {
					try {
						refinedObject.close();
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
				}
			}

			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

			// Store the previous and current outages
			if (isPreviousSustainedEventExists) {
				logger.debug("Event : " + eventKey + " does exist in sustained bucket");

				refinedObjectData = refinedObject.getObjectContent();

				try {
					previousOutageEventList = mapper.readValue(refinedObjectData,
							new TypeReference<ArrayList<OutageEvent>>() {
							});
				} catch (Exception e) {
					e.printStackTrace();
					// Write it out to Exception
					continue;
				} finally {
					if (null != refinedObject) {
						try {
							refinedObject.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				if (!outageEvent.getOutageAsOfTime().isAfter(previousOutageEventList.get(0).getOutageAsOfTime())) {
					logger.info("Dropped out --- since Event's AsOfTime is less than the Previous one:" + eventKey);
					statsMonitor.incrDroppedEventCount();
					statsMonitor.incrNumberOfStaleEventsInBatch();
					continue;
				}
			} else { // Momentary
				// Check if power was restored before OFF notification was sent
				try {
					logger.debug("Getting Momentary Off, check if power was restored before OFF notification was sent "
							+ eventKey);
					momentaryObject = s3Client.getObject(System.getenv("OUTAGE_MOMENTARY_BUCKET"), eventKey);
					logger.debug("Deleting Existing Momentary, so that " + eventKey
							+ " won't be picked up by Momentary Wrapper");
					s3Client.deleteObject(System.getenv("OUTAGE_MOMENTARY_BUCKET"), eventKey);
					statsMonitor.incrNumberOfMomentaryEventsDeleted();
				} catch (SdkClientException e) {
					// do nothing
				} finally {
					if (null != momentaryObject) {
						try {
							momentaryObject.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				if (outageEvent.getIsPremisePowerOn()) {
					logger.debug("Ignore the Restored event, " + eventKey
							+ ", since there is no Corresponding Sustained Event.");
					statsMonitor.incrDroppedEventCount();
					continue;
				}
			}

			outageEvent = outageManager.ProcessRules(outageEvent,
					isPreviousSustainedEventExists ? previousOutageEventList.get(0) : null);

			logger.debug(" premise number: " + eventKey + "-- reason type: "
					+ outageEvent.getValueAddProcessingReasonType().name());

			if ((outageEvent
					.getValueAddProcessingReasonType() == ValueAddProcessingReasonType.PreviousOutageEventAsOfDateIsGreater)
					|| (outageEvent.getValueAddProcessingReasonType() == ValueAddProcessingReasonType.None)) {
				logger.info("Skipping to process Event " + outageEvent.getPremiseNumber()
						+ " --- Could be an ON Event without a corresponding Previous Event **** We should consider these events to be written out to another S3 to investigate. ");
				statsMonitor.incrDroppedEventCount();
				continue;
			}

			if (!outageEvent.getIsPremisePowerOn()) { // OFF Event
				processOnEvents(outageEvent, statsMonitor);
			} else { // ON Event
				processOffEvents(outageEvent, isPreviousSustainedEventExists ? previousOutageEventList.get(0) : null,
						statsMonitor);
			}

			statsMonitor.incrNumberOfEventToProcess();
			statsMonitor.incrProcessingReasonTypeCount(outageEvent);
			printStateOfOutageEvent(outageEvent);
		}
		OutageBatchStatistic outageBatchStatistic = statsMonitor.getOutageBatchStatistics(batchKey);
		persistBatchStatistics(outageBatchStatistic);

		return "SUCCESS";
	}

	public String createSmallBatchesToProcessOutageEvents(ArrayList<OutageEvent> batch, String batchKey,
			int batchSize) {
		// Create Operating Company Manager used to set the timezone of each event
		OperatingCompanyManager operatingCompanyManager = new OperatingCompanyManager();
		Map<String, OperatingCompanyV2> operatingCompanyMap = operatingCompanyManager.BuildOperatingCompanyMap();
		ArrayList<OutageEvent> eventsList = new ArrayList<>();

		int totalEvents = 0;
		int numberOfBatches = 0;
		OutageStatisticsMonitor statsMonitor = new OutageStatisticsMonitor();

		for (OutageEvent outageEvent : batch) {
			totalEvents++;
			OperatingCompanyV2 outageOpco = operatingCompanyMap.get(outageEvent.getPremiseNumber().substring(0, 2));
			if (null == outageOpco) {
				statsMonitor.incrDroppedEventCount();
				continue;
			}

			outageEvent.setBatchKey(batchKey);
			outageEvent.setValueAddOperatingCompanyTimeZone(outageOpco.getTimeZone());
			outageEvent.setValueAddCurrentDateTime(DateTime.now());

			eventsList.add(outageEvent);
			if (eventsList.size() == batchSize || batch.size() == totalEvents) {
				numberOfBatches++;
				InvokeRequest request = new InvokeRequest()
						.withFunctionName(System.getenv("OUTAGE_EVENTS_PARTIAL_BATCH_PROCESSOR"))
						.withInvocationType("Event");
				try {
					byte[] bytes = mapper.writeValueAsBytes(eventsList);
					InputStream is = new ByteArrayInputStream(bytes);
					request.withPayload(ByteBuffer.wrap(mapper.writeValueAsBytes(eventsList)));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}

				ClientConfiguration config = new ClientConfiguration();
				config.setConnectionTimeout(1000);
				config.setClientExecutionTimeout(1000 * 60);

				AWSLambdaClient asyncClient = (AWSLambdaClient) AWSLambdaAsyncClientBuilder.standard()
						.withRegion(Regions.US_EAST_1).withClientConfiguration(config).build();

				InvokeResult invResult = asyncClient.invoke(request);
				eventsList = new ArrayList<>();
			}
		}
		if(statsMonitor.getDroppedEventCount() > 0) {
			OutageBatchStatistic outageBatchStatistic = statsMonitor.getOutageBatchStatistics(batchKey);
			persistBatchStatistics(outageBatchStatistic);
		}
		logger.info(numberOfBatches + " batches created from parent Batch size of " + batch.size());
		return "SUCCESS";
	}

	public boolean isCurrentOutageAlertAsOfTimeGreaterThanPreviousOne(OutageEvent outageEvent) {
		NotifiedOutageAlertsState previousAlertStateInfo = notifiedOutageAlertsStateDao
				.getNotifiedOutageAlertState(outageEvent.getPremiseNumber());

		DateTimeFormatter dtFormatter = DateTimeFormat.forPattern("yyyyMMddHHmm");
		if (null != previousAlertStateInfo) {
			DateTime previousAlertsAsOfTimeFromDb = DateTime.parse(previousAlertStateInfo.getAsOfTime(),
					dtFormatter.withZone(DateTimeZone.forID(outageEvent.getValueAddOperatingCompanyTimeZone())));
			/*
			 * conditions to check whether a customer being previously notified of an outage
			 * or not
			 */
			if (!outageEvent.getOutageAsOfTime().isAfter(previousAlertsAsOfTimeFromDb)) {
				return false;
			}
		}
		return true;
	}

	public void updateNotifiedOutageAlertsState(OutageEvent outageEvent) {
		DateTimeFormatter dtFormatter = DateTimeFormat.forPattern("yyyyMMddHHmm");
		NotifiedOutageAlertsState alertStateInfo = new NotifiedOutageAlertsState();
		alertStateInfo.setAsOfTime(outageEvent.getOutageAsOfTime().toString(dtFormatter));
		alertStateInfo.setPremiseNumber(outageEvent.getPremiseNumber());
		alertStateInfo.setAlertType(outageEvent.getOutageType());
		notifiedOutageAlertsStateDao.updateNotifiedOutageAlertState(alertStateInfo);
	}

}
