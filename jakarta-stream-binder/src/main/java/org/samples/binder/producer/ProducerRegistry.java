package org.samples.binder.producer;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.samples.binder.Producer;

@ApplicationScoped
@Slf4j
public class ProducerRegistry {

    private final Map<String, Producer<?>> producers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Producer<T> getOrCreate(String channelName, Class<T> payloadType, Supplier<Producer<T>> supplier) {
        log.info("Obtaining Producer for channel '{}' y payload {}", channelName, payloadType);
        return (Producer<T>) producers.computeIfAbsent(channelName, key -> supplier.get());
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