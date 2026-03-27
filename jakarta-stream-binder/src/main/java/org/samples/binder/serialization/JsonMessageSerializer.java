package org.samples.binder.serialization;

import java.nio.charset.StandardCharsets;

import org.samples.binder.Message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonMessageSerializer {

    private final ObjectMapper mapper;

    public JsonMessageSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> byte[] serialize(T payload) {
        if (payload instanceof Message) {
            Message<?> message = (Message<?>) payload;
            return json(message.payload());
        }
        return json(payload);
    }

    private byte[] json(Object payload) {
        try {
            String json = mapper.writeValueAsString(payload);
            return json.getBytes(StandardCharsets.UTF_8);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize message payload to JSON", e);
        }
    }

}
