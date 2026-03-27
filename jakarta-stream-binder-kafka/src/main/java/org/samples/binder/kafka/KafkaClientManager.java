package org.samples.binder.kafka;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.samples.binder.ChannelConfig;

@ApplicationScoped
@Slf4j
public class KafkaClientManager {

    private final Map<String, KafkaProducer<String, byte[]>> producersByBootstrap = new ConcurrentHashMap<>();

    public KafkaProducer<String, byte[]> getOrCreateProducer(ChannelConfig cfg) {
        String key = cfg.getBootstrapServers();
        return producersByBootstrap.computeIfAbsent(key, k -> {
            log.info("Creating shared KafkaProducer for bootstrapServers={}", k);
            Properties props = new Properties();
            props.put("bootstrap.servers", k);
            props.put("key.serializer", StringSerializer.class.getName());
            props.put("value.serializer", ByteArraySerializer.class.getName());
            return new KafkaProducer<>(props);
        });
    }

    public KafkaConsumer<String, byte[]> createConsumer(ChannelConfig cfg) {
        String consumerGroup = cfg.getConsumerGroup() != null ? cfg.getConsumerGroup() : "group-" + UUID.randomUUID();
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, cfg.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        log.info("Creating KafkaConsumer for topic='{}' group='{}' bootstrapServers='{}'", cfg.getTopic(), consumerGroup,
            cfg.getBootstrapServers());
        return new KafkaConsumer<>(props);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down KafkaClientManager, closing {} shared producers", producersByBootstrap.size());
        for (Map.Entry<String, KafkaProducer<String, byte[]>> e : producersByBootstrap.entrySet()) {
            try {
                e.getValue().close();
            }
            catch (Exception ex) {
                log.warn("Error closing KafkaProducer for {}", e.getKey(), ex);
            }
        }
        producersByBootstrap.clear();
    }
}
