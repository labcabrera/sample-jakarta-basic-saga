package org.samples.saga.outbox;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

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

    private String correlationId;

    private String channel;

    private String payloadType;

    @Lob
    private String payload;

    private Status status;

    private int attempts;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date sentAt;

    private String messageId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date nextAttemptAt;

    private String dlqReason;

    public static enum Status {
        PENDING, SENDING, SENT, FAILED, DLQ
    }

}
