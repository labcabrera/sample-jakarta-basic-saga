package org.samples.binder.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageProducer;
import org.samples.binder.MessageProducerProvider;

@ApplicationScoped
public class KafkaMessageProducerProvider implements MessageProducerProvider {

    @Override
    public ChannelConfig.BrokerType getBrokerType() {
        return ChannelConfig.BrokerType.KAFKA;
    }

    @Override
    public <T> MessageProducer<T> createProducer(ChannelConfig cfg, Class<T> payloadType) {
        return new KafkaProducerAdapter<>(cfg, payloadType);
    }
}
