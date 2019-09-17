package com.aep.cx.outage.alerts.dao;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

public class NotifiedOutageAlertsStateDao {
	static final Logger logger = LogManager.getLogger(NotifiedOutageAlertsStateDao.class);
	private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

	DynamoDBMapper mapperdb = new DynamoDBMapper(client);

	public NotifiedOutageAlertsState getNotifiedOutageAlertState(String premiseNumber) {
		NotifiedOutageAlertsState alertState = new NotifiedOutageAlertsState();
		  
		alertState.setPremiseNumber(premiseNumber);
		DynamoDBQueryExpression<NotifiedOutageAlertsState> queryExpression = new DynamoDBQueryExpression<NotifiedOutageAlertsState>()
				.withHashKeyValues(alertState);
		List<NotifiedOutageAlertsState> itemList = mapperdb.query(NotifiedOutageAlertsState.class, queryExpression);

		if (itemList.size() > 0)
			return itemList.get(0);
		return null;
	}

	public String updateNotifiedOutageAlertState(NotifiedOutageAlertsState alertInfo) {
		mapperdb.save(alertInfo);
		return null;
	}
}