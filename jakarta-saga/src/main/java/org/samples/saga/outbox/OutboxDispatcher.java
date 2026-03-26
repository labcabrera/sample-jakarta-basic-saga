package org.samples.saga.outbox;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.samples.binder.producer.ProducerFactory;
import org.samples.binder.SendResult;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageProducer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class OutboxDispatcher {

    @Inject
    private OutboxRepository repository;

    @Inject
    private ProducerFactory producerFactory;

    @Inject
    private OutboxConfiguration config;

    private volatile boolean running = true;
    private volatile Thread workerThread;

    private final ObjectMapper mapper;

    public OutboxDispatcher() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    public void onStart(@Observes @Initialized(ApplicationScoped.class) Object init) {
        log.info("Iniciando OutboxDispatcher");
        Thread thread = new Thread(this::loop, "outbox-dispatcher");
        thread.setDaemon(true);
        this.workerThread = thread;
        thread.start();
    }

    public void onStop(@Observes @Destroyed(ApplicationScoped.class) Object init) {
        log.info("Deteniendo OutboxDispatcher");
        running = false;
        Thread thread = this.workerThread;
        if (thread != null) {
            thread.interrupt();
        }
    }

    private void loop() {
        while (running) {
            try {
                List<OutboxEventEntity> pending = repository.findPending(config.getBatchSize());
                pending.forEach(this::handleEvent);
                Thread.sleep(config.getLoopIntervalMs());
            }
            catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                running = false;
            }
            catch (Exception ex) {
                log.error("Outbox dispatcher error", ex);
                try {
                    Thread.sleep(config.getWaitOnErrorMs());
                }
                catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void handleEvent(OutboxEventEntity e) {
        try {
            log.info("Handling event {}", e);
            boolean claimed = repository.markSending(e.getId());
            if (!claimed) {
                return;
            }
            Class<?> cls = Class.forName(e.getPayloadType());
            Object payload = mapper.readValue(e.getPayload(), cls);
            ChannelConfig cfg = producerFactory.loadChannelConfig(e.getChannel());
            MessageProducer producer = producerFactory.createProducer(cfg, cls);
            CompletionStage<SendResult> cs = producer.send(payload);
            SendResult result = cs.toCompletableFuture().get(8, TimeUnit.SECONDS);
            String messageId = result.messageId();
            repository.markSent(e.getId(), messageId);
            log.info("Outbox event {} sent to {} messageId={}", e.getId(), e.getChannel(), messageId);
        }
        catch (Exception ex) {
            log.error("Failed to dispatch outbox event {}", e.getId(), ex);
            int attempts = e.getAttempts() + 1;
            try {
                if (attempts >= config.getMaxAttempts()) {
                    repository.markDlq(e.getId(), attempts, ex.getMessage());
                    log.error("Outbox event {} moved to DLQ after {} attempts, channel={} reason={}", e.getId(), attempts, e.getChannel(),
                        ex.getMessage());
                }
                else {
                    // Exponential backoff capped to maxBackoffMs
                    long multiplier = (long) Math.pow(2, attempts - 1);
                    long backoffMs = Math.min(config.getBaseBackoffMs() * multiplier, config.getMaxBackoffMs());
                    Date nextAttempt = new Date(System.currentTimeMillis() + backoffMs);
                    repository.markFailed(e.getId(), attempts, nextAttempt);
                    log.warn("Failed to dispatch outbox event {} channel={} attempts={} nextAttempt={} - {}", e.getId(), e.getChannel(),
                        attempts, nextAttempt, ex.getMessage());
                }
            }
            catch (Exception repoEx) {
                log.error("Failed updating outbox event state for {}", e.getId(), repoEx);
            }
        }
    }

}
