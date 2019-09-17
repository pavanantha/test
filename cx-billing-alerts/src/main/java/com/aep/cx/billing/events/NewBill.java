package com.aep.cx.billing.events;

import org.joda.time.DateTime;

public class NewBill {

        private Header header;
        private DateTime billDate;
        private double billAmount;
        private DateTime billDueDate;
        private double pastDueAmount;
        private double totalAmountDue;

        public NewBill()
        {

        }

        public void setHeader(Header header) {
            this.header = header;
        }
    
        public Header getHeader() {
            return header;
        }

        public void setBillAmount(double billAmount) {
            this.billAmount = billAmount;
        }
    
        public double getBillAmount() {
            return billAmount;
        }

        public void setBillDate(DateTime billDate) {
            this.billDate = billDate;
        }
    
        public DateTime getBillDate() {
            return billDate;
        }

        public void setBillDueDate(DateTime billDueDate) {
            this.billDueDate = billDueDate;
        }
    
        public DateTime getBillDueDate() {
            return billDueDate;
        }

        public double getPastDueAmount() {
            return pastDueAmount;
        }

        public void setPastDueAmount(double pastDueAmount) {
            this.pastDueAmount = pastDueAmount;
        }

        public double getTotalAmountDue() {
            return totalAmountDue;
        }

        public void setTotalAmountDue(double totalAmountDue) {
            this.totalAmountDue = totalAmountDue;
        }

}