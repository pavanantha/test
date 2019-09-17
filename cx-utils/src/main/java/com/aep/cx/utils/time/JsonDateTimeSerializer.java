package com.aep.cx.utils.time;

import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonDateTimeSerializer implements JsonDeserializer<DateTime>, JsonSerializer<DateTime>{

	public JsonDateTimeSerializer() {
		// TODO Auto-generated constructor stub
	}
	
	static final org.joda.time.format.DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmm");
		     // ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

	@Override
	public DateTime deserialize(JsonElement jelm, Type type, JsonDeserializationContext ctx)
			throws JsonParseException {
		return jelm.getAsString().length() == 0 ? null : DATE_TIME_FORMATTER.parseDateTime(jelm.getAsString());
	}

	@Override
	public JsonElement serialize(DateTime dt, Type arg1, JsonSerializationContext ctx) {
		return new JsonPrimitive(dt == null ? StringUtils.EMPTY : DATE_TIME_FORMATTER.print(dt)); 
	}

}
