package org.samples.app.interfaces.http.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class OutboxEventDto {

    private String id;

    private String aggregateId;

    private String channel;

    private String payloadType;

    private String payload;

    private String status; // PENDING, SENDING, SENT, FAILED

    private int attempts;

    private Instant createdAt;

    private Instant sentAt;

    private String messageId;

    private Instant nextAttemptAt;

    private String dlqReason;

}
