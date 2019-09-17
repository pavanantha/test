package com.aep.cx.billing.events;

import org.joda.time.DateTime;
import com.aep.cx.utils.time.CustomStdDateTimeDeserializer;
import com.aep.cx.utils.time.CustomStdDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class BillDue extends Header {

        @JsonDeserialize(using = CustomStdDateTimeDeserializer.class)
	    @JsonSerialize(using = CustomStdDateTimeSerializer.class)
        private DateTime billDueDate;
        
        private double billAmount;
        private double totalAmount;
        private double pastDueAmount;
        private double pendingPaymentAmount;
        private String cppCustomer;
        private String budgetBillAniversary;

        public BillDue()
        {

        }

        public void setBillDueDate(DateTime billDueDate) {
            this.billDueDate = billDueDate;
        }
    
        public DateTime getBillDueDate() {
            return billDueDate;
        }

        public void setTotalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
        }
    
        public double getTotalAmount() {
            return totalAmount;
        }

        public void setCPPCustomer(String cppCustomer) {
            this.cppCustomer = cppCustomer;
        }
    
        public String getCPPCustomer() {
            return cppCustomer;
        }

        public void setBudgetBillAniversary(String budgetBillAniversary) {
            this.budgetBillAniversary = budgetBillAniversary;
        }
    
        public String getBudgetBillAniversary() {
            return budgetBillAniversary;
        }

        public void setPastDueAmount(double pastDueAmount) {
            this.pastDueAmount = pastDueAmount;
        }
    
        public double getPastDueAmount() {
            return pastDueAmount;
        }

        public void setBillAmount(double billAmount) {
            this.billAmount = billAmount;
        }
    
        public double getBillAmount() {
            return billAmount;
        }

        public void setPendingPaymentAmount(double pendingPaymentAmount)
        {
            this.pendingPaymentAmount = pendingPaymentAmount;
        }

        public double getPendingPaymentAmount()
        {
            return pendingPaymentAmount;
        }
}