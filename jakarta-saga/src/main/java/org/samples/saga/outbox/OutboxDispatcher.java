package org.samples.saga.outbox;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.samples.binder.producer.ProducerFactory;
import org.samples.binder.SendResult;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageProducer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class OutboxDispatcher {

    @Inject
    private OutboxRepository repository;

    @Inject
    private ProducerFactory producerFactory;

    private volatile boolean running = true;

    private final ObjectMapper mapper;

    //TODO read from config
    private int LOOP_INTERVAL_MS = 1000;
    private int BATCH_SIZE = 10;
    private int WAIT_TIME_ON_ERROR_MS = 5000;

    public OutboxDispatcher() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void start() {
        Thread thread = new Thread(this::loop, "outbox-dispatcher");
        thread.setDaemon(true);
        thread.start();
    }

    private void loop() {
        while (running) {
            try {
                List<OutboxEventEntity> pending = repository.findPending(BATCH_SIZE);
                pending.forEach(this::handleEvent);
                Thread.sleep(LOOP_INTERVAL_MS);
            }
            catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                running = false;
            }
            catch (Exception e) {
                log.error("Outbox dispatcher error", e);
                try {
                    Thread.sleep(WAIT_TIME_ON_ERROR_MS);
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
            boolean claimed = repository.markSending(e.getId());
            if (!claimed) {
                return;
            }
            Class<?> cls = Class.forName(e.getPayloadType());
            Object payload = mapper.readValue(e.getPayload(), cls);

            ChannelConfig cfg = producerFactory.loadChannelConfig(e.getChannel(), cls);
            MessageProducer producer = producerFactory.createProducer(cfg, cls);
            CompletionStage<SendResult> cs = producer.send(payload);
            SendResult result = cs.toCompletableFuture().get(8, TimeUnit.SECONDS);
            String messageId = result.messageId();
            repository.markSent(e.getId(), messageId);
            log.info("Outbox event {} sent to {} messageId={}", e.getId(), e.getChannel(), messageId);
        }
        catch (Exception ex) {
            int attempts = e.getAttempts() + 1;
            repository.markFailed(e.getId(), attempts);
            log.warn("Failed to dispatch outbox event {}: {}", e.getId(), ex.getMessage());
        }
    }

}
