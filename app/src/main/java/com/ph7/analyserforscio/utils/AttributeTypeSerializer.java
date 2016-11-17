package com.ph7.analyserforscio.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.ph7.analyserforscio.models.scio.ScioModelAttribute;

import java.lang.reflect.Type;

public class AttributeTypeSerializer implements JsonSerializer<ScioModelAttribute.AttributeType> {
    @Override
    public JsonElement serialize(ScioModelAttribute.AttributeType src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
