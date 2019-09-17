package com.aep.cx.outage.business;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.aep.cx.outage.alerts.dao.NotifiedOutageAlertsStateDao;
import com.aep.cx.outage.alerts.domains.OutageBatchStatistic;
import com.aep.cx.outage.alerts.domains.OutageEvent;
import com.aep.cx.outage.alerts.domains.OutageStatisticsMonitor;
import com.aep.cx.outage.alerts.enums.ValueAddAlertType;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ProcessOutageStatus {
	final Logger logger = LogManager.getLogger(ProcessOutageStatus.class);
	AmazonS3 s3Client;
	final ObjectMapper mapper;
	NotifiedOutageAlertsStateDao notifiedOutageAlertsStateDao;
	ObjectMetadata metadata = new ObjectMetadata();

	public ProcessOutageStatus() {
		this(AmazonS3ClientBuilder.standard().build(),  new ObjectMapper());
	}

	public ProcessOutageStatus(AmazonS3 s3Client, ObjectMapper mapper) {
		this.s3Client = s3Client;
		this.mapper = mapper;
		this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	public void processOutageEventsPartialBatch(ArrayList<OutageEvent> events, Boolean momentaryProcess) {
		if (events != null) {
			OutageStatisticsMonitor statsMonitor = new OutageStatisticsMonitor();
			OutageService service = new OutageService();

			String sustainedBucket = System.getenv("OUTAGE_SUSTAINED_BUCKET");
			String restoredBucket = System.getenv("OUTAGE_RESTORED_BUCKET");
			String momentaryBucket = System.getenv("OUTAGE_MOMENTARY_BUCKET");
			LoadData2S3 load2S3 = new LoadData2S3();

			ArrayList<OutageEvent> eventList = new ArrayList<OutageEvent>();
			for (OutageEvent outageEvent : events) {
				eventList.add(outageEvent);
				outageEvent.setValueAddCurrentDateTime(DateTime.now());

				outageEvent.setMomentaryWaitThresholdInMinutes(
						Integer.parseInt(System.getenv("MOMENTARY_WAIT_THRESHOLD_IN_MINUTES")));

				CustomerOutageStatus os = new CustomerOutageStatus();
				os.setCustomeroutageStatus(outageEvent);
				statsMonitor.incrNumberOfEventsInBatch();
				
				try {
					if (outageEvent.getValueAddAlertType().equals(ValueAddAlertType.MOMENTARY)) {
						if(outageEvent.getIsPremisePowerOn()) {
							LoadData2S3.deleteData(momentaryBucket, outageEvent.getPremiseNumber());
							logger.info("Restored Event :- " + outageEvent.getPremiseNumber() + " getting dropped out, Since Event is still in Momentary");
							statsMonitor.incrDroppedEventCount();
						}else {
							load2S3.loadData(momentaryBucket, outageEvent.getPremiseNumber(), eventList);
							statsMonitor.incrNumberOfMomentaryEventToProcess();
						}
					} else if (!outageEvent.getValueAddAlertType().equals(ValueAddAlertType.NONE)) {
						DateTime tm = DateTime.now();
						String key = outageEvent.getPremiseNumber() + "/" + tm.toString("yyyy-MMM-dd") + "/" + outageEvent.getValueAddAlertType() + "/" + outageEvent.getBatchKey() + "/" + outageEvent.getPremiseNumber();
						if (outageEvent.getValueAddAlertType().equals(ValueAddAlertType.CANCELLED)
								|| outageEvent.getValueAddAlertType().equals(ValueAddAlertType.RESTORED)) {
							//LoadData2S3.deleteData(sustainedBucket, outageEvent.getPremiseNumber());
							load2S3.loadData(restoredBucket, key, eventList);
							statsMonitor.incrNumberOfRestoredEventToProcess();
						} else {
							LoadData2S3.deleteData(momentaryBucket, outageEvent.getPremiseNumber());
							load2S3.loadData(sustainedBucket, key, eventList);
							statsMonitor.incrNumberOfSustainedEventsToProcess();
						}
					} else {
						if (momentaryProcess) {
							LoadData2S3.deleteData(momentaryBucket, outageEvent.getPremiseNumber());
						}
						statsMonitor.incrDroppedEventCount();
					}
				}catch (Exception e) {
					e.printStackTrace();
				}

				eventList.clear();
			}

			/*OutageBatchStatistic outageBatchStatistic = statsMonitor.getOutageBatchStatistics(events.get(0).getBatchKey());
			service.persistBatchStatistics(outageBatchStatistic);*/
		}
	}
}
