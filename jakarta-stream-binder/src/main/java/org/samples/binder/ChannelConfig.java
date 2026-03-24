package org.samples.binder;

import lombok.Data;

@Data
public class ChannelConfig {

    public static <T> ChannelConfig kafka(String channelName, String topic, String bootstrapServers, Class<T> payloadType) {
        ChannelConfig cfg = new ChannelConfig(Type.KAFKA, channelName);
        cfg.topic = topic;
        cfg.bootstrapServers = bootstrapServers;
        return cfg;
    }

    public static <T> ChannelConfig rabbit(String channelName, String exchange, String routingKey, String host, Integer port,
        String username,
        String password, Class<T> payloadType) {
        ChannelConfig cfg = new ChannelConfig(Type.RABBITMQ, channelName);
        cfg.exchange = exchange;
        cfg.routingKey = routingKey;
        cfg.host = host;
        cfg.port = port;
        cfg.username = username;
        cfg.password = password;
        return cfg;
    }

    public static <T> ChannelConfig rabbit(String channelName, String queue, String host, Integer port, String username,
        String password, Class<T> payloadType) {
        ChannelConfig cfg = new ChannelConfig(Type.RABBITMQ, channelName);
        cfg.queue = queue;
        cfg.host = host;
        cfg.port = port;
        cfg.username = username;
        cfg.password = password;
        return cfg;
    }

    private String channelName;
    private Type type;

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

    private ChannelConfig(Type type, String channelName) {
        System.out.println("Creating channel config for channel: " + channelName + " of type: " + type);
        this.type = type;
        this.channelName = channelName;
    }

    public enum Type {
        KAFKA, RABBITMQ
    }

}
