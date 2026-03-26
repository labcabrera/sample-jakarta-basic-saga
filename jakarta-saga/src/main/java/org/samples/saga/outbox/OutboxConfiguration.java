package org.samples.saga.outbox;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;

//TODO read from config
@ApplicationScoped
@Getter
public class OutboxConfiguration {

    private int loopIntervalMs = 1000;
    private int batchSize = 10;
    private int waitOnErrorMs = 5000;
    private int maxAttempts = 5;
    private long baseBackoffMs = 1000L;
    private long maxBackoffMs = 60_000L;
}
