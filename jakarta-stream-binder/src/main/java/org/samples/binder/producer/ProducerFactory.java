package org.samples.binder.producer;

import java.util.Properties;

import org.samples.binder.BinderConfiguration;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageProducer;
import org.samples.binder.MessageProducerProvider;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ProducerFactory {

    private final BinderConfiguration config = new BinderConfiguration();

    @Inject
    @Any
    private Instance<MessageProducerProvider> providers;

    public <T> ChannelConfig loadChannelConfig(String channelName) {
        log.info("Loading channel config for '{}'", channelName);
        String type = config.getType(channelName);
        String prefix = "messaging.channels." + channelName + ".";
        String defaultPrefix = "messaging.channels." + type + ".";
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

    public <T> MessageProducer<T> createProducer(ChannelConfig cfg) {
        return providers.stream()
            .filter(p -> p.getBrokerType().equalsIgnoreCase(cfg.getType()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("No MessageProducerProvider found for type: '%s'. Channel: '%s'. Available providers: %s",
                    cfg.getType(),
                    cfg.getChannelName(),
                    providers.stream().map(MessageProducerProvider::getBrokerType).toList())))
            .createProducer(cfg);
    }
}