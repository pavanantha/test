/*package com.aep.cx.billing.events;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.aep.cx.billing.alerts.enrollment.events.EnrollmentAlerts;

public class OldBillingAlerts {

        public EnrollmentAlerts header;
        public DateTime paymentDate;
        public Double paymentAmount;
        public String paymentSource;
        public String otcData;
        public String CPPFlag;
        public Double CPPAmount;
        public DateTime achDate;
        public DateTime billCreationDate;
        public Double billAmount;
        public DateTime billDueDate;
        public Double disconnectAmount;
        public DateTime disconnectDate;
        public String billType;
        public String billSendType;
        public Double pastDueAmount;
        public Double totalAmount;
        public DateTime eventTimeStamp;
        public String budgetBillAnniversary;
        public double pendingPaymentAmount;

        public BillingAlerts()
        {
        }

        public BillingAlerts(String mqMessage)
        {
			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
			DateTime defaultDateTime = formatter.parseDateTime("2001-01-01");
            this.header = new EnrollmentAlerts(mqMessage);
            this.paymentDate = mqMessage.substring(987, 10).trim().length() > 0 ? formatter.parseDateTime(mqMessage.substring(987, 10).trim()) : defaultDateTime;
            this.paymentAmount = Double.parseDouble(mqMessage.substring(997, 13).trim());
            this.paymentSource = mqMessage.substring(1010, 4).trim();
            this.otcData = mqMessage.substring(1014, 35).trim();
            this.CPPAmount = Double.parseDouble(mqMessage.substring(1049, 13).trim());
            this.achDate = mqMessage.substring(1062, 10).trim().length() > 0 ? formatter.parseDateTime(mqMessage.substring(1062, 10).trim()) : defaultDateTime;
            this.billCreationDate = mqMessage.substring(1072, 10).trim().length() > 0 ? formatter.parseDateTime(mqMessage.substring(1072, 10).trim()) : defaultDateTime;
            this.billAmount = Double.parseDouble(mqMessage.substring(1082, 13).trim());
            this.billDueDate = mqMessage.substring(1095, 10).trim().length() > 0 ? formatter.parseDateTime(mqMessage.substring(1095, 10).trim()) : defaultDateTime;
            this.disconnectAmount = Double.parseDouble(mqMessage.substring(1105, 13).trim());
            this.disconnectDate = mqMessage.substring(1118, 10).trim().length() > 0 ? formatter.parseDateTime(mqMessage.substring(1118, 10).trim()) : defaultDateTime;
            this.billType = mqMessage.substring(1128, 1).trim();
            this.billSendType = mqMessage.substring(1129, 1).trim();
            this.pastDueAmount = Double.parseDouble(mqMessage.substring(1130, 13).trim());
            
            this.totalAmount = Double.parseDouble(mqMessage.substring(1143, 13).trim());
            this.eventTimeStamp = mqMessage.substring(1156, 26).trim().length() > 0 ? formatter.parseDateTime(mqMessage.substring(1156, 26).trim()) : defaultDateTime;
            

            // Please do not remove the code below until we figure out build sms and build email for payment alerts.
		/*
		 * switch (this.header.alertType.ToLower()) { case "payment":
		 * this.header.macssEmailContent = "&Payment:" +
		 * this.paymentAmount.ToString("f2") + "&Payment Date:" +
		 * this.paymentDate.ToString("yyyy-MM-ddTH:mm:ss.fffffff");
		 * this.header.emailContent = this.header.learnMoreLink + "," +
		 * this.paymentAmount + "," + formatter.parseDateTime("2001-01-01") + "," +
		 * "0.0" + "," + "0.0" + "," + this.paymentDate + "," +
		 * this.header.opcoDetails.opcoSite + this.header.opcoDetails.PayMyBillUrl + ","
		 * + this.header.opcoDetails.opcoSite + this.header.opcoDetails.AssistAgencyLink
		 * + "," + this.header.pendingPaymentAmount + "," + this.header.cppCustomer +
		 * "," + this.header.budgetBillAniversary; break; case "bill-due":
		 * this.header.macssEmailContent = "&Bill Amount:" +
		 * this.billAmount.ToString("f2") + "&Bill Due Date:" +
		 * this.billDueDate.ToString("yyyy-MM-ddTH:mm:ss.fffffff") +
		 * "&Total Amount Due:" + this.totalAmount.ToString("f2") + "&Past Due:" +
		 * this.pastDueAmount.ToString("f2"); this.header.emailContent =
		 * this.header.learnMoreLink + "," + this.billAmount + "," + this.billDueDate +
		 * "," + this.totalAmount + "," + this.pastDueAmount + "," +
		 * formatter.parseDateTime("2001-01-01") + "," +
		 * this.header.opcoDetails.opcoSite + this.header.opcoDetails.PayMyBillUrl + ","
		 * + this.header.opcoDetails.opcoSite + this.header.opcoDetails.AssistAgencyLink
		 * + "," + this.header.pendingPaymentAmount + "," + this.header.cppCustomer +
		 * "," + this.header.budgetBillAniversary; break; case "disc-notice":
		 * this.disconnectAmount = this.disconnectAmount >
		 * this.header.pendingPaymentAmount ? this.disconnectAmount -
		 * this.header.pendingPaymentAmount : 0; this.pastDueAmount = this.pastDueAmount
		 * > this.header.pendingPaymentAmount ? this.pastDueAmount -
		 * this.header.pendingPaymentAmount : 0; this.totalAmount = this.totalAmount >
		 * this.header.pendingPaymentAmount ? this.totalAmount -
		 * this.header.pendingPaymentAmount : 0;
		 * 
		 * // round up
		 * 
		 * this.disconnectAmount = Math.Ceiling(this.disconnectAmount * 100) / 100;
		 * this.pastDueAmount = Math.Ceiling(this.pastDueAmount * 100) / 100;
		 * this.totalAmount = Math.Ceiling(this.totalAmount * 100) / 100;
		 * 
		 * 
		 * this.header.macssEmailContent = "&Total Amount Due:" +
		 * this.totalAmount.ToString("f2") + "&Past Due:" +
		 * this.pastDueAmount.ToString("f2") + "&Must Be Received By:" +
		 * this.disconnectDate.ToString("yyyy-MM-ddTH:mm:ss.fffffff") +
		 * "&Minimum Amount:" + this.disconnectAmount.ToString("f2");
		 * this.header.emailContent = this.header.learnMoreLink + "," +
		 * this.disconnectAmount + "," + this.disconnectDate + "," + this.totalAmount +
		 * "," + this.pastDueAmount + "," + formatter.parseDateTime("2001-01-01") + ","
		 * + this.header.opcoDetails.opcoSite + this.header.opcoDetails.PayMyBillUrl +
		 * "," + this.header.opcoDetails.opcoSite +
		 * this.header.opcoDetails.AssistAgencyLink + "," +
		 * this.header.pendingPaymentAmount + "," + this.header.cppCustomer + "," +
		 * this.header.budgetBillAniversary;;
		 * 
		 * break; }
		 
        }
 } */