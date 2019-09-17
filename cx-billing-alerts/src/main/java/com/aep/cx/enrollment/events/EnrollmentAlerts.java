package com.aep.cx.enrollment.events;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class EnrollmentAlerts {


        private String region ;
        private String tdat ;
        private String alertType ;
        private String accountNumber ;
        private String premiseNumber ;
        private String ezid ;
        private String endPoint ;
        private String webID ;
        private String AlertDetails ;

        private String cppCustomer ;
        private String budgetBillAniversary ;
        private double pendingPaymentAmount ;

        public EnrollmentAlerts() {
        }

        public EnrollmentAlerts(String mqMessage)
        {
			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmm");
			DateTime defaultDateTime = formatter.parseDateTime("200101010101");
			//msgNotification.setOutageEtrTime(dt);
			
            this.region = mqMessage.substring(0, 2).trim();
            this.region = "MP";
            this.tdat = mqMessage.substring(46, 8).trim();
            this.alertType = mqMessage.substring(54, 16).trim().toLowerCase().contentEquals("pending-payment") ? "PAYMENT" : mqMessage.substring(54, 16).trim();
            this.accountNumber = mqMessage.substring(70, 11).trim();
            this.premiseNumber = mqMessage.substring(81, 9).trim();
            this.ezid = mqMessage.substring(90, 17).trim();
            this.endPoint = mqMessage.substring(107, 320).trim();
            this.webID = mqMessage.substring(427, 320).trim();
            this.AlertDetails = mqMessage.substring(747, 80).trim();
            this.pendingPaymentAmount = mqMessage.substring(913, 13).trim().length() > 0 ? Double.parseDouble(mqMessage.substring(913, 13).trim()) : 0;

            String cppcustomerL = mqMessage.substring(911, 1) == null ? "" : mqMessage.substring(911, 1);
            this.cppCustomer = cppcustomerL.toLowerCase().contains("y") ? "y" : "n";

            String budgetBillAniversaryL = mqMessage.substring(912, 1) == null ? "" : mqMessage.substring(912, 1);
            this.budgetBillAniversary = budgetBillAniversaryL.toLowerCase().contains("y") ? "y" : "n";

        }

		public String getRegion() {
			return region;
		}

		public void setRegion(String region) {
			this.region = region;
		}

		public String getTdat() {
			return tdat;
		}

		public void setTdat(String tdat) {
			this.tdat = tdat;
		}

		public String getAlertType() {
			return alertType;
		}

		public void setAlertType(String alertType) {
			this.alertType = alertType;
		}

		public String getAccountNumber() {
			return accountNumber;
		}

		public void setAccountNumber(String accountNumber) {
			this.accountNumber = accountNumber;
		}

		public String getPremiseNumber() {
			return premiseNumber;
		}

		public void setPremiseNumber(String premiseNumber) {
			this.premiseNumber = premiseNumber;
		}

		public String getEzid() {
			return ezid;
		}

		public void setEzid(String ezid) {
			this.ezid = ezid;
		}

		public String getEndPoint() {
			return endPoint;
		}

		public void setEndPoint(String endPoint) {
			this.endPoint = endPoint;
		}

		public String getWebID() {
			return webID;
		}

		public void setWebID(String webID) {
			this.webID = webID;
		}

		public String getAlertDetails() {
			return AlertDetails;
		}

		public void setAlertDetails(String alertDetails) {
			AlertDetails = alertDetails;
		}

		public String getCppCustomer() {
			return cppCustomer;
		}

		public void setCppCustomer(String cppCustomer) {
			this.cppCustomer = cppCustomer;
		}

		public String getBudgetBillAniversary() {
			return budgetBillAniversary;
		}

		public void setBudgetBillAniversary(String budgetBillAniversary) {
			this.budgetBillAniversary = budgetBillAniversary;
		}

		public double getPendingPaymentAmount() {
			return pendingPaymentAmount;
		}

		public void setPendingPaymentAmount(double pendingPaymentAmount) {
			this.pendingPaymentAmount = pendingPaymentAmount;
		}
}
