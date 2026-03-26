package org.samples.binder.rabbitmq;

import jakarta.enterprise.context.ApplicationScoped;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageProducer;
import org.samples.binder.MessageProducerProvider;

@ApplicationScoped
public class RabbitMessageProducerProvider implements MessageProducerProvider {

    @jakarta.inject.Inject
    RabbitConnectionManager connectionManager;

    @Override
    public ChannelConfig.BrokerType getBrokerType() {
        return ChannelConfig.BrokerType.RABBITMQ;
    }

    @Override
    public <T> MessageProducer<T> createProducer(ChannelConfig cfg, Class<T> payloadType) {
        return new RabbitProducerAdapter<>(cfg, payloadType, connectionManager);
    }
}
