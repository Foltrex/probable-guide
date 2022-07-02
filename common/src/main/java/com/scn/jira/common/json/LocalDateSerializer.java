package com.scn.jira.common.json;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.time.LocalDate;

public class LocalDateSerializer extends JsonSerializer<LocalDate> {

    @Override
    public void serialize(LocalDate value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value.toString());
        }
    }
}
