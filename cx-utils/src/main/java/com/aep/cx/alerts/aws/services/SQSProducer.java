// package com.aep.cx.alerts.aws.services;

// import com.aep.cx.utils.alerts.notification.msessages.BuildEmail;
// import com.aep.cx.utils.alerts.notification.msessages.BuildSMS;
// import com.aep.cx.utils.delivery.SMSDelivery;
// import com.aep.cx.utils.enums.QueueType;
// import com.amazonaws.AmazonClientException;
// import com.amazonaws.AmazonServiceException;
// import com.amazonaws.services.sqs.AmazonSQS;
// import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
// import com.amazonaws.services.sqs.model.CreateQueueRequest;
// import com.amazonaws.services.sqs.model.SendMessageRequest;

// public class SQSProducer {
// //public static void main(String[] args) {

// //public static void InsertMesage(List<String> message,String queueType) {
// public static void InsertMesage(Object data, QueueType queueType) {
// /*
// * Create a new instance of the builder with all defaults (credentials
// * and region) set automatically. For more information, see
// * Creating Service Clients in the AWS SDK for Java Developer Guide.
// */

// final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

// System.out.println("===============================================");
// System.out.println("Getting Started with Amazon SQS Standard Queues");
// System.out.println("===============================================\n");

// try {

// // Create a queue.
// System.out.println("Creating a new SQS queue called :" + queueType);
// final CreateQueueRequest smsQueueRequest = new
// CreateQueueRequest("SMSQueue");

// final CreateQueueRequest emailQueueRequest = new
// CreateQueueRequest("EmailQueue");

// final CreateQueueRequest messageHistoryQueueRequest = new
// CreateQueueRequest("CISQueue");

// final String smsQueueUrl = sqs.createQueue(smsQueueRequest).getQueueUrl();

// final String EmailQueueUrl =
// sqs.createQueue(emailQueueRequest).getQueueUrl();

// final String CISQueueUrl =
// sqs.createQueue(messageHistoryQueueRequest).getQueueUrl();

// if (queueType.equals(QueueType.EMAIL)) {
// BuildEmail email = (BuildEmail) data;
// for (String xat : email.getXatPayload()) {
// System.out.println("sending new message to SQS Email queue:" + xat);
// sqs.sendMessage(new SendMessageRequest(EmailQueueUrl, xat));
// }
// }

// if (queueType.equals(QueueType.TEXT)) {
// BuildSMS sms = (BuildSMS) data;

// StringBuilder builder = new StringBuilder();
// sms.getSmsPayload().forEach(load->{
// builder.append(load);
// });

// System.out.println("sending new message to SQS Email queue:" +
// sms.getSmsPayload().toString());
// sqs.sendMessage(new SendMessageRequest(smsQueueUrl, builder.toString()));
// SMSDelivery.CallI2SMS(builder.toString());
// }

// /* Iterator its = smsContacts.entrySet().iterator();
// while (its.hasNext()) {
// Map.Entry pair = (Map.Entry)its.next();
// System.out.println(pair.getKey() + " = " + pair.getValue());
// its.remove(); // avoids a ConcurrentModificationException
// }

// Iterator ite = emailContacts.entrySet().iterator();
// while (ite.hasNext()) {
// Map.Entry pair = (Map.Entry)ite.next();
// System.out.println(pair.getKey() + " = " + pair.getValue());
// ite.remove(); // avoids a ConcurrentModificationException
// }*/

// // Send a message.
// //System.out.println("Sending a message to MyQueue.\n");
// //sqs.sendMessage(new SendMessageRequest(smsQueueUrl,message.toString()));
// //"This is my message text."));

// } catch (final AmazonServiceException ase) {
// System.out.println("Caught an AmazonServiceException, which means " +
// "your request made it to Amazon SQS, but was " +
// "rejected with an error response for some reason.");
// System.out.println("Error Message: " + ase.getMessage());
// System.out.println("HTTP Status Code: " + ase.getStatusCode());
// System.out.println("AWS Error Code: " + ase.getErrorCode());
// System.out.println("Error Type: " + ase.getErrorType());
// System.out.println("Request ID: " + ase.getRequestId());
// } catch (final AmazonClientException ace) {
// System.out.println("Caught an AmazonClientException, which means " +
// "the client encountered a serious internal problem while " +
// "trying to communicate with Amazon SQS, such as not " +
// "being able to access the network.");
// System.out.println("Error Message: " + ace.getMessage());
// }
// }
// }
