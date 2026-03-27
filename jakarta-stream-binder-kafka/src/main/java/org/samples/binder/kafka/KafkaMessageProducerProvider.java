package org.samples.binder.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageProducer;
import org.samples.binder.MessageProducerProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class KafkaMessageProducerProvider implements MessageProducerProvider {

    @Inject
    private KafkaClientManager kafkaClientManager;

    @Inject
    private ObjectMapper mapper;

    @Override
    public String getBrokerType() {
        return "kafka";
    }

    @Override
    public <T> MessageProducer<T> createProducer(ChannelConfig cfg) {
        KafkaProducer<String, byte[]> p = kafkaClientManager.getOrCreateProducer(cfg);
        return new KafkaProducerAdapter<>(cfg, p, false, mapper);
    }
}
