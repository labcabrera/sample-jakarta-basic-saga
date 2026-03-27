package org.samples.saga.outbox;

import org.samples.binder.DomainEvent;
import org.samples.binder.Message;
import org.samples.saga.outbox.OutboxEventEntity.Status;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class OutboxService {

    @Inject
    private OutboxRepository repository;

    @Inject
    private ObjectMapper mapper;

    public void enqueue(String channel, Object payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }
        log.info("Enqueuing outbox event for channel {} with payload {}", channel, payload.getClass().getSimpleName());
        try {
            String correlationId = getCorrelationId(payload);
            String payloadJson = mapper.writeValueAsString(payload);
            OutboxEventEntity event = OutboxEventEntity.builder()
                .correlationId(correlationId)
                .channel(channel)
                .payload(payloadJson)
                .payloadType(payload.getClass().getName())
                .status(Status.PENDING)
                .attempts(0)
                .build();
            repository.save(event);
        }
        catch (Exception e) {
            log.error("Failed to enqueue outbox event", e);
            throw new RuntimeException("Failed to enqueue outbox event", e);
        }
    }

    private String getCorrelationId(Object payload) {
        if (payload instanceof DomainEvent domainEvent) {
            return domainEvent.aggregateId();
        }
        else if (payload instanceof Message<?> message) {
            if (message.payload() instanceof DomainEvent domainEvent) {
                return domainEvent.aggregateId();
            }
        }
        log.warn("Could not extract aggregate ID from payload of type {}", payload.getClass().getName());
        return null;
    }

}
