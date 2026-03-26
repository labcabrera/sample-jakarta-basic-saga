package org.samples.binder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelConfig {

    public static <T> ChannelConfig kafka(String channelName, String topic, String bootstrapServers, Class<T> payloadType) {
        ChannelConfig cfg = new ChannelConfig(BrokerType.KAFKA, channelName);
        cfg.topic = topic;
        cfg.bootstrapServers = bootstrapServers;
        return cfg;
    }

    public static <T> ChannelConfig kafka(String channelName, String topic, String bootstrapServers, String consumerGroup,
        Class<T> payloadType) {
        ChannelConfig cfg = new ChannelConfig(BrokerType.KAFKA, channelName);
        cfg.topic = topic;
        cfg.bootstrapServers = bootstrapServers;
        cfg.consumerGroup = consumerGroup;
        return cfg;
    }

    public static <T> ChannelConfig rabbit(String channelName, String queue, String host, Integer port, String username,
        String password, Class<T> payloadType) {
        ChannelConfig cfg = new ChannelConfig(BrokerType.RABBITMQ, channelName);
        cfg.queue = queue;
        cfg.host = host;
        cfg.port = port;
        cfg.username = username;
        cfg.password = password;
        return cfg;
    }

    private String channelName;
    private BrokerType type;
    private Integer maxAttempts;

    // RabbitMQ specific
    private String exchange;
    private String routingKey;
    private String queue;
    private String host;
    private Integer port;
    private String username;
    private String password;

    // Kafka specific
    private String topic;
    private String bootstrapServers;
    private String consumerGroup;

    private ChannelConfig(BrokerType type, String channelName) {
        log.debug("Creating channel config for channel '{}'' of type {}", channelName, type);
        this.type = type;
        this.channelName = channelName;
    }

    public enum BrokerType {
        KAFKA, RABBITMQ
    }

}
