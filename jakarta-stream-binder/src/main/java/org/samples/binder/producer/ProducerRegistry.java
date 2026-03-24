package org.samples.binder.producer;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.samples.binder.MessageProducer;

@ApplicationScoped
@Slf4j
public class ProducerRegistry {

    private final Map<String, MessageProducer<?>> producers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> MessageProducer<T> getOrCreate(String channelName, Class<T> payloadType, Supplier<MessageProducer<T>> supplier) {
        log.info("Obtaining Producer for channel '{}' y payload {}", channelName, payloadType);
        return (MessageProducer<T>) producers.computeIfAbsent(channelName, key -> supplier.get());
    }

    @PreDestroy
    void shutdown() {
        log.info("Shutting down ProducerRegistry, closing {} producers", producers.size());
        producers.values().forEach(p -> {
            try {
                p.close();
            }
            catch (Exception ignored) {
            }
        });
    }
}