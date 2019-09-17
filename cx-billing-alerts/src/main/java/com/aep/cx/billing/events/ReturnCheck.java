package com.aep.cx.billing.events;


public class ReturnCheck extends Header {

        private double processFee;
        private String reason;
        private double paymentAmount;

        public ReturnCheck()
        {

        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    
        public String getReason() {
            return reason;
        }

        public void setProcessFee(double processFee) {
            this.processFee = processFee;
        }
    
        public double getProcessFee() {
            return processFee;
        }

        public double getPaymentAmount()
        {
            return paymentAmount;
        }

        public void setPaymentAmount(double paymentAmount)
        {
            this.paymentAmount = paymentAmount;
        }
}