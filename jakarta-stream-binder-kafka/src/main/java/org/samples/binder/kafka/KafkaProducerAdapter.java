package org.samples.binder.kafka;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.samples.binder.ChannelConfig;
import org.samples.binder.Message;
import org.samples.binder.MessageProducer;
import org.samples.binder.SendResult;
import org.samples.binder.serialization.JsonMessageSerializer;

@Slf4j
public class KafkaProducerAdapter<T> implements MessageProducer<T> {

    private final KafkaProducer<String, byte[]> producer;
    private final String topic;
    private final JsonMessageSerializer jsonSerializer = new JsonMessageSerializer();
    private final boolean owner;

    public KafkaProducerAdapter(ChannelConfig cfg, Class<T> payloadType) {
        log.info("Creating KafkaProducerAdapter for channel '{}' topic='{}'", cfg.getChannelName(), cfg.getTopic());

        Properties props = new Properties();
        props.put("bootstrap.servers", cfg.getBootstrapServers());
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", ByteArraySerializer.class.getName());
        this.producer = new KafkaProducer<>(props);
        this.topic = cfg.getTopic();
        this.owner = true;
    }

    public KafkaProducerAdapter(ChannelConfig cfg, Class<T> payloadType, KafkaProducer<String, byte[]> sharedProducer, boolean owner) {
        log.info("Creating KafkaProducerAdapter (shared) for channel '{}' topic='{}'", cfg.getChannelName(), cfg.getTopic());
        this.producer = sharedProducer;
        this.topic = cfg.getTopic();
        this.owner = owner;
    }

    @Override
    public CompletionStage<SendResult> send(T payload) {
        Message<T> msg = new Message<>(payload, null, Map.of());
        return send(msg);
    }

    @Override
    public CompletionStage<SendResult> send(Message<T> message) {
        log.info("Sending message to Kafka topic '{}': {}", topic, message);
        CompletableFuture<SendResult> future = new CompletableFuture<>();
        try {
            byte[] bytes = jsonSerializer.serialize(message.payload());
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, message.key(), bytes);
            if (message.headers() != null) {
                message.headers().forEach((k, v) -> record.headers().add(new RecordHeader(k, v.getBytes())));
            }
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception != null) {
                        log.info("Failed to send message to Kafka topic '{}'", topic, exception);
                        future.completeExceptionally(exception);
                    }
                    else {
                        log.info("Message sent to Kafka topic '{}', partition {}, offset {}", metadata.topic(), metadata.partition(),
                            metadata.offset());
                        String messageId = metadata.topic() + ":" + metadata.partition() + ":" + metadata.offset();
                        future.complete(new SendResult(metadata.topic(), messageId, metadata.timestamp()));
                    }
                }
            });
        }
        catch (Exception ex) {
            log.error("Failed to send message to Kafka topic '{}'", topic, ex);
            future.completeExceptionally(ex);
        }
        return future;
    }

    @Override
    public void close() {
        log.info("Closing Kafka producer for topic '{}' (owner={})", topic, owner);
        if (owner) {
            producer.close();
        }
    }

}
