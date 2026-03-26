package org.samples.binder.consumer;

import org.samples.binder.BinderConfiguration;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageConsumer;
import org.samples.binder.ChannelConfig.BrokerType;
import org.samples.binder.MessageConsumerProvider;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ConsumerFactory {

    private final BinderConfiguration config = new BinderConfiguration();

    @jakarta.inject.Inject
    @Any
    private Instance<MessageConsumerProvider> providers;

    public <T> MessageConsumer<T> createConsumer(ChannelConfig cfg, Class<T> payloadType) {
        for (MessageConsumerProvider p : providers) {
            if (p.getBrokerType() == cfg.getType()) {
                return p.createConsumer(cfg, payloadType);
            }
        }
        throw new IllegalArgumentException("No MessageConsumerProvider found for type: " + cfg.getType());
    }

    public <T> ChannelConfig loadChannelConfig(String channelName, Class<T> payloadType) {
        log.info("Cargando configuración para canal {} con payload {}", channelName, payloadType.getName());
        String prefix = "messaging.channels." + channelName + ".";
        String type = config.getValue(prefix + "type", String.class);
        return switch (type.toLowerCase()) {
        case "kafka" -> buildKafkaConfig(channelName);
        case "rabbitmq" -> buildRabbitConfig(channelName);
        default -> throw new IllegalArgumentException("Tipo de canal no soportado para " + channelName + ": " + type);
        };
    }

    private ChannelConfig buildKafkaConfig(String channelName) {
        String prefix = "messaging.channels." + channelName + ".";
        String defaultPrefix = "messaging.channels.kafka.";
        return ChannelConfig.builder()
            .type(BrokerType.KAFKA)
            .channelName(null)
            .topic(config.getValue(prefix + "topic", String.class))
            .bootstrapServers(getValue(prefix + "bootstrap.servers", defaultPrefix + "bootstrap.servers", String.class))
            .consumerGroup(getValue(prefix + "consumer-group", defaultPrefix + "consumer-group", String.class))
            .build();
    }

    private ChannelConfig buildRabbitConfig(String channelName) {
        String prefix = "messaging.channels." + channelName + ".";
        String defaultPrefix = "messaging.channels.rabbitmq.";
        return ChannelConfig.builder()
            .type(BrokerType.RABBITMQ)
            .channelName(channelName)
            .queue(config.getValue(prefix + "queue", String.class))
            .host(getValue(prefix + "host", defaultPrefix + "host", String.class))
            .port(getValue(prefix + "port", defaultPrefix + "port", Integer.class))
            .username(getValue(prefix + "username", defaultPrefix + "username", String.class))
            .password(getValue(prefix + "password", defaultPrefix + "password", String.class))
            .build();
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
