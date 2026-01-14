package org.mrp.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public abstract class JsonObject {
    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        // register JavaTimeModule to handle Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }
}