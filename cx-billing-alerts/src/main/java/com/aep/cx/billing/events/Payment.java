package com.aep.cx.billing.events;

import org.joda.time.DateTime;

import com.aep.cx.utils.time.CustomStdDateTimeDeserializer;
import com.aep.cx.utils.time.CustomStdDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class Payment extends Header{

        @JsonDeserialize(using = CustomStdDateTimeDeserializer.class)
        @JsonSerialize(using = CustomStdDateTimeSerializer.class)
        private DateTime paymentDate;
        private Double paymentAmount;
        private String paymentSource;
        private String OTC;
        
        public Payment() {

        }

        public void setPaymentDate(DateTime paymentDate) {
            this.paymentDate = paymentDate;
        }
    
        public DateTime getPaymentDate() {
            return paymentDate;
        }

        public void setPaymentAmount(double paymentAmount) {
            this.paymentAmount = paymentAmount;
        }
    
        public double getPaymentAmount() {
            return paymentAmount;
        }

        public void setPaymentSource(String paymentSource) {
            this.paymentSource = paymentSource;
        }
    
        public String getPaymentSource() {
            return paymentSource;
        }

        public void setOTC(String OTC) {
            this.OTC = OTC;
        }
    
        public String getOTC() {
            return OTC;
        }
}