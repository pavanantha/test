package com.aep.cx.utils.time;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class CustomStdDateTimeDeserializer extends StdDeserializer<DateTime>{

	private static final long serialVersionUID = -4786530145472747507L;

	protected CustomStdDateTimeDeserializer(Class<?> vc) {
		super(vc);
	}
	
	/*protected CustomStdDateDeserializer(Class<DateTime> dt) {
		super(dt);
	}*/

	public CustomStdDateTimeDeserializer() {
		this(null);
	}

	@Override
	public DateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		DateTimeFormatter df = DateTimeFormat.forPattern("yyyyMMddHHmm");
		String value = p.readValueAs(String.class);
		if(StringUtils.isEmpty(value)) {
			return null;
		}
		return DateTime.parse(value, df);
		//return DateTime.parse(p.readValueAs(String.class), df);
	}

}
