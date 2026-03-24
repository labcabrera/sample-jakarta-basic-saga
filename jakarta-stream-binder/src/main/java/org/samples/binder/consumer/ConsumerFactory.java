package org.samples.binder.consumer;

import org.samples.binder.BinderConfiguration;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageConsumer;
import org.samples.binder.kafka.KafkaConsumerAdapter;
import org.samples.binder.rabbitmq.RabbitConsumerAdapter;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ConsumerFactory {

    private final BinderConfiguration config = new BinderConfiguration();

    public <T> MessageConsumer<T> createConsumer(ChannelConfig cfg, Class<T> payloadType) {
        return (MessageConsumer<T>) switch (cfg.getType()) {
        case RABBITMQ -> new RabbitConsumerAdapter<T>(cfg, payloadType);
        case KAFKA -> new KafkaConsumerAdapter<T>(cfg, payloadType);
        };
    }

    public <T> ChannelConfig loadChannelConfig(String channelName, Class<T> payloadType) {
        log.info("Cargando configuración para canal {} con payload {}", channelName, payloadType.getName());
        String prefix = "messaging.channels." + channelName + ".";
        String type = config.getValue(prefix + "type", String.class);
        return switch (type.toLowerCase()) {
        case "kafka" -> ChannelConfig.kafka(
            channelName,
            config.getValue(prefix + "topic", String.class),
            getValue(prefix + "bootstrap.servers", "messaging.channels.kafka.bootstrap.servers", String.class),
            payloadType);
        case "rabbitmq" -> ChannelConfig.rabbit(
            channelName,
            config.getValue(prefix + "queue", String.class),
            getValue(prefix + "host", "messaging.channels.rabbit.host", String.class),
            getValue(prefix + "port", "messaging.channels.rabbit.port", Integer.class),
            getValue(prefix + "username", "messaging.channels.rabbit.username", String.class),
            getValue(prefix + "password", "messaging.channels.rabbit.password", String.class),
            payloadType);
        default -> throw new IllegalArgumentException(
            "Tipo de canal no soportado para " + channelName + ": " + type);
        };
    }

    private <T> T getValue(String key, String altKey, Class<T> clazz) {
        if (config.containsKey(key)) {
            return config.getValue(key, clazz);
        }
        else if (config.containsKey(altKey)) {
            return config.getValue(altKey, clazz);
        }
        else {
            throw new IllegalArgumentException("Missing configuration for keys: " + key + " or " + altKey);
        }
    }
}
