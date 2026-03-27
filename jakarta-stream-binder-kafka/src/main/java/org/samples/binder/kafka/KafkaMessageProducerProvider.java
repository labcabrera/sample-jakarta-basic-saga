package org.samples.binder.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageProducer;
import org.samples.binder.MessageProducerProvider;

@ApplicationScoped
public class KafkaMessageProducerProvider implements MessageProducerProvider {

    @Inject
    private KafkaClientManager kafkaClientManager;

    @Override
    public ChannelConfig.BrokerType getBrokerType() {
        return ChannelConfig.BrokerType.KAFKA;
    }

    @Override
    public <T> MessageProducer<T> createProducer(ChannelConfig cfg, Class<T> payloadType) {
        KafkaProducer<String, byte[]> p = kafkaClientManager.getOrCreateProducer(cfg);
        return new KafkaProducerAdapter<>(cfg, payloadType, p, false);
    }
}
