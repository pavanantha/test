package com.aep.cx.preferences.dao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import com.aep.cx.utils.alerts.common.data.AlertsNotificationData;
import com.aep.cx.utils.time.JsonDateTimeSerializer;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CustomerPreferencesDao {

	static final Logger logger = LogManager.getLogger(CustomerPreferencesDao.class);

	private static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
			.withEndpointConfiguration(
					new AwsClientBuilder.EndpointConfiguration("https://dynamodb.us-east-1.amazonaws.com", "us-east-1"))
			.build();

	private DynamoDB dynamoDB = new DynamoDB(client);
	private final String tableName = System.getenv("OUTAGE_PREFERENCES_DYNAMODBTABLE");
	private Table table;
	private HashMap<String, ArrayList<CustomerPreferences>> preferencesMap = new HashMap<String, ArrayList<CustomerPreferences>>();

	private ArrayList<CustomerContacts> customerContacts;
	private ArrayList<CustomerContacts> customerContactsforPreferences;
	// private ArrayList<CustomerPreferences> respDao = new
	// ArrayList<CustomerPreferences>();
	private GetItemSpec spec;
	private Item outcome;
	private UpdateItemSpec updateItemSpec;
	private DeleteItemSpec deleteItemSpec;

	public HashMap<String, ArrayList<CustomerPreferences>> getPreferencesByPremise(
			ArrayList<AlertsNotificationData> premiseList) {

		ArrayList<CustomerPreferences> respDaoList;

		for (AlertsNotificationData alertData : premiseList) {
			respDaoList = getCustomerPreferences(alertData);
			
			if(!respDaoList.isEmpty()) {
				preferencesMap.put(alertData.getPremiseNumber(), respDaoList);
			}
		}
		return preferencesMap;
	}

	public ArrayList<CustomerPreferences> getCustomerPreferences(AlertsNotificationData alertData) {

		ArrayList<CustomerPreferences> cpList = new ArrayList<CustomerPreferences>();
		table = dynamoDB.getTable(tableName);

		logger.info("Get Query Spec by Premise Number" + alertData.getPremiseNumber());
		QuerySpec spec = new QuerySpec().withKeyConditionExpression("premiseNumber = :premiseNumber")
				.withValueMap(new ValueMap().withString(":premiseNumber", alertData.getPremiseNumber()));

		ItemCollection<QueryOutcome> items = table.query(spec);

		Iterator<Item> iterator = items.iterator();
		InputStream contacts = null;
		InputStream info = null;

		ObjectMapper mapper = new ObjectMapper();
		while (iterator.hasNext()) {
			try {
				CustomerPreferences cp = new CustomerPreferences();
				Map<String, Object> s = iterator.next().asMap();
				contacts = new ByteArrayInputStream(s.get("preferences").toString().getBytes());
				info = new ByteArrayInputStream(s.get("CustomerInfo").toString().getBytes());

				customerContacts = mapper.readValue(contacts, new TypeReference<ArrayList<CustomerContacts>>(){});
				customerContactsforPreferences = new ArrayList<CustomerContacts>();
				for(CustomerContacts item : customerContacts) {
					if (null != item.getEndPoint() && null !=item.getWebId()) {
						customerContactsforPreferences.add(item);
					}
				}
				//cp.setCustomerContacts(mapper.readValue(contacts, new TypeReference<ArrayList<CustomerContacts>>(){}));
				cp.setCustomerContacts(customerContactsforPreferences);
				cp.setCustomerInfo(mapper.readValue(info, new TypeReference<CustomerInfo>() {
				}));
				cp.getCustomerInfo().setAccountNumber(s.get("accountNumber").toString());
				cp.getCustomerInfo().setPremiseNumber(s.get("premiseNumber").toString());
				if (customerContactsforPreferences.size() > 0) {
					cpList.add(cp);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return cpList;
	}

	public void UpdateCustomerPreferences(ArrayList<CustomerPreferences> preferencesList) {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("EST"));
		logger.debug("Start of Update preferences :" +c.getTime());
		for (CustomerPreferences preference : preferencesList) {
			updateCustomerPreference(preference);
		}
		logger.debug("End of Update preferences :" +c.getTime());
	}

	public void updateCustomerPreference(CustomerPreferences preference) {
		Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new JsonDateTimeSerializer()).create();

		table = dynamoDB.getTable(tableName);

		spec = new GetItemSpec().withPrimaryKey("premiseNumber", preference.getCustomerInfo().getPremiseNumber(),
				"accountNumber", preference.getCustomerInfo().getAccountNumber());
		outcome = table.getItem(spec);

		try {
			if (null != outcome) {
				CustomerContacts[] cs = gson.fromJson(outcome.get("preferences").toString(), CustomerContacts[].class);
				ArrayList<CustomerContacts> clist = new ArrayList<CustomerContacts>();
				for (int i = 0; i < cs.length; i++) {
					if (!cs[i].getWebId().equalsIgnoreCase(preference.getCustomerContacts().get(0).getWebId())) {
						clist.add(cs[i]);
					}

					System.out.println(cs[i].toString());
				}

				for (CustomerContacts customerContacts : preference.getCustomerContacts()) {
					if (null != customerContacts.getEndPoint()) {
						clist.add(customerContacts);
					}
				}
				preference.setCustomerContacts(clist);
			}
			if (preference.getCustomerContacts().size() > 0) {
				updateItemSpec = new UpdateItemSpec()
						.withPrimaryKey("premiseNumber", preference.getCustomerInfo().getPremiseNumber(),
								"accountNumber", preference.getCustomerInfo().getAccountNumber())
						.withUpdateExpression("set CustomerInfo = :a,preferences = :b")
						.withValueMap(new ValueMap().with(":a", gson.toJson(preference.getCustomerInfo())).with(":b",
								gson.toJson(preference.getCustomerContacts())))
						.withReturnValues(ReturnValue.UPDATED_NEW);
				UpdateItemOutcome udpateResult = table.updateItem(updateItemSpec);
				logger.debug("Update Premise Number:"+ preference.getCustomerInfo().getPremiseNumber()+ "#Acct:"+preference.getCustomerInfo().getAccountNumber());
			} else {
				deleteItemSpec = new DeleteItemSpec().withPrimaryKey("premiseNumber",
						preference.getCustomerInfo().getPremiseNumber(), "accountNumber",
						preference.getCustomerInfo().getAccountNumber());
				DeleteItemOutcome deleteResult = table.deleteItem(deleteItemSpec);
				logger.debug("Delete Premise Number:"+ preference.getCustomerInfo().getPremiseNumber()+ "#Acct:"+preference.getCustomerInfo().getAccountNumber());
			}
			// }
		} catch (Exception ex) {
			System.out.println("Exception occured updating DynamoDB:" + ex.getMessage());
		}

	}

	
	  public void deleteCustomerPreference(CustomerPreferences preference) {

		table = dynamoDB.getTable(tableName);

		deleteItemSpec = new DeleteItemSpec().withPrimaryKey("premiseNumber",
				preference.getCustomerInfo().getPremiseNumber(), "accountNumber",
				preference.getCustomerInfo().getAccountNumber());

		try {
			DeleteItemOutcome deleteResult = table.deleteItem(deleteItemSpec);
		} catch (Exception ex) {
			System.out.println("Exception occured deleting DynamoDB:" + ex.getMessage());
		}
	}
	 
	  
		public void updateBatchPreferences (ArrayList<CustomerPreferences> clist) {
			
			Gson gson = new Gson();
			ArrayList<Item> itemList = new ArrayList<Item>();
			
			try {
				
				int recCount = 0;
				BatchWriteItemOutcome outcome;
				TableWriteItems tw =  new TableWriteItems(tableName);
				String prevPremiseAccount = "000000000"+"00000000000";
				List<CustomerContacts> prevContactList = new ArrayList<CustomerContacts>();
				CustomerPreferences prevPreference = new CustomerPreferences();
				CustomerInfo cinfo = new CustomerInfo();
				cinfo.setAccountNumber("00000000000");
				prevPreference.setCustomerInfo(cinfo);
				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("EST"));
				System.out.println("Start of Update preferences :" +c.getTime());
				
				for (CustomerPreferences preference : clist) {
					if (recCount == 25) {
						tw.withItemsToPut(itemList);
						outcome = dynamoDB.batchWriteItem (tw);
				        do {  
				            // Confirm no unprocessed items 
				            Map<String, List<WriteRequest>> unprocessedItems 
				               = outcome.getUnprocessedItems();  
				                  
				            if (outcome.getUnprocessedItems().size() == 0) { 
				               System.out.println("All items processed."); 
				            } else { 
				               System.out.println("Gathering unprocessed items..."); 
				               outcome = dynamoDB.batchWriteItemUnprocessed(unprocessedItems); 
				            }  
				         } while (outcome.getUnprocessedItems().size() > 0);
				        itemList = new ArrayList<Item>();
						//tw =  new TableWriteItems(tableName);
						recCount = 0;
					}
					if (prevPremiseAccount.contentEquals(preference.getCustomerInfo().getPremiseNumber()
							+ preference.getCustomerInfo().getAccountNumber())) {

						prevContactList = prevPreference.getCustomerContacts();
						for (CustomerContacts customerContacts : preference.getCustomerContacts()) {
							prevContactList.add(customerContacts);
						}
						preference.setCustomerContacts(prevContactList);
					} else {
						if (!prevPremiseAccount.contentEquals("000000000"+"00000000000")) {
							itemList.add(new Item()
									.withPrimaryKey("premiseNumber", prevPreference.getCustomerInfo().getPremiseNumber(),
											"accountNumber", prevPreference.getCustomerInfo().getAccountNumber())
									.with("CustomerInfo", gson.toJson(prevPreference.getCustomerInfo()))
									.with("preferences", gson.toJson(prevPreference.getCustomerContacts()))
									.with("lastUpdateTime", DateTime.now().toString()));
						}

					}
					
					/*
					 * tw.withItemsToPut(new Item ().withPrimaryKey("premiseNumber",
					 * preference.getCustomerInfo().getPremiseNumber(), "accountNumber",
					 * preference.getCustomerInfo().getAccountNumber()) .with("CustomerInfo",
					 * gson.toJson(preference.getCustomerInfo())) .with("preferences",
					 * gson.toJson(preference.getCustomerContacts())) .with("lastUpdateTime",
					 * gson.toJson(DateTime.now())));
					 */
					
					prevPremiseAccount = preference.getCustomerInfo().getPremiseNumber() + preference.getCustomerInfo().getAccountNumber();
					

					prevPreference.setCustomerContacts(preference.getCustomerContacts());
					prevPreference.setCustomerInfo(preference.getCustomerInfo());
					recCount ++;
				}
				
				if (!prevPreference.getCustomerInfo().getAccountNumber().contentEquals("00000000000")) {
					itemList.add(new Item()
							.withPrimaryKey("premiseNumber", prevPreference.getCustomerInfo().getPremiseNumber(),
									"accountNumber", prevPreference.getCustomerInfo().getAccountNumber())
							.with("CustomerInfo", gson.toJson(prevPreference.getCustomerInfo()))
							.with("preferences", gson.toJson(prevPreference.getCustomerContacts()))
							.with("lastUpdateTime", DateTime.now().toString()));
				}
				
				if (itemList.size() > 0) {
					tw.withItemsToPut(itemList);
					outcome = dynamoDB.batchWriteItem(tw);
					do {
						// Confirm no unprocessed items
						Map<String, List<WriteRequest>> unprocessedItems = outcome.getUnprocessedItems();

						if (outcome.getUnprocessedItems().size() == 0) {
							System.out.println("All items processed.");
						} else {
							System.out.println("Gathering unprocessed items...");
							outcome = dynamoDB.batchWriteItemUnprocessed(unprocessedItems);
						}
					} while (outcome.getUnprocessedItems().size() > 0);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

}
