package org.samples.binder.consumer;

import java.util.Properties;

import org.samples.binder.BinderConfiguration;
import org.samples.binder.BinderConfigurationException;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageConsumer;
import org.samples.binder.MessageConsumerProvider;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ConsumerFactory {

    private final BinderConfiguration config = new BinderConfiguration();

    @Inject
    @Any
    private Instance<MessageConsumerProvider> providers;

    public <T> MessageConsumer<T> createConsumer(ChannelConfig cfg, Class<T> payloadType) {
        return providers.stream()
            .filter(p -> p.getBrokerType().equalsIgnoreCase(cfg.getType()))
            .findFirst()
            .orElseThrow(() -> new BinderConfigurationException(
                String.format("No MessageConsumerProvider found for type: '%s'. Channel: '%s'. Available providers: %s",
                    cfg.getType(),
                    cfg.getChannelName(),
                    providers.stream().map(MessageConsumerProvider::getBrokerType).toList())))
            .createConsumer(cfg, payloadType);
    }

    public <T> ChannelConfig loadChannelConfig(String channelName, Class<T> payloadType) {
        log.info("Loading configuration for channel '{}'' and type '{}'", channelName, payloadType.getName());
        String type = config.getType(channelName);
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
