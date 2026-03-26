package org.samples.saga.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OutboxService {

    private final ObjectMapper mapper;

    @Inject
    private OutboxRepository repository;

    public OutboxService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    public void enqueue(String channel, Object payload) {
        try {
            String payloadJson = mapper.writeValueAsString(payload);
            OutboxEvent event = OutboxEvent.builder()
                .channel(channel)
                .payload(payloadJson)
                .payloadType(payload.getClass().getName())
                .status("PENDING")
                .attempts(0)
                .build();
            repository.save(event);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to enqueue outbox event", e);
        }
    }

}
