package org.samples.binder.consumer;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.samples.binder.Consumer;

@ApplicationScoped
@Slf4j
public class ConsumerRegistry {

    private final Map<String, Consumer<?>> consumers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Consumer<T> getOrCreate(String channelName, Class<T> payloadType, Supplier<Consumer<T>> supplier) {
        log.info("Obtaining Consumer for channel '{}' and payload {}", channelName, payloadType);
        return (Consumer<T>) consumers.computeIfAbsent(channelName, key -> supplier.get());
    }

    @PreDestroy
    void shutdown() {
        consumers.values().forEach(c -> {
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        });
    }
}
