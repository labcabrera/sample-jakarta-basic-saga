package org.samples.binder.rabbitmq;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageConsumer;
import org.samples.binder.MessageConsumerProvider;

@ApplicationScoped
public class RabbitMessageConsumerProvider implements MessageConsumerProvider {

    @Inject
    RabbitConnectionManager connectionManager;

    @Override
    public ChannelConfig.BrokerType getBrokerType() {
        return ChannelConfig.BrokerType.RABBITMQ;
    }

    @Override
    public <T> MessageConsumer<T> createConsumer(ChannelConfig cfg, Class<T> payloadType) {
        return new RabbitConsumerAdapter<>(cfg, payloadType, connectionManager);
    }
}
