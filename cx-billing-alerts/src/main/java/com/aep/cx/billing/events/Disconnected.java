package com.aep.cx.billing.events;

import org.joda.time.DateTime;

import com.aep.cx.utils.time.CustomStdDateTimeDeserializer;
import com.aep.cx.utils.time.CustomStdDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Disconnected extends Header{

	private double disconnectedAmount;
	
	@JsonDeserialize(using = CustomStdDateTimeDeserializer.class)
    @JsonSerialize(using = CustomStdDateTimeSerializer.class)
	private DateTime disconnectedDate;

	public Disconnected() {}

	public void setDisconnectedDate(DateTime disconnectedDate) {
		this.disconnectedDate = disconnectedDate;
	}

	public DateTime getDisconnectedDate() {
		return disconnectedDate;
	}

	public void setDisconnectedAmount(double disconnectedAmount) {
		this.disconnectedAmount = disconnectedAmount;
	}

	public double getDisconnectedAmount() {
		return disconnectedAmount;
	}
}