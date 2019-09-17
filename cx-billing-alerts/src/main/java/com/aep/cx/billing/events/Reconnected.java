package com.aep.cx.billing.events;

import org.joda.time.DateTime;

import com.aep.cx.utils.time.CustomStdDateTimeDeserializer;
import com.aep.cx.utils.time.CustomStdDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reconnected extends Header {
	@JsonDeserialize(using = CustomStdDateTimeDeserializer.class)
    @JsonSerialize(using = CustomStdDateTimeSerializer.class)
	private DateTime reconnectDateAndTime;
	private double reconnectFee;

	public Reconnected() {
	}

	public void setReconnectDateAndTime(DateTime reconnectDateAndTime) {
		this.reconnectDateAndTime = reconnectDateAndTime;
	}

	public DateTime getReconnectDateAndTime() {
		return reconnectDateAndTime;
	}

	public double getReconnectFee() {
		return reconnectFee;
	}

	public void setReconnectFee(double reconnectFee) {
		this.reconnectFee = reconnectFee;
	}
}