package com.yes4all.common.annotation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.yes4all.common.utils.CommonDataUtil;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooleanDeserializer extends JsonDeserializer<Boolean> {
    protected static final List<String> TRUE_VALUE_LIST = Stream.of(new String[]{"YES", "ACTIVE", "TRUE", "PASS", "PASSED"})
        .collect(Collectors.toList());

    @Override
    public Boolean deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonToken currentToken = jsonParser.getCurrentToken();
        if (JsonToken.VALUE_TRUE.equals(currentToken)) {
            return true;
        } else if (JsonToken.VALUE_FALSE.equals(currentToken)) {
            return false;
        }
        String value = jsonParser.getValueAsString();
        return CommonDataUtil.isNotEmpty(value) && TRUE_VALUE_LIST.contains(value.toUpperCase());
    }
}
