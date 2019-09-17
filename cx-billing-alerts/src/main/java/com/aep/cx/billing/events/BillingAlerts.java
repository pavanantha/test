package com.aep.cx.billing.events;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.aep.cx.preferences.dao.CustomerInfo;
import com.aep.cx.utils.enums.AlertNames;
import com.aep.cx.utils.enums.MessageType;
import com.aep.cx.utils.enums.MessageTypeGlobal;
import com.aep.cx.utils.opco.OperatingCompanyManager;
import com.aep.cx.utils.opco.OperatingCompanyV2;

public class BillingAlerts extends Header {
	public String[] parts = new String[] {};

	public Payment payment;
	public BillDue billDue;
	private DisconnectNotice discNotice;
	private Disconnected disconnected;
	public ReconnectCreated reconnectCreated;
	private Reconnected reconnected;
	public CPPAlert cpp;
	public ReturnCheck returnCheck;
	public InHouseWelcome inHouse;
	public NewBill newBill;
	public OrderTracking orderTracking;

	private NumberFormat format = new DecimalFormat("#.00");
	private DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");
	private DateTimeFormatter fmtToString = DateTimeFormat.forPattern("yyyy-MM-dd");
	private DateTime defaultDateTime = formatter.parseDateTime("2015-01-01-01.01.01.000001");
	private DateTimeFormatter shortFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

	public BillingAlerts() {
	}

	public BillingAlerts(String mqMessage) {

		String[] parts = null;
		this.setCustomerInfo(null);

		this.setAlertType(mqMessage.substring(54, 54 + 16).trim().toLowerCase().equals("pending-payment") ? "payment"
				: mqMessage.substring(54, 54 + 16).trim().toLowerCase());

		OperatingCompanyManager cm = new OperatingCompanyManager();
		Map<String, OperatingCompanyV2> opcoBuild = cm.BuildOperatingCompanyMap();
		OperatingCompanyV2 opcoDetails = opcoBuild.get(mqMessage.substring(70, 70 + 11).trim().substring(0, 2));

		this.setWebID(null);
		this.setEndPoint(null);
		System.out.println("alert type:"+this.getAlertType());
		if (!this.getAlertType().equals("cpp-enrollment") && !this.getAlertType().equals("cpp-acknowledge")
				&& !this.getAlertType().equals("cpp-update") && !this.getAlertType().equals("cpp-cancel")
				&& !this.getAlertType().equals("insp-complete") && !this.getAlertType().equals("ordr-complete")) {
			// this.setRegion(mqMessage.substring(0,2));
			// this.setTdat(mqMessage.substring(46, 46 + 8));

			this.setPremiseNumber(mqMessage.substring(81, 81 + 9).trim());
			this.setAccountNumber(mqMessage.substring(70, 70 + 11).trim());
			this.setEzid(null != mqMessage.substring(90, 90 + 17).trim() ? mqMessage.substring(90, 90 + 17).trim()
					: "unknown");
			this.setEndPoint(mqMessage.substring(107, 107 + 320).trim());
			this.setWebID(mqMessage.substring(427, 427 + 320).trim());
			this.setAlertDetails(mqMessage.substring(747, 747 + 5).trim());
			// this.setOPCO(new OperatingCompany(this.getAccountNumber().substring(0,2)));
			// this.setLearnMoreLink(opcoDetails.getOpcoSite() +
			// opcoDetails.getManageAlertUrl());
		} else {
			parts = mqMessage.split("\\|");
			//this.setRegion(parts[0].substring(0, 2));
			//this.setTdat(parts[0].substring(46, 8 + 46).trim());
			this.setAccountNumber(parts[1]);
			opcoDetails = opcoBuild.get(this.getAccountNumber().trim().substring(0,2)); 
			this.setEzid("");
			this.setEndPoint("");
			this.setWebID("");
			this.setAlertDetails("");
			this.setPremiseNumber("");

		}

		this.setMacssID("MCSPYM");
		this.setExternalID("XATPYM");

		StringBuilder messageHist = null;
		StringBuilder emailContent = null;
		
		double totalAmount = 0.00;
		double pastDueAmount = 0.00;

		switch (this.getAlertType()) {
		case "payment":
			payment = new Payment();

			payment.setOTC(mqMessage.substring(1014, 1049).trim());
			payment.setPaymentAmount(Double.parseDouble(mqMessage.substring(997, 1010).trim()));
			payment.setPaymentDate(mqMessage.substring(987, 997).trim().length() > 0
					? DateTime.parse(mqMessage.substring(987, 997).trim())
					: defaultDateTime);
			payment.setPaymentSource(mqMessage.substring(1010, 1014).trim());

			messageHist = new StringBuilder("&Payment:");
			messageHist.append(format.format(payment.getPaymentAmount()));
			messageHist.append("&Payment Date:");
			messageHist.append(fmtToString.print(payment.getPaymentDate()));

			this.setMacssEmailContent(messageHist.toString());

			// emailContent = new
			// StringBuilder(opcoDetails.getOpcoSite()+opcoDetails.getLearnMoreLinkUrl());
			emailContent = new StringBuilder();
			// emailContent.append(',');
			emailContent.append(format.format(payment.getPaymentAmount()));
			emailContent.append(',');
			emailContent.append(fmtToString.print(defaultDateTime));
			emailContent.append(",0.00,0.00,");
			emailContent.append(fmtToString.print(payment.getPaymentDate()));
			emailContent.append(',');
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getPayMyBillUrl());
			emailContent.append(',');
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getAssistAgencyLink());
			emailContent.append(',');
			emailContent.append("0.00");
			emailContent.append(',');
			emailContent.append(',');
			emailContent.append(',');
			emailContent.append(',');

			this.setEmailContent(emailContent.toString());

			payment.setEmailContent(emailContent.toString().trim());
			payment.setMacssEmailContent(messageHist.toString().trim());
			payment.setAccountNumber(getAccountNumber().trim());
			payment.setAlertType(getAlertType().trim().toLowerCase());
			payment.setEzid(getEzid().trim());
			payment.setExternalID(getExternalID().trim());
			payment.setMacssID(getMacssID().trim());
			payment.setWebID(null != getWebID().trim() ? getWebID().trim() : null);
			payment.setEndPoint(null != getEndPoint().trim() ? getEndPoint().trim() : null);
			payment.setPremiseNumber(getPremiseNumber().trim());

			payment.setMacssID("MCSPYM");
			payment.setExternalID("XATPYM");

			// payment.setHeader(header);
			break;
		case "bill-due":
			billDue = new BillDue();
						
			billDue.setBillAmount(Double.parseDouble(mqMessage.substring(1082, 13 + 1082).trim()));
			billDue.setBillDueDate(mqMessage.substring(1095, 10 + 1095).trim().length() > 0 ? DateTime.parse(mqMessage.substring(1095, 10 + 1095).trim()) : defaultDateTime);
			billDue.setTotalAmount(Double.parseDouble(mqMessage.substring(1143, 13 + 1143).trim()));
			billDue.setPastDueAmount(Double.parseDouble(mqMessage.substring(1130, 13 + 1130).trim()));
			billDue.setPendingPaymentAmount(mqMessage.substring(913, 13 + 913).trim().length() > 0 ? Double.parseDouble(mqMessage.substring(913, 13 + 913).trim()) : 0);

			String cppcustomerL = mqMessage.substring(911, 912) == null ? "" : mqMessage.substring(911, 912);
			billDue.setCPPCustomer(cppcustomerL.toLowerCase().contains("y") ? "y" : "n");

			String budgetBillAniversary = mqMessage.substring(912, 913) == null ? "" : mqMessage.substring(912, 913);
			billDue.setBudgetBillAniversary(budgetBillAniversary.toLowerCase().contains("b") ? "b" : "a");

			//this.macssEmailContent = "&Bill Amount:" + format.format(this.billAmount) + "&Bill Due Date:" + this.billDueDate + "&Total Amount Due:" + format.format(this.totalAmount) + "&Past Due:" + format.format(this.pastDueAmount);
			
			messageHist = new StringBuilder("&Bill Amount:");
			messageHist.append(format.format(billDue.getBillAmount()));
			messageHist.append("&Bill Due Date:");
			messageHist.append(fmtToString.print(billDue.getBillDueDate()));
			messageHist.append("&Total Amount Due:");
			messageHist.append(format.format(billDue.getTotalAmount()));
			messageHist.append("&Past Due:");
			messageHist.append(format.format(billDue.getPastDueAmount()));
			this.setMacssEmailContent(messageHist.toString());
			
			emailContent = new StringBuilder(format.format(billDue.getBillAmount()));
			emailContent.append(',');
			emailContent.append(fmtToString.print(billDue.getBillDueDate()));
			emailContent.append(',');
			emailContent.append(format.format(billDue.getTotalAmount()));
			emailContent.append(',');
			emailContent.append(format.format(billDue.getPastDueAmount()));
			emailContent.append(',');
			emailContent.append("2001-01-01");
			emailContent.append(',');
			emailContent.append(opcoDetails.getOpcoSite()); 
			emailContent.append(opcoDetails.getPayMyBillUrl());
			emailContent.append(","); 
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getAssistAgencyLink());
			emailContent.append(","); 
			emailContent.append(format.format(billDue.getPendingPaymentAmount()));
			emailContent.append(","); 
			emailContent.append(billDue.getCPPCustomer());
			emailContent.append(",");
			emailContent.append(billDue.getBudgetBillAniversary());
			emailContent.append(","); 
			//emailContent.append(format.format(this.returnCheckProcessFee));
			emailContent.append(","); 
			//emailContent.append(this.returnCheckReason);
			
			this.setEmailContent(emailContent.toString());
			
			
			billDue.setEmailContent(emailContent.toString().trim());
			billDue.setMacssEmailContent(messageHist.toString().trim());
			billDue.setAccountNumber(getAccountNumber().trim());
			billDue.setAlertType(getAlertType().trim().toLowerCase());
			billDue.setEzid(getEzid().trim());
			billDue.setExternalID(getExternalID().trim());
			billDue.setMacssID(getMacssID().trim());
			billDue.setWebID(null != getWebID().trim()? getWebID().trim(): null);
			billDue.setEndPoint(null != getEndPoint().trim()? getEndPoint().trim(): null);
			billDue.setPremiseNumber(getPremiseNumber().trim());
			break;
		case "disc-notice":
			discNotice = new DisconnectNotice();
			discNotice.setAccountNumber(getAccountNumber());
			discNotice.setPremiseNumber(getPremiseNumber());
			discNotice.setEzid(getEzid());
			discNotice.setAlertType(getAlertType());
			discNotice.setWebID(null != getWebID() ? getWebID() : "");
			discNotice.setEndPoint(null != getEndPoint() ? getEndPoint() : "");
			discNotice.setExternalID(getExternalID());
			discNotice.setMacssID(getMacssID());
			discNotice.setDisconnectAmount(Double.parseDouble(mqMessage.substring(1105, 1118).trim()));
			discNotice.setPendingPaymentAmount(mqMessage.substring(913, 913 + 13).trim().length() > 0 ? Double.parseDouble(mqMessage.substring(913, 913 + 13).trim()) : 0);

			discNotice.setDisconnectAmount(discNotice.getDisconnectAmount() > discNotice.getPendingPaymentAmount()
					? discNotice.getDisconnectAmount() - discNotice.getPendingPaymentAmount()
					: 0);
			discNotice.setBillDueDate(mqMessage.substring(1118, 1118 + 10).trim().length() > 0
					? shortFormatter.parseDateTime((mqMessage.substring(1118, 1118 + 10).trim()))
					: defaultDateTime);

			discNotice.setPastDueAmount(discNotice.getPastDueAmount() > discNotice.getPendingPaymentAmount()
					? discNotice.getPastDueAmount() - discNotice.getPendingPaymentAmount()
					: 0);
			discNotice.setTotalAmount(discNotice.getTotalAmount() > discNotice.getPendingPaymentAmount()
					? discNotice.getTotalAmount() - discNotice.getPendingPaymentAmount()
					: 0);

			// round up
			// discNotice.setDisconnectAmount(Math.ceil(discNotice.getDisconnectAmount() * 100) / 100);
			// discNotice.setPastDueAmount(Math.ceil(discNotice.getPastDueAmount() * 100) / 100);
			// discNotice.setTotalAmount(Math.ceil(discNotice.getTotalAmount() * 100) / 100);

			discNotice.setDisconnectAmount(discNotice.getDisconnectAmount());
			discNotice.setPastDueAmount(discNotice.getPastDueAmount());
			discNotice.setTotalAmount(discNotice.getTotalAmount());

			messageHist = new StringBuilder();
			messageHist.append("&Total Amount Due:");
			messageHist.append(format.format(discNotice.getTotalAmount()));
			messageHist.append("&Past Due:");
			messageHist.append(format.format(discNotice.getPastDueAmount()));
			messageHist.append("&Must Be Received By:");
			messageHist.append(fmtToString.print(discNotice.getBillDueDate()));
			messageHist.append("&Minimum Amount:");
			
			this.setMacssEmailContent(messageHist.toString());
			
			discNotice.setMacssEmailContent(getMacssEmailContent());

			emailContent = new StringBuilder();
			emailContent.append(format.format(discNotice.getDisconnectAmount()));
			emailContent.append(',');
			emailContent.append(fmtToString.print(discNotice.getBillDueDate()));
			emailContent.append(',');
			emailContent.append(discNotice.getTotalAmount());
			emailContent.append(',');
			emailContent.append(format.format(discNotice.getPastDueAmount()));
			emailContent.append(',');
			emailContent.append("2001-01-01");
			emailContent.append(',');
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getPayMyBillUrl());
			emailContent.append(',');
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getAssistAgencyLink());
			emailContent.append(",");
			emailContent.append(format.format(discNotice.getPendingPaymentAmount()));
			emailContent.append(",");
			emailContent.append(",");
			emailContent.append(",");
			emailContent.append(",");
			this.setEmailContent(emailContent.toString());

			discNotice.setEmailContent(getEmailContent());

			break;
		case "return-check":
			returnCheck = new ReturnCheck();
			returnCheck.setProcessFee(mqMessage.substring(926, 13 + 926).trim().length() > 0 ? Double.parseDouble(mqMessage.substring(926, 13 + 926).trim()) : 0);
			returnCheck.setReason(mqMessage.substring(939, 25 + 939).trim().length() > 0 ? mqMessage.substring(939, 25 + 939).trim() : "unknown");
			returnCheck.setPaymentAmount(mqMessage.substring(997, 13 + 997).trim().length() > 0 ? Double.parseDouble(mqMessage.substring(997, 13 + 997).trim()) : 0);
	
			messageHist = new StringBuilder("&Payment:");
			messageHist.append(format.format(returnCheck.getPaymentAmount()));
			messageHist.append("&Return Check Reason: ");
			messageHist.append(returnCheck.getReason());
			messageHist.append("&Return Check Process Fee: ");
			messageHist.append(format.format(returnCheck.getProcessFee()));

			//String date = payment.getPaymentDate().ToString("yyyy-MM-dd");
			//this.macssEmailContent =  +  +  + this.returnCheckReason + "&Return Check Process Fee: " + format.format(this.returnCheckProcessFee);
			
			emailContent = new StringBuilder(format.format(returnCheck.getPaymentAmount()));
			emailContent.append(",2001-01-01,0.00,0.00,2001-01-01,");
			
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getPayMyBillUrl());
			emailContent.append(',');
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getAssistAgencyLink());
			emailContent.append(',');
			emailContent.append("0.00");
			//emailContent.append(format.format(this.pendingPaymentAmount));
			emailContent.append(',');
			//emailContent.append(this.cppCustomer);
			emailContent.append(',');
			//emailContent.append(this.budgetBillAniversary);
			emailContent.append(',');
			emailContent.append(format.format(returnCheck.getProcessFee()));
			emailContent.append(',');
			//emailContent.append(this.returnCheckReason);
			this.setEmailContent(emailContent.toString());

			//returnCheck.setHeader(header);
			//this.emailContent = this.learnMoreLink +  +  + "," +  + "," +  + "," + "0.00" + "," +  + "," + this.opcoDetails.getOpcoSite() + this.opcoDetails.getPayMyBillUrl() + "," + this.opcoDetails.getOpcoSite() + this.opcoDetails.getAssistAgencyLink() + "," + format.format(this.pendingPaymentAmount) + "," + this.cppCustomer + "," + this.budgetBillAniversary + "," + format.format(this.returnCheckProcessFee) + "," + this.returnCheckReason;
			
			returnCheck.setEmailContent(emailContent.toString().trim());
			returnCheck.setMacssEmailContent(messageHist.toString().trim());
			returnCheck.setAccountNumber(getAccountNumber().trim());
			returnCheck.setAlertType(getAlertType().trim().toLowerCase());
			returnCheck.setEzid(getEzid().trim());
			returnCheck.setExternalID(getExternalID().trim());
			returnCheck.setMacssID(getMacssID().trim());
			returnCheck.setWebID(null != getWebID().trim()? getWebID().trim(): null);
			returnCheck.setEndPoint(null != getEndPoint().trim()? getEndPoint().trim(): null);
			returnCheck.setPremiseNumber(getPremiseNumber().trim());

			break;
		case "disconnected":

			disconnected = new Disconnected();
			
			disconnected.setAccountNumber(getAccountNumber());
			disconnected.setPremiseNumber(getPremiseNumber());
			disconnected.setEzid(getEzid());
			disconnected.setAlertType(getAlertType());
			disconnected.setWebID(null != getWebID() ? getWebID() : "");
			disconnected.setEndPoint(null != getEndPoint() ? getEndPoint() : "");
			disconnected.setExternalID(getExternalID());
			disconnected.setMacssID(getMacssID());
			
			disconnected.setDisconnectedAmount(mqMessage.substring(476, 476 + 13).trim().length() > 0
					? Double.parseDouble(mqMessage.substring(476, 476 + 13).trim())
					: 0);
			disconnected.setDisconnectedDate(mqMessage.substring(450, 450 + 26).trim().length() > 0
					? formatter.parseDateTime(mqMessage.substring(450, 450 + 26).trim())
					: defaultDateTime);

			messageHist = new StringBuilder("&Must Be Received By:");
			messageHist.append(fmtToString.print(disconnected.getDisconnectedDate()));
			messageHist.append("&Minimum Amount:");
			messageHist.append(format.format(disconnected.getDisconnectedAmount()));
			this.setMacssEmailContent(messageHist.toString());
			
			disconnected.setMacssEmailContent(getMacssEmailContent());
			
			emailContent = new StringBuilder();
			emailContent.append(format.format(disconnected.getDisconnectedAmount()));
			emailContent.append(',');
			emailContent.append(fmtToString.print(disconnected.getDisconnectedDate()));
			emailContent.append(',');
			emailContent.append(format.format(totalAmount));
			emailContent.append(',');
			emailContent.append(format.format(pastDueAmount));
			emailContent.append(',');
			emailContent.append("2001-01-01");
			emailContent.append(',');
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getPayMyBillUrl());
			emailContent.append(',');
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getAssistAgencyLink());
			emailContent.append(",");
			emailContent.append(format.format(disconnected.getDisconnectedAmount()));
			emailContent.append(",");
			//Customer
			emailContent.append(",");//Customer
			//budgetBillAniversary
			emailContent.append(",");
			//returnCheckProcessFee
			emailContent.append(",");
			//returnCheckReason
			this.setEmailContent(emailContent.toString());
			disconnected.setEmailContent(getEmailContent());

			break;
		case "reconnected":

			reconnected = new Reconnected();
			
			reconnected.setAccountNumber(getAccountNumber());
			reconnected.setPremiseNumber(getPremiseNumber());
			reconnected.setEzid(getEzid());
			reconnected.setAlertType(getAlertType());
			reconnected.setWebID(null != getWebID() ? getWebID() : "");
			reconnected.setEndPoint(null != getEndPoint() ? getEndPoint() : "");
			reconnected.setExternalID(getExternalID());
			reconnected.setMacssID(getMacssID());
			
			reconnected.setReconnectFee(mqMessage.substring(427, 427 + 13).trim().length() > 0
					? Double.parseDouble(mqMessage.substring(427, 427 + 13).trim())
					: 0);
			reconnected.setReconnectDateAndTime(mqMessage.substring(440, 440 + 26).trim().length() > 0
					? formatter.parseDateTime(mqMessage.substring(440, 440 + 26).trim())
					: defaultDateTime);

			messageHist = new StringBuilder("&Reconnect Date:");
			messageHist.append(fmtToString.print(reconnected.getReconnectDateAndTime()));
			messageHist.append("&Reconnect Fee:" );
			messageHist.append(format.format(reconnected.getReconnectFee()));
			this.setMacssEmailContent(messageHist.toString());
			
			reconnected.setMacssEmailContent(getMacssEmailContent());

			/*reconnected.setPendingPaymentAmount(mqMessage.substring(913, 913 +
			13).trim().length() > 0 ? Double.parseDouble(mqMessage.substring(913, 913 +
			13).trim()) : 0);*/
			
			emailContent = new StringBuilder();
			emailContent.append(format.format(reconnected.getReconnectFee()));
			emailContent.append(',');
			emailContent.append(fmtToString.print(reconnected.getReconnectDateAndTime()));
			emailContent.append(',');
			emailContent.append(format.format(totalAmount));
			emailContent.append(',');
			emailContent.append(format.format(pastDueAmount));
			emailContent.append(',');
			emailContent.append("2001-01-01");
			emailContent.append(',');
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getPayMyBillUrl());
			emailContent.append(',');
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getAssistAgencyLink());
			emailContent.append(",");
			emailContent.append(format.format(reconnected.getReconnectFee()));
			emailContent.append(",");
			//Customer
			emailContent.append(",");
			//budgetBillAniversary
			emailContent.append(",");
			//returnCheckProcessFee
			emailContent.append(",");
			//returnCheckReason
			this.setEmailContent(emailContent.toString());
			reconnected.setEmailContent(getEmailContent());
			
			break;
		case "recon-created":

			reconnectCreated = new ReconnectCreated();
			reconnectCreated.setPendingPayment(mqMessage.substring(913, 913 + 13).trim().length() > 0
					? Double.parseDouble(mqMessage.substring(913, 913 + 13).trim())
					: 0);

			// this.macssEmailContent = "&Payment Received On:" +
			// fmtToString.print(this.reconnectTime) + "&Payment Amount:" +
			// format.format(this.pendingPaymentAmount);
			// this.emailContent = this.opcoDetails.getReconnectURL() + "," +
			// format.format(this.reconnectFee) + "," +
			// fmtToString.print(this.reconnectTime) + "," + format.format(this.totalAmount)
			// + "," + format.format(this.pastDueAmount) + "," + "2001-01-01" + "," +
			// this.opcoDetails.getOpcoSite() + this.opcoDetails.getPayMyBillUrl() + "," +
			// this.opcoDetails.getOpcoSite() + this.opcoDetails.getAssistAgencyLink() + ","
			// + format.format(this.pendingPaymentAmount) + "," + this.cppCustomer + "," +
			// this.budgetBillAniversary + "," + format.format(this.returnCheckProcessFee) +
			// "," + this.returnCheckReason;
			// reconnectCreated.setHeader(header);
			break;
		case "inhouse-welcome":

			inHouse = new InHouseWelcome();

			// this.macssEmailContent = "&Next Bill Date:" +
			// fmtToString.print(this.billCreationDate);
			// this.emailContent = this.learnMoreLink + "," +
			// format.format(payment.getPaymentAmount()) + "," +
			// fmtToString.print(this.billCreationDate) + "," +
			// format.format(this.totalAmount) + "," + "0.00," + "2001-01-01" + "," +
			// this.opcoDetails.getOpcoSite() + this.opcoDetails.getPayMyBillUrl() + "," +
			// this.opcoDetails.getOpcoSite() + this.opcoDetails.getAssistAgencyLink() + ","
			// + "0.00," + this.cppCustomer + "," + this.budgetBillAniversary + "," +
			// "0.00," + this.returnCheckReason;
			// inHouse.setHeader(header);
			break;
		case "new-bill":

			newBill = new NewBill();

			// this.macssEmailContent = "&Bill Amount:" + format.format(this.billAmount) +
			// "&Bill Due Date:" + fmtToString.print(this.billDueDate) + "&Total Amount
			// Due:" + format.format(this.totalAmount) + "&Past Due:" +
			// format.format(this.pastDueAmount);
			// this.emailContent = this.learnMoreLink + "," + format.format(this.billAmount)
			// + "," + fmtToString.print(this.billDueDate) + "," +
			// format.format(this.totalAmount) + "," + format.format(this.pastDueAmount) +
			// "," + "2001-01-01" + "," + this.opcoDetails.getOpcoSite() +
			// this.opcoDetails.getPayMyBillUrl() + "," + this.opcoDetails.getOpcoSite() +
			// this.opcoDetails.getAssistAgencyLink() + "," +
			// format.format(this.pendingPaymentAmount) + "," + this.cppCustomer + "," +
			// this.budgetBillAniversary + "," + format.format(this.returnCheckProcessFee) +
			// "," + this.returnCheckReason;
			// newBill.setHeader(header);
			break;
		case "cpp-acknowledge":

			cpp = new CPPAlert();
			cpp.setPrenoteStatusCode(parts[5].trim().length() > 0 ? parts[5].trim() : " ");
			cpp.setBankNumber(parts[7].trim().length() > 0 ? parts[7].trim() : " ");
			cpp.setToBeCurrNext("");

			emailContent = new StringBuilder(format.format(cpp.getRemainingAmount()));
			emailContent.append(",");
			emailContent.append("2001-01-01");
			emailContent.append(",");
			emailContent.append("0.00");
			emailContent.append(",0.00,");
			emailContent.append("2001-01-01");
			emailContent.append(",");
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getPayMyBillUrl());
			emailContent.append(",");
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getAssistAgencyLink());
			emailContent.append(",0.00,,,0.00,");
			//emailContent.append(cpp.getBankNumber());

			if (cpp.getPrenoteStatusCode().equals("F"))
			{
				this.setMacssEmailContent("&Unable to finish AutoPay Activation.");
			
			}

			cpp.setEmailContent(emailContent.toString().trim());
			cpp.setAccountNumber(getAccountNumber().trim());
			cpp.setAlertType(getAlertType().trim().toLowerCase());
			cpp.setEzid(getEzid().trim());
			cpp.setExternalID(getExternalID().trim());
			cpp.setMacssID(getMacssID().trim());
			cpp.setWebID(null != getWebID().trim()? getWebID().trim(): null);
			cpp.setEndPoint(null != getEndPoint().trim()? getEndPoint().trim(): null);
			cpp.setPremiseNumber(getPremiseNumber().trim());
			
			cpp.setMacssEmailContent(this.getMacssEmailContent());

			break;
		case "cpp-update":

			cpp = new CPPAlert();
		
			cpp.setPrenoteStatusCode(parts[5].trim().length() > 0 ? parts[5].trim() : " ");
			cpp.setBankNumber(parts[7].trim().length() > 0 ? parts[7].trim() : " ");

			emailContent = new StringBuilder(format.format(cpp.getRemainingAmount()));
			emailContent.append(",");
			emailContent.append("2001-01-01");
			emailContent.append(",");
			emailContent.append("0.00");
			emailContent.append(",0.00,");
			emailContent.append("2001-01-01");
			emailContent.append(",");
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getPayMyBillUrl());
			emailContent.append(",");
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getAssistAgencyLink());
			emailContent.append(",0.00,,,0.00,");
			emailContent.append(cpp.getBankNumber());

			cpp.setEmailContent(emailContent.toString().trim());
			cpp.setAccountNumber(getAccountNumber().trim());
			cpp.setAlertType(getAlertType().trim().toLowerCase());
			cpp.setEzid(getEzid().trim());
			cpp.setExternalID(getExternalID().trim());
			cpp.setMacssID(getMacssID().trim());
			cpp.setWebID(null != getWebID().trim()? getWebID().trim(): null);
			cpp.setEndPoint(null != getEndPoint().trim()? getEndPoint().trim(): null);
			cpp.setPremiseNumber(getPremiseNumber().trim());
			
			this.setMacssEmailContent("&Unable to finish AutoPay Activation.");
			cpp.setMacssEmailContent(this.getMacssEmailContent());
			break;
		case "cpp-cancel":

			cpp = new CPPAlert();
		
			cpp.setPrenoteStatusCode(parts[5].trim().length() > 0 ? parts[5].trim() : " ");
			cpp.setBankNumber(parts[7].trim().length() > 0 ? parts[7].trim() : " ");
			
			emailContent = new StringBuilder(format.format(cpp.getRemainingAmount()));
			emailContent.append(",");
			emailContent.append(fmtToString.print(cpp.getDueDate()));
			emailContent.append(",");
			emailContent.append(format.format(cpp.getRemainingAmount()));
			emailContent.append(",0.00,");
			emailContent.append(fmtToString.print(cpp.getNextBillDate()));
			emailContent.append(",");
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getPayMyBillUrl());
			emailContent.append(",");
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getAssistAgencyLink());
			emailContent.append(",0.00,,,0.00,");
			emailContent.append(cpp.getBankNumber());

			cpp.setEmailContent(emailContent.toString().trim());
			cpp.setAccountNumber(getAccountNumber().trim());
			cpp.setAlertType(getAlertType().trim().toLowerCase());
			cpp.setEzid(getEzid().trim());
			cpp.setExternalID(getExternalID().trim());
			cpp.setMacssID(getMacssID().trim());
			cpp.setWebID(null != getWebID().trim()? getWebID().trim(): null);
			cpp.setEndPoint(null != getEndPoint().trim()? getEndPoint().trim(): null);
			cpp.setPremiseNumber(getPremiseNumber().trim());
			
			this.setMacssEmailContent("");
			cpp.setMacssEmailContent(this.getMacssEmailContent());
			break;
		case "cpp-enrollment":

			cpp = new CPPAlert();
		
			cpp.setDueDate(parts[2].trim().length() > 0 ?  shortFormatter.parseDateTime(parts[2].trim()) : defaultDateTime);
			cpp.setRemainingAmount(parts[3].trim().length() > 0 ? Double.parseDouble(parts[3].trim()) : 0);
			cpp.setNextBillDate(parts[4].trim().length() > 0 ? shortFormatter.parseDateTime(parts[4].trim()) : defaultDateTime);
			cpp.setPrenoteStatusCode(parts[5].trim().length() > 0 ? parts[5].trim() : " ");
			cpp.setToBeCurrNext(parts[6].trim().length() > 0 ? parts[6].trim() : " ");
			cpp.setBankNumber(parts[7].trim().length() > 0 ? parts[7].trim() : " ");


			messageHist = new StringBuilder();
			if (cpp.getRemainingAmount() > 0 && cpp.getToBeCurrNext().equals("C"))
			{
				messageHist.append("&Current bill with due amount:");
				messageHist.append(format.format(cpp.getRemainingAmount()));
				messageHist.append("&Due Date:");
				messageHist.append(fmtToString.print(cpp.getDueDate()));
			}
			else if (cpp.getRemainingAmount() > 0 && cpp.getToBeCurrNext().equals("N"))
			{
				messageHist.append("&Next bill with current due amount:");
				messageHist.append(format.format(cpp.getRemainingAmount()));
				messageHist.append("&Due Date:");
				messageHist.append(fmtToString.print(cpp.getDueDate()));
			}
			else
			{
				messageHist.append("&Next Bill"); 
				messageHist.append("&Next read date:");
				messageHist.append(fmtToString.print(cpp.getDueDate()));
			}


			emailContent = new StringBuilder(format.format(cpp.getRemainingAmount()));
			emailContent.append(",");
			emailContent.append(fmtToString.print(cpp.getDueDate()));
			emailContent.append(",");
			emailContent.append(format.format(cpp.getRemainingAmount()));
			emailContent.append(",0.00,");
			emailContent.append(fmtToString.print(cpp.getNextBillDate()));
			emailContent.append(",");
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getPayMyBillUrl());
			emailContent.append(",");
			emailContent.append(opcoDetails.getOpcoSite());
			emailContent.append(opcoDetails.getAssistAgencyLink());
			emailContent.append(",0.00,,,0.00,");
			emailContent.append(cpp.getBankNumber());

			this.setMacssEmailContent(messageHist.toString());
			cpp.setEmailContent(emailContent.toString().trim());
			cpp.setMacssEmailContent(messageHist.toString().trim());
			cpp.setAccountNumber(getAccountNumber().trim());
			cpp.setAlertType(getAlertType().trim().toLowerCase());
			cpp.setEzid(getEzid().trim());
			cpp.setExternalID(getExternalID().trim());
			cpp.setMacssID(getMacssID().trim());
			cpp.setWebID(null != getWebID().trim()? getWebID().trim(): null);
			cpp.setEndPoint(null != getEndPoint().trim()? getEndPoint().trim(): null);
			cpp.setPremiseNumber(getPremiseNumber().trim());
			//cpp.setHeader(header);
		

			break;

		case "ordr-complete":
		case "insp-complete":
			orderTracking = new OrderTracking();
			orderTracking.setAccountNumber(this.getAccountNumber());
			orderTracking.setAlertType(this.getAlertType());
			orderTracking.setOrderDate(parts[4].trim().length() > 0 ?  shortFormatter.parseDateTime(parts[4].trim()) : defaultDateTime);
			orderTracking.setOrderNumber(parts[2].trim());
			orderTracking.setOrderType(parts[3].trim());
			orderTracking.setCompletedRequirements(parts[5].trim());
			orderTracking.setPendingRequirements(parts[6].trim());
			orderTracking.setExternalID(MessageType.XATTEX.toString());
			orderTracking.setMacssID(MessageType.MCSTEX.toString());
			break;

			
		default:
			AlertNames alertName = new AlertNames(this.getAlertType());
			if (alertName.GlobalAlertType.toString().contentEquals(MessageTypeGlobal.SUBSCRIPTION.toString())) {
				CustomerInfo cinfo = new CustomerInfo();
				cinfo.setCity(mqMessage.substring(832, 832 + 20).trim());
				cinfo.setStreetAddress(mqMessage.substring(852, 852 + 20).trim());
				cinfo.setState(mqMessage.substring(872, 872 + 2).trim());
				cinfo.setZipCode(mqMessage.substring(874, 874 + 5).trim());
				cinfo.setName(
						mqMessage.substring(762, 762 + 35).trim() + " " + mqMessage.substring(797, 795 + 35).trim());
				this.setCustomerInfo(cinfo);
				this.setExternalID(MessageType.XATFTP.toString());
				this.setMacssID(MessageType.MCSXAT.toString());
				emailContent = new StringBuilder();
				emailContent.append("," + DateTime.now().toString(DateTimeFormat.forPattern("M/d/yyyy h:mm:ss a")));
				emailContent.append(",,,,");
				emailContent.append(this.getAlertDetails().trim());
				this.setEmailContent(emailContent.toString());
				messageHist = new StringBuilder();
				messageHist.append(
						"&MaxCustOut:0&Cause:unknown&ETR:2001-01-01T00:00:00.0000000&Duration:unknown&OutageChannel:");
				messageHist.append(this.getAlertDetails().trim());
				messageHist.append("&CreditChannel:NNNN");
				this.setMacssEmailContent(messageHist.toString());
			}
			break;
		}
	}

	public DisconnectNotice getDiscNotice() {
		return discNotice;
	}

	public void setDiscNotice(DisconnectNotice discNotice) {
		this.discNotice = discNotice;
	}

	public Disconnected getDisconnected() {
		return disconnected;
	}

	public void setDisconnected(Disconnected disconnected) {
		this.disconnected = disconnected;
	}

	public Reconnected getReconnected() {
		return reconnected;
	}

	public void setReconnected(Reconnected reconnected) {
		this.reconnected = reconnected;
	}
}
