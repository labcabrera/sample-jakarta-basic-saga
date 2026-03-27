package org.samples.binder.rabbitmq;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageProducer;
import org.samples.binder.MessageProducerProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class RabbitMessageProducerProvider implements MessageProducerProvider {

    @Inject
    private RabbitConnectionManager connectionManager;

    @Inject
    private ObjectMapper mapper;

    @Override
    public ChannelConfig.BrokerType getBrokerType() {
        return ChannelConfig.BrokerType.RABBITMQ;
    }

    @Override
    public <T> MessageProducer<T> createProducer(ChannelConfig cfg, Class<T> payloadType) {
        return new RabbitProducerAdapter<>(cfg, payloadType, connectionManager, mapper);
    }
}
