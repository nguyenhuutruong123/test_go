package com.yes4all.common.annotation;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.yes4all.common.utils.CommonDataUtil;

import java.io.IOException;

public class DoubleDeserializer extends JsonDeserializer<Double> {
    @Override
    public Double deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonToken currentToken = jsonParser.getCurrentToken();
        if (JsonToken.VALUE_STRING.equals(currentToken)) {
            String value = jsonParser.getValueAsString();
            return CommonDataUtil.isNotEmpty(value) ? jsonParser.getValueAsDouble() : null;
        }
        return jsonParser.readValueAs(Double.class);
    }
}
