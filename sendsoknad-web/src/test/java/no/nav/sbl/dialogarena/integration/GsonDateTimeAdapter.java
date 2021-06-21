package no.nav.sbl.dialogarena.integration;

import com.google.gson.*;
import org.joda.time.DateTime;

import java.lang.reflect.Type;


public class GsonDateTimeAdapter implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {
    @Override
    public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new DateTime(json.getAsString());
    }

    @Override
    public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }
}
