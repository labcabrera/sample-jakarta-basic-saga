package org.samples.binder.producer;

import java.util.Optional;

import org.samples.binder.BinderConfiguration;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageProducer;
import org.samples.binder.ChannelConfig.BrokerType;
import org.samples.binder.MessageProducerProvider;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ProducerFactory {

    private final BinderConfiguration config = new BinderConfiguration();

    @jakarta.inject.Inject
    @Any
    private Instance<MessageProducerProvider> providers;

    public <T> ChannelConfig loadChannelConfig(String channelName) {
        log.info("Cargando configuración para canal {}", channelName);
        String prefix = "messaging.channels." + channelName + ".";
        String type = config.getValue(prefix + "type", String.class);
        return switch (type.toLowerCase()) {
        case "kafka" -> buildKafkaConfig(channelName);
        case "rabbitmq" -> buildRabbitConfig(channelName);
        default -> throw new IllegalArgumentException("Tipo de canal no soportado para " + channelName + ": " + type);
        };
    }

    public <T> MessageProducer<T> createProducer(ChannelConfig cfg, Class<T> payloadType) {
        for (MessageProducerProvider p : providers) {
            if (p.getBrokerType() == cfg.getType()) {
                return p.createProducer(cfg, payloadType);
            }
        }
        throw new IllegalArgumentException("No MessageProducerProvider found for type: " + cfg.getType());
    }

    private ChannelConfig buildKafkaConfig(String channelName) {
        String prefix = "messaging.channels." + channelName + ".";
        String defaultPrefix = "messaging.channels.kafka.";
        ChannelConfig channelConfig = ChannelConfig.builder()
            .type(BrokerType.KAFKA)
            .channelName(channelName)
            .topic(config.getValue(prefix + "topic", String.class))
            .bootstrapServers(getValue(prefix + "bootstrap.servers", defaultPrefix + "bootstrap.servers", String.class))
            .build();
        return applySharedConfig(channelConfig, channelName);
    }

    private ChannelConfig buildRabbitConfig(String channelName) {
        String prefix = "messaging.channels." + channelName + ".";
        String defaultPrefix = "messaging.channels.rabbitmq.";
        ChannelConfig channelConfig = ChannelConfig.builder()
            .type(BrokerType.RABBITMQ)
            .channelName(channelName)
            .exchange(config.getValue(prefix + "exchange", String.class))
            .routingKey(config.getValue(prefix + "routing-key", String.class))
            .host(getValue(prefix + "host", defaultPrefix + "host", String.class))
            .port(getValue(prefix + "port", defaultPrefix + "port", Integer.class))
            .username(getValue(prefix + "username", defaultPrefix + "username", String.class))
            .password(getValue(prefix + "password", defaultPrefix + "password", String.class))
            .build();
        return applySharedConfig(channelConfig, channelName);
    }

    private ChannelConfig applySharedConfig(ChannelConfig config, String channelName) {
        Optional<Integer> maxAttempts = getOptionalValue(
            "messaging.channels." + channelName + ".max-attempts",
            "messaging.channels.max-attempts", Integer.class);
        if (maxAttempts.isPresent()) {
            config.setMaxAttempts(maxAttempts.get());
        }
        return config;
    }

    private <T> T getValue(String key, String altKey, Class<T> clazz) {
        Optional<T> value = getOptionalValue(key, altKey, clazz);
        return value.orElseThrow(() -> new IllegalArgumentException("Missing configuration for keys: " + key + " or " + altKey));
    }

    private <T> Optional<T> getOptionalValue(String key, String altKey, Class<T> clazz) {
        if (config.containsKey(key)) {
            return Optional.of(config.getValue(key, clazz));
        }
        else if (config.containsKey(altKey)) {
            return Optional.of(config.getValue(altKey, clazz));
        }
        return Optional.empty();
    }
}