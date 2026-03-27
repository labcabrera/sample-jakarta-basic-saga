package org.samples.binder.consumer;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.samples.binder.MessageConsumer;

@ApplicationScoped
@Slf4j
public class ConsumerRegistry {

    private final Map<String, MessageConsumer<?>> consumers = new ConcurrentHashMap<>();

    public <T> MessageConsumer<T> getOrCreate(String channelName, Class<T> payloadType, Supplier<MessageConsumer<T>> supplier) {
        log.info("Obtaining Consumer for channel '{}' and payload {}", channelName, payloadType);
        return (MessageConsumer<T>) consumers.computeIfAbsent(channelName, key -> supplier.get());
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
