package org.samples.binder.rabbitmq;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.samples.binder.ChannelConfig;
import org.samples.binder.Message;
import org.samples.binder.Producer;
import org.samples.binder.SendResult;
import org.samples.binder.serialization.JsonMessageSerializer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RabbitProducerAdapter<T> implements Producer<T> {

    private final String exchange;
    private final String routingKey;
    private final String destination;
    private final Connection connection;
    private final Channel channel;
    private final JsonMessageSerializer jsonSerializer = new JsonMessageSerializer();

    public RabbitProducerAdapter(ChannelConfig cfg, Class<T> payloadType) {
        this.exchange = cfg.getExchange();
        this.routingKey = cfg.getRoutingKey();
        this.destination = cfg.getChannelName();

        log.info("Creando RabbitProducerAdapter para canal '{}' exchange='{}' routingKey='{}' payload={}",
            destination, exchange, routingKey, payloadType);

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
            this.channel.confirmSelect();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to create RabbitMQ producer for " + destination, e);
        }
    }

    @Override
    public CompletionStage<SendResult> send(T payload) {
        return send(new Message<>(payload, null, Map.of()));
    }

    @Override
    public CompletionStage<SendResult> send(Message<T> message) {
        CompletableFuture<SendResult> fut = new CompletableFuture<>();
        try {
            byte[] body = jsonSerializer.serialize(message.payload());
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .messageId(UUID.randomUUID().toString())
                .timestamp(new java.util.Date())
                .build();

            channel.basicPublish(exchange == null ? "" : exchange, routingKey == null ? "" : routingKey, props, body);

            // wait for confirm with a small timeout
            boolean ok = channel.waitForConfirms(5000);
            if (ok) {
                log.info("Message sent to RabbitMQ channel '{}', exchange='{}', routingKey='{}'", destination, exchange, routingKey);
                SendResult result = new SendResult(destination, props.getMessageId(), System.currentTimeMillis());
                fut.complete(result);
            }
            else {
                log.warn("Message NOT confirmed by RabbitMQ broker for channel '{}'", destination);
                fut.completeExceptionally(new IOException("Message not confirmed by broker"));
            }
        }
        catch (Exception e) {
            log.error("Error sending message to RabbitMQ channel '{}'", destination, e);
            fut.completeExceptionally(e);
        }
        return fut;
    }

    @Override
    public void close() {
        log.info("Closing adapter");
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
