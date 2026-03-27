package org.samples.binder.consumer;

import java.util.Properties;

import org.samples.binder.BinderConfiguration;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageConsumer;
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
            if (p.getBrokerType().equalsIgnoreCase(cfg.getType())) {
                return p.createConsumer(cfg, payloadType);
            }
        }
        throw new IllegalArgumentException("No MessageConsumerProvider found for type: " + cfg.getType());
    }

    public <T> ChannelConfig loadChannelConfig(String channelName, Class<T> payloadType) {
        log.info("Cargando configuración para canal {} con payload {}", channelName, payloadType.getName());
        String type = config.getValue("messaging.channels." + channelName + ".type", String.class);
        String prefix = "messaging.channels." + channelName + ".";
        String defaultPrefix = "messaging.channels." + type.toLowerCase() + ".";
        Properties filteredProperties = new Properties();
        config.getProperties().forEach((k, v) -> {
            String key = (String) k;
            if (key.startsWith(prefix)) {
                String propKey = key.substring(prefix.length());
                filteredProperties.put(propKey, v);
            }
            else if (key.startsWith(defaultPrefix)) {
                String propKey = key.substring(defaultPrefix.length());
                filteredProperties.put(propKey, v);
            }
        });
        return ChannelConfig.builder()
            .channelName(channelName)
            .type(type)
            .properties(filteredProperties)
            .build();
    }

}
