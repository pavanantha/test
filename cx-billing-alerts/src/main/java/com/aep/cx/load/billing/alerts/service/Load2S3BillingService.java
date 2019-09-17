package com.aep.cx.load.billing.alerts.service;

import java.util.ArrayList;
import java.util.TimeZone;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.aep.cx.billing.alerts.enrollment.events.EnrollmentAlerts;
import com.aep.cx.billing.events.BillDue;
import com.aep.cx.billing.events.BillingAlerts;
import com.aep.cx.billing.events.CPPAlert;
import com.aep.cx.billing.events.DisconnectNotice;

import com.aep.cx.billing.events.Disconnected;
import com.aep.cx.billing.events.InHouseWelcome;
import com.aep.cx.billing.events.NewBill;
import com.aep.cx.billing.events.OrderTracking;
import com.aep.cx.billing.events.Payment;
import com.aep.cx.billing.events.ReconnectCreated;
import com.aep.cx.billing.events.Reconnected;
import com.aep.cx.billing.events.ReturnCheck;
import com.aep.cx.macss.customer.subscriptions.MACSSIntegrationWrapper;
import com.aep.cx.utils.alerts.aws.s3.loader.LoadData2S3;
import com.aep.cx.utils.enums.AlertNames;
import com.aep.cx.utils.enums.MessageTypeGlobal;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.SdkBaseException;
import com.amazonaws.SdkClientException;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClient;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Load2S3BillingService {
	
	public static final AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
	
	public static void Load2s3byAlert(ArrayList<MACSSIntegrationWrapper> alertData) {
		String bucketName = null;
		String bucketKey = null;

		try {
			ArrayList<NewBill> newBillList = new ArrayList<>();
			ArrayList<InHouseWelcome> inhouseWelcomeList = new ArrayList<>();
			ArrayList<CPPAlert> cppAlertList =  new ArrayList<>();
			ArrayList<BillDue> billDueAlertsList = new ArrayList<>();
			ArrayList<Payment> paymentAlertsList = new ArrayList<Payment>();
			ArrayList<ReturnCheck> returnCheckList = new ArrayList<>();
			ArrayList<DisconnectNotice> disconnectNoticeList = new ArrayList<>();
			ArrayList<Disconnected> disconnectedList = new ArrayList<>();
			ArrayList<Reconnected> reconnectedList = new ArrayList<>();
			ArrayList<ReconnectCreated> reconnectCreatedList = new ArrayList<>();
			ArrayList<OrderTracking> orderTrackingList = new ArrayList<OrderTracking>();
			
			ArrayList<EnrollmentAlerts> enrollmentAlertsList = new ArrayList<EnrollmentAlerts>();
			
			for (MACSSIntegrationWrapper macssIntegrationWrapper : alertData) {

				BillingAlerts alertContent = new BillingAlerts(macssIntegrationWrapper.getMessageString());


				switch (alertContent.getAlertType().trim().toLowerCase()) {
				case "new-bill":
					newBillList.add(alertContent.newBill);
					break;
				case "inhouse-welcome":
					inhouseWelcomeList.add(alertContent.inHouse);
					break;				
				case "bill-due":
					billDueAlertsList.add(alertContent.billDue);
					break;
				case "payment":
					paymentAlertsList.add(alertContent.payment);
					break;
				case "return-check":
					returnCheckList.add(alertContent.returnCheck);
					break;				
				case "disc-notice":
					disconnectNoticeList.add(alertContent.getDiscNotice());
					break;
				case "disconnected":
					disconnectedList.add(alertContent.getDisconnected());
					break;
				case "reconnected":
					reconnectedList.add(alertContent.getReconnected());
					break;
				case "recon-create":
					reconnectCreatedList.add(alertContent.reconnectCreated);
					break;	
				case "insp-complete":
				case "ordr-complete":
					orderTrackingList.add(alertContent.orderTracking);
					break;	
				default:
				AlertNames alertName = new AlertNames(alertContent.getAlertType().trim().toLowerCase());
				System.out.println("alert name: " + alertName);
				if (alertName.GlobalAlertType.toString().contentEquals(MessageTypeGlobal.SUBSCRIPTION.toString())) {
					enrollmentAlertsList.add(new EnrollmentAlerts(alertContent));
				} 
				
				if (alertName.GlobalAlertType.toString().contentEquals(MessageTypeGlobal.CPPENROLLMENT.toString())) {
					System.out.println("CPP type");
					cppAlertList.add(alertContent.cpp);

				}
				
				if (alertName.GlobalAlertType.toString().contentEquals(MessageTypeGlobal.NONE.toString())) {
					System.out.println("Not ready for Alert=" + alertContent.getAlertType());
				}
				break;
				}
			}
				
			LoadData2S3 load2s3 = new LoadData2S3();

			DateTime dateTime = DateTime.now()
					.toDateTime(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/New_York")));
			String folder = dateTime.toString("yyyy") + "/" + dateTime.toString("MMM") + "/" + dateTime.toString("dd");
			String suffix =  UUID.randomUUID().toString().replaceAll("-", "")+"_" + dateTime.toString("HHmmssSSS");
			
			if (paymentAlertsList.size() > 0) {
				bucketName = System.getenv("ALERTS_RAW_PAYMENT");
				bucketKey = dateTime.toString("yyyyMMdd") + "/payment_" +suffix;
				
				load2s3.loadData(bucketName, bucketKey, paymentAlertsList);
			}
			
			if (billDueAlertsList.size() > 0) {
				bucketName = System.getenv("ALERTS_RAW_BILLDUE");
				bucketKey = dateTime.toString("yyyyMMdd") + "/billdue_" + suffix;
				
				load2s3.loadData(bucketName, bucketKey, billDueAlertsList);
			}
			
			if (enrollmentAlertsList.size() > 0) {
				bucketName = System.getenv("ALERTS_RAW_ENROLLMENT");
				bucketKey = dateTime.toString("yyyyMMdd") + "/enrollment_" + suffix;
				load2s3.loadData(bucketName, bucketKey, enrollmentAlertsList);
			}
			
			if (cppAlertList.size() > 0) {
				bucketName = System.getenv("ALERTS_RAW_CPP_ENROLLMENT");
				bucketKey = dateTime.toString("yyyyMMdd") + "/cppEnrollment_" + suffix;
				 load2s3.loadData(bucketName, bucketKey, cppAlertList);
			}
			
			if (returnCheckList.size() > 0) {
				bucketName = System.getenv("ALERTS_RAW_RETURN_CHECK");
				bucketKey = dateTime.toString("yyyyMMdd") + "/returnCheck_" + suffix;
				load2s3.loadData(bucketName, bucketKey, returnCheckList);
			}

			
			if(disconnectNoticeList.size() > 0) {
				bucketName = System.getenv("ALERTS_RAW_DISCONNECT_NOTICE");
				bucketKey =  folder + "/disconnectNotice_" + suffix;
				load2s3.loadData(bucketName, bucketKey, disconnectNoticeList);
			}
			
			if(disconnectedList.size() > 0) {
				bucketName = System.getenv("ALERTS_RAW_DISCONNECTED");
				bucketKey =  folder + "/disconnected_"   + suffix;
				load2s3.loadData(bucketName, bucketKey, disconnectedList);
			}
			
			if(reconnectedList.size() > 0) {
				bucketName = System.getenv("ALERTS_RAW_RECONNECTED");
				bucketKey =  folder + "/reconnected_"   + suffix;
				load2s3.loadData(bucketName, bucketKey, reconnectedList);
      }

			if(orderTrackingList.size() > 0) {
				bucketName = System.getenv("ALERTS_RAW_ORDER_TRACKING");
				bucketKey =  folder + "/ordertracking_"   + suffix;
				load2s3.loadData(bucketName, bucketKey, orderTrackingList);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	
	public static void Load2s3byAlertHeader (ArrayList<MACSSIntegrationWrapper> alertData, String headerName) {
		for (MACSSIntegrationWrapper macssIntegrationWrapper : alertData) {
		
			System.out.println("alerts content:"+macssIntegrationWrapper.getMessageString());
		}
        ObjectMapper mapper = new ObjectMapper();
        String lambdaToInvoke = null;
                
        try {
			
			String bucketName = null;
			DateTime dateTime = DateTime.now().toDateTime(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/New_York")));
			String bucketKey = dateTime.toString("yyyyMMdd")+ "/" + headerName + "_" +UUID.randomUUID().toString().replaceAll("-", "")+"_"+ dateTime.toString("HHmmssSSS");
			
			switch (headerName.toLowerCase()) {
			case "billing":
				bucketName = System.getenv("BILLING_RAW_BUCKET");
				lambdaToInvoke = System.getenv("LAMBDA_TO_INVOKE_BILLING");
				
				break;
			case "profile":
				bucketName = System.getenv("PROFILE_RAW_BUCKET");
				lambdaToInvoke = System.getenv("LAMBDA_TO_INVOKE_BILLING");
				break;
			default:
				bucketName = System.getenv("BILLING_RAW_UNKNOWN_BUCKET");
				break;
			}
			
			for (MACSSIntegrationWrapper macssIntegrationWrapper : alertData) {
				
				System.out.println("alerts content:"+macssIntegrationWrapper.getMessageString());
			}
			if (null != bucketName) {
				LoadData2S3 load2s3 = new LoadData2S3();
				load2s3.loadData(bucketName, bucketKey, alertData);
			}
			
			if (null != lambdaToInvoke) {
				InvokeRequest request = new InvokeRequest().withFunctionName(lambdaToInvoke)
						.withInvocationType("Event");
				request.withPayload(mapper.writeValueAsString(alertData));
				ClientConfiguration config = new ClientConfiguration();
				config.setConnectionTimeout(1000);
				config.setClientExecutionTimeout(1000 * 60);
				AWSLambdaClient asyncClient = (AWSLambdaClient) AWSLambdaAsyncClientBuilder.standard().withRegion(Regions.US_EAST_1).withClientConfiguration(config).build();	
				InvokeResult invResult = asyncClient.invoke(request);
			}
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}