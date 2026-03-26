package org.samples.binder.rabbitmq;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageConsumer;
import org.samples.binder.Message;
import org.samples.binder.serialization.JsonMessageDeserializer;

@Slf4j
public class RabbitConsumerAdapter<T> implements MessageConsumer<T> {

    // process one message at a time to preserve ordering and enable controlled acks
    private static final int PREFETCH_COUNT = 1;

    private final String queue;
    private final Connection connection;
    private final Channel channel;
    private Consumer<Message<T>> handler;
    private JsonMessageDeserializer jsonDeserializer = new JsonMessageDeserializer();

    public RabbitConsumerAdapter(ChannelConfig cfg, Class<T> payloadType) {
        this.queue = cfg.getQueue();
        log.info("Creating RabbitConsumerAdapter for channel='{}'", cfg.getChannelName());
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(cfg.getHost());
            factory.setPort(cfg.getPort());
            factory.setUsername(cfg.getUsername());
            factory.setPassword(cfg.getPassword());
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            this.channel.basicQos(PREFETCH_COUNT);
            String consumerTag = channel.basicConsume(queue, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
                    throws IOException {
                    T payload = jsonDeserializer.deserialize(body, payloadType);
                    Message<T> msg = new Message<>(payload, properties != null ? properties.getMessageId() : null, Map.of());
                    if (handler != null) {
                        try {
                            handler.accept(msg);
                            try {
                                channel.basicAck(envelope.getDeliveryTag(), false);
                            }
                            catch (IOException e) {
                                log.warn("Failed to ack message", e);
                            }
                        }
                        catch (Exception e) {
                            log.warn("Error in consumer handler, nack and send to DLQ if configured", e);
                            try {
                                // reject and do not requeue so DLX can route to DLQ if configured
                                channel.basicNack(envelope.getDeliveryTag(), false, false);
                            }
                            catch (IOException ex) {
                                log.warn("Failed to nack message", ex);
                            }
                        }
                    }
                    else {
                        // no handler registered - requeue by default
                        try {
                            channel.basicNack(envelope.getDeliveryTag(), false, true);
                        }
                        catch (IOException e) {
                            log.warn("Failed to nack message (no handler)", e);
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
        CompletableFuture<Message<T>> future = new CompletableFuture<>();
        subscribe(msg -> future.complete(msg));
        return future;
    }

    @Override
    public void subscribe(Consumer<Message<T>> handler) {
        this.handler = handler;
    }

    @Override
    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        }
        catch (Exception e) {
            log.warn("Error closing Rabbit channel", e);
        }
        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        }
        catch (Exception e) {
            log.warn("Error closing Rabbit connection", e);
        }
    }
}
