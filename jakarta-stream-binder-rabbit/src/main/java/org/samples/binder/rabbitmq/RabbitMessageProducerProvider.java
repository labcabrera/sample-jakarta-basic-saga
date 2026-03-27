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
    public String getBrokerType() {
        return "rabbitmq";
    }

    @Override
    public <T> MessageProducer<T> createProducer(ChannelConfig cfg) {
        return new RabbitProducerAdapter<>(cfg, connectionManager, mapper);
    }
}
