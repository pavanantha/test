package com.aep.cx.billing.events;

import org.joda.time.DateTime;

public class ReconnectCreated {

        private Header header;
        private DateTime reconnectDateAndTime;
        private double reconnectFee;
        private double pendingPayment;


        public ReconnectCreated()
        {

        }

        public void setHeader(Header header) {
            this.header = header;
        }
    
        public Header getHeader() {
            return header;
        }

        public void setReconnectDateAndTime(DateTime reconnectDateAndTime) {
            this.reconnectDateAndTime = reconnectDateAndTime;
        }
    
        public DateTime getReconnectDateAndTime() {
            return reconnectDateAndTime;
        }

        public void setReconnectedFee(double reconnectFee) {
            this.reconnectFee = reconnectFee;
        }
    
        public double getReconnectFee() {
            return reconnectFee;
        }

        public void setPendingPayment(double pendingPayment) {
            this.pendingPayment = pendingPayment;
        }
    
        public double getPendingPayment() {
            return pendingPayment;
        }
}