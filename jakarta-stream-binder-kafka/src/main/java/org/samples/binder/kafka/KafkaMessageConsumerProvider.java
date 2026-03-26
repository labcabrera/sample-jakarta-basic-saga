package org.samples.binder.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageConsumer;
import org.samples.binder.MessageConsumerProvider;

@ApplicationScoped
public class KafkaMessageConsumerProvider implements MessageConsumerProvider {

    @Override
    public ChannelConfig.BrokerType getBrokerType() {
        return ChannelConfig.BrokerType.KAFKA;
    }

    @Override
    public <T> MessageConsumer<T> createConsumer(ChannelConfig cfg, Class<T> payloadType) {
        return new KafkaConsumerAdapter<>(cfg, payloadType);
    }
}
