package com.ph7.foodscan.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.ph7.foodscan.models.scio.ScioModelAttribute;

import java.lang.reflect.Type;

/**
 * Created by craigtweedy on 19/02/2016.
 */
public class AttributeTypeDeserializer implements JsonDeserializer<ScioModelAttribute.AttributeType> {

    @Override
    public ScioModelAttribute.AttributeType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        int key = json.getAsInt();
        return ScioModelAttribute.AttributeType.fromKey(key);
    }
}

