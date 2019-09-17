package com.aep.cx.billing.events;

import org.joda.time.DateTime;

import com.aep.cx.utils.time.CustomStdDateTimeDeserializer;
import com.aep.cx.utils.time.CustomStdDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisconnectNotice extends Header {
	
	@JsonDeserialize(using = CustomStdDateTimeDeserializer.class)
    @JsonSerialize(using = CustomStdDateTimeSerializer.class)
	private DateTime billDueDate;
	
	private double totalAmount;
	private double pastDueAmount;
	private double disconnectAmount;
	private double pendingPaymentAmount;

	public DisconnectNotice() {}

	public DateTime getBillDueDate() {
		return billDueDate;
	}

	public void setBillDueDate(DateTime billDueDate) {
		this.billDueDate = billDueDate;
	}

	public void setTotalAmount(double totalAmountDue) {
		this.totalAmount = totalAmountDue;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setPastDueAmount(double pastDueAmount) {
		this.pastDueAmount = pastDueAmount;
	}

	public double getPastDueAmount() {
		return pastDueAmount;
	}

	public void setDisconnectAmount(double disconnectAmount) {
		this.disconnectAmount = disconnectAmount;
	}

	public double getDisconnectAmount() {
		return disconnectAmount;
	}

	public void setPendingPaymentAmount(double pendingPaymentAmount) {
		this.pendingPaymentAmount = pendingPaymentAmount;
	}

	public double getPendingPaymentAmount() {
		return pendingPaymentAmount;
	}
}