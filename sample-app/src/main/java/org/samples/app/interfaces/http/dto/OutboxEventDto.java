package org.samples.app.interfaces.http.dto;

import java.util.Date;

import org.samples.saga.outbox.OutboxEventEntity.Status;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class OutboxEventDto {

    private String id;

    private String correlationId;

    private String channel;

    private String payloadType;

    private String payload;

    private Status status;

    private int attempts;

    private String dlqReason;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private Date createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private Date sentAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private Date nextAttemptAt;

}
