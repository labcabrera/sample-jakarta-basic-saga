package org.samples.binder.serialization;

import java.nio.charset.StandardCharsets;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonMessageDeserializer {

    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] body, Class<T> payloadType) {
        if (body == null)
            return null;
        if (payloadType == String.class) {
            return (T) new String(body, StandardCharsets.UTF_8);
        }
        try (Jsonb jsonb = JsonbBuilder.create()) {
            String s = new String(body, StandardCharsets.UTF_8);
            return jsonb.fromJson(s, payloadType);
        }
        catch (Exception e) {
            log.warn("Failed to deserialize payload, falling back to toString", e);
            try {
                return (T) new String(body, StandardCharsets.UTF_8);
            }
            catch (Exception ex) {
                return null;
            }
        }
    }
}
