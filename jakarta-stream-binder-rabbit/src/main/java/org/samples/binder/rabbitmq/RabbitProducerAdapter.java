package org.samples.binder.rabbitmq;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.samples.binder.ChannelConfig;
import org.samples.binder.DomainEvent;
import org.samples.binder.Message;
import org.samples.binder.MessageProducer;
import org.samples.binder.SendResult;
import org.samples.binder.serialization.JsonMessageSerializer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RabbitProducerAdapter<T> implements MessageProducer<T> {

    private static final int DELIVERY_MODE_PERSISTENT = 2;
    private static final int CONFIRMATION_TIMEOUT_MS = 5000;

    private final String exchange;
    private final String routingKey;
    private final String destination;
    private final Connection connection;
    private final Channel channel;
    private final JsonMessageSerializer jsonSerializer = new JsonMessageSerializer();

    public RabbitProducerAdapter(ChannelConfig cfg, Class<T> payloadType, RabbitConnectionManager connectionManager) {
        this.exchange = cfg.getExchange();
        this.routingKey = cfg.getRoutingKey();
        this.destination = cfg.getChannelName();
        log.info("Creating RabbitProducerAdapter for channel {}/{} with destination {}", exchange, routingKey, destination);
        try {
            this.connection = connectionManager.getConnection(cfg);
            this.channel = connection.createChannel();
            this.channel.confirmSelect();
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to create RabbitMQ producer for " + destination, ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletionStage<SendResult> send(T payload) {
        if (payload instanceof Message) {
            return send((Message<T>) payload);
        }
        String key = null;
        if (payload instanceof DomainEvent event) {
            key = event.getId();
        }
        else {
            log.warn("Payload is not a DomainEvent, generating random key for traceability");
            key = UUID.randomUUID().toString();
        }
        return send(new Message<>(payload, key, Map.of()));
    }

    @Override
    public CompletionStage<SendResult> send(Message<T> message) {
        CompletableFuture<SendResult> future = new CompletableFuture<>();
        try {
            byte[] body = jsonSerializer.serialize(message.payload());
            AMQP.BasicProperties props = buildProperties(message);
            channel.basicPublish(exchange == null ? "" : exchange, routingKey == null ? "" : routingKey, props, body);
            boolean ok = channel.waitForConfirms(CONFIRMATION_TIMEOUT_MS);
            if (ok) {
                log.info("Message sent to RabbitMQ channel '{}', exchange='{}', routingKey='{}'", destination, exchange, routingKey);
                SendResult result = new SendResult(destination, props.getMessageId(), System.currentTimeMillis());
                future.complete(result);
            }
            else {
                log.warn("Message NOT confirmed by RabbitMQ broker for channel '{}'", destination);
                future.completeExceptionally(new IOException("Message not confirmed by broker"));
            }
        }
        catch (Exception e) {
            log.error("Error sending message to RabbitMQ channel '{}'", destination, e);
            future.completeExceptionally(e);
        }
        return future;
    }

    private BasicProperties buildProperties(Message<T> message) {
        BasicProperties.Builder builder = new BasicProperties.Builder()
            .messageId(UUID.randomUUID().toString())
            .timestamp(new Date())
            .deliveryMode(DELIVERY_MODE_PERSISTENT);
        if (message.key() != null) {
            log.info("Setting correlationId for message with key '{}'", message.key());
            builder.correlationId(message.key());
        }
        if (message.headers() != null && !message.headers().isEmpty()) {
            Map<String, Object> hdrs = new HashMap<>();
            message.headers().forEach((k, v) -> hdrs.put(k, v));
            builder.headers(hdrs);
        }
        return builder.build();
    }

    @Override
    public void close() {
        log.info("Closing adapter");
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        }
        catch (Exception ex) {
            log.warn("Error closing Rabbit channel", ex);
        }
        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        }
        catch (Exception ex) {
            log.warn("Error closing Rabbit connection", ex);
        }
    }
}
