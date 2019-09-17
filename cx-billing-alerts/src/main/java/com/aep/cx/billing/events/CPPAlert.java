package com.aep.cx.billing.events;

import org.joda.time.DateTime;

import com.aep.cx.utils.time.CustomStdDateTimeDeserializer;
import com.aep.cx.utils.time.CustomStdDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class CPPAlert extends Header{

	private double currentRemainingAmount;
	
	@JsonDeserialize(using = CustomStdDateTimeDeserializer.class)
	@JsonSerialize(using = CustomStdDateTimeSerializer.class)
	private DateTime dueDate;
	
	@JsonDeserialize(using = CustomStdDateTimeDeserializer.class)
	@JsonSerialize(using = CustomStdDateTimeSerializer.class)
	private DateTime nextBillDate;
	
	private String prenoteStatusCode;
	private String toBeCurrNext;
	private String bankNumber;

	public CPPAlert() {}

	public void setRemainingAmount(double currentRemainingAmount) {
		this.currentRemainingAmount = currentRemainingAmount;
	}

	public double getRemainingAmount() {
		return currentRemainingAmount;
	}

	public void setNextBillDate(DateTime nextBillDate) {
		this.nextBillDate = nextBillDate;
	}

	public DateTime getNextBillDate() {
		return nextBillDate;
	}

	public void setDueDate(DateTime dueDate) {
		this.dueDate = dueDate;
	}

	public DateTime getDueDate() {
		return dueDate;
	}

	public void setPrenoteStatusCode(String prenoteStatusCode) {
		this.prenoteStatusCode = prenoteStatusCode;
	}

	public String getPrenoteStatusCode() {
		return prenoteStatusCode;
	}

	public void setToBeCurrNext(String toBeCurrNext) {
		this.toBeCurrNext = toBeCurrNext;
	}

	public String getToBeCurrNext() {
		return toBeCurrNext;
	}

	public void setBankNumber(String bankNumber) {
		this.bankNumber = bankNumber;
	}

	public String getBankNumber() {
		return bankNumber;
	}
}