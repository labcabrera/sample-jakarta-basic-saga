package org.samples.binder.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.samples.binder.ChannelConfig;
import org.samples.binder.Consumer;
import org.samples.binder.Message;

@Slf4j
public class RabbitConsumerAdapter<T> implements Consumer<T> {

    private final String queue;
    private final Connection connection;
    private final Channel channel;
    private final Class<T> payloadType;
    private java.util.function.Consumer<Message<T>> handler;

    public RabbitConsumerAdapter(ChannelConfig cfg, Class<T> payloadType) {
        this.queue = cfg.getQueue();
        this.payloadType = payloadType;

        log.info("Creating RabbitConsumerAdapter for channel='{}' queue='{}' payload='{}'' host='{}@{}' user='{}@{}'",
            cfg.getChannelName(),
            queue,
            payloadType,
            cfg.getHost(),
            cfg.getPort(),
            cfg.getUsername(),
            cfg.getPassword());

        try {
            ConnectionFactory factory = new ConnectionFactory();
            if (cfg.getHost() != null)
                factory.setHost(cfg.getHost());
            if (cfg.getPort() != null)
                factory.setPort(cfg.getPort());
            if (cfg.getUsername() != null)
                factory.setUsername(cfg.getUsername());
            if (cfg.getPassword() != null)
                factory.setPassword(cfg.getPassword());

            this.connection = factory.newConnection();
            this.channel = connection.createChannel();

            String consumerTag = channel.basicConsume(queue, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                    T payload = deserialize(body);
                    Message<T> msg = new Message<>(payload, properties != null ? properties.getMessageId() : null, Map.of());
                    if (handler != null) {
                        try {
                            handler.accept(msg);
                        }
                        catch (Exception e) {
                            log.warn("Error in consumer handler", e);
                        }
                    }
                }
            });
            log.info("Rabbit consumer registered: tag='{}' connectedTo='{}:{}'", consumerTag, factory.getHost(), factory.getPort());

        }
        catch (Exception e) {
            throw new RuntimeException("Failed to create RabbitMQ consumer for " + queue, e);
        }
    }

    @Override
    public CompletionStage<Message<T>> receive() {
        CompletableFuture<Message<T>> fut = new CompletableFuture<>();
        // simple implementation: register a one-shot handler
        subscribe(msg -> fut.complete(msg));
        return fut;
    }

    @Override
    public void subscribe(java.util.function.Consumer<Message<T>> handler) {
        this.handler = handler;
    }

    @SuppressWarnings("unchecked")
    private T deserialize(byte[] body) {
        if (body == null)
            return null;
        if (payloadType == String.class) {
            return (T) new String(body, StandardCharsets.UTF_8);
        }
        try (Jsonb jsonb = JsonbBuilder.create()) {
            String s = new String(body, StandardCharsets.UTF_8);
            return jsonb.fromJson(s, payloadType);
        }
        catch (Exception e) {
            log.warn("Failed to deserialize payload, falling back to toString", e);
            try {
                return (T) new String(body, StandardCharsets.UTF_8);
            }
            catch (Exception ex) {
                return null;
            }
        }
    }

    @Override
    public void close() {
        try {
            if (channel != null && channel.isOpen())
                channel.close();
        }
        catch (Exception e) {
            log.warn("Error closing Rabbit channel", e);
        }
        try {
            if (connection != null && connection.isOpen())
                connection.close();
        }
        catch (Exception e) {
            log.warn("Error closing Rabbit connection", e);
        }
    }
}
