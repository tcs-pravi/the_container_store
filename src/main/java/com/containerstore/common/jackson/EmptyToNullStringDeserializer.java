package com.containerstore.common.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

import java.io.IOException;

import static com.google.common.base.Strings.emptyToNull;

class EmptyToNullStringDeserializer extends StdScalarDeserializer<String> {

    private static final long serialVersionUID = 1L;

    EmptyToNullStringDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        return emptyToNull(StringDeserializer.instance.deserialize(jp, ctxt));
    }
}
