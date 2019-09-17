package com.aep.cx.billing.events;

import org.joda.time.DateTime;

public class InHouseWelcome {

        private Header header;
        private DateTime nextBillDate;

        public InHouseWelcome()
        {

        }

        public void setHeader(Header header) {
            this.header = header;
        }
    
        public Header getHeader() {
            return header;
        }

        public void setNextBillDueDate(DateTime nextBillDate) {
            this.nextBillDate = nextBillDate;
        }
    
        public DateTime getNextBillDueDate() {
            return nextBillDate;
        }
}