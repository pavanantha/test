package com.aep.cx.utils.time;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

// we can use JsonSerializer<DateTime> instead of StdSerializer<DateTime and no need to define any constructor
public class CustomStdDateTimeSerializer extends StdSerializer<DateTime> {

	private static final long serialVersionUID = 5767144139299985110L;

	protected CustomStdDateTimeSerializer(StdSerializer<?> src) {
		super(src);
	}
	
	protected CustomStdDateTimeSerializer(Class<?> t, boolean dummy) {
		super(t, dummy);
	}
	
	protected CustomStdDateTimeSerializer(Class<DateTime> t) {
		super(t);
	}
	
	public CustomStdDateTimeSerializer() {
		super(DateTime.class);
	}

	@Override
	public void serialize(DateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmm");
		gen.writeString(formatter.print(value));
	}

}
