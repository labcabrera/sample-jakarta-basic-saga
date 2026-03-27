package org.samples.binder.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageConsumer;
import org.samples.binder.Message;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

@Slf4j
public class KafkaConsumerAdapter<T> implements MessageConsumer<T> {

    private final KafkaConsumer<String, byte[]> consumer;
    private final String topic;
    private final Class<T> payloadType;
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile Consumer<Message<T>> handler;
    private final List<CompletableFuture<Message<T>>> pending = Collections.synchronizedList(new ArrayList<>());
    private final Thread poller;
    private volatile boolean running = true;

    public KafkaConsumerAdapter(ChannelConfig cfg, Class<T> payloadType) {
        this.topic = cfg.getTopic();
        this.payloadType = payloadType;
        String consumerGroup = cfg.getConsumerGroup() != null ? cfg.getConsumerGroup() : "group-" + UUID.randomUUID();
        log.info("Creating KafkaConsumerAdapter for channel='{}' topic='{}'", cfg.getChannelName(), cfg.getTopic());

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, cfg.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList(topic));
        this.poller = new Thread(() -> {
            try {
                while (running) {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
                    for (ConsumerRecord<String, byte[]> r : records) {
                        T payload = deserialize(r.value());
                        Message<T> msg = new Message<>(payload, r.key(), Collections.emptyMap());
                        // first satisfy pending one-shot receives
                        CompletableFuture<Message<T>> future = null;
                        synchronized (pending) {
                            if (!pending.isEmpty())
                                future = pending.remove(0);
                        }
                        if (future != null) {
                            future.complete(msg);
                            continue;
                        }
                        if (handler != null) {
                            try {
                                handler.accept(msg);
                            }
                            catch (Exception ex) {
                                log.warn("Error in consumer handler", ex);
                            }
                        }
                    }
                }
            }
            catch (WakeupException we) {
                // expected on close
            }
            catch (Exception ex) {
                log.error("Error in Kafka consumer poll loop", ex);
            }
            finally {
                try {
                    consumer.close();
                }
                catch (Exception ignored) {
                }
            }
        }, "kafka-consumer-poller-" + topic);
        this.poller.setDaemon(true);
        this.poller.start();
    }

    @SuppressWarnings("unchecked")
    private T deserialize(byte[] body) {
        if (body == null)
            return null;
        if (payloadType == String.class) {
            return (T) new String(body);
        }
        try {
            return mapper.readValue(body, payloadType);
        }
        catch (Exception e) {
            log.warn("Failed to deserialize Kafka payload, falling back to string", e);
            try {
                return (T) new String(body);
            }
            catch (Exception ex) {
                return null;
            }
        }
    }

    @Override
    public CompletionStage<Message<T>> receive() {
        CompletableFuture<Message<T>> fut = new CompletableFuture<>();
        pending.add(fut);
        return fut;
    }

    @Override
    public void subscribe(java.util.function.Consumer<Message<T>> handler) {
        this.handler = handler;
    }

    @Override
    public void close() {
        running = false;
        try {
            consumer.wakeup();
        }
        catch (Exception ignored) {
        }
        try {
            poller.join(1000);
        }
        catch (InterruptedException ignored) {
        }
    }
}
