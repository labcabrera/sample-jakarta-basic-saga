package org.samples.saga.outbox;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbox_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEventEntity {

    @Id
    private String id;

    private String aggregateId;

    private String channel;

    private String payloadType;

    @Lob
    private String payload;

    private String status; // PENDING, SENDING, SENT, FAILED

    private int attempts;

    private Instant createdAt;

    private Instant sentAt;

    private String messageId;

    private Instant nextAttemptAt;

    private String dlqReason;

}
