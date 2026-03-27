package org.samples.binder.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageConsumer;
import org.samples.binder.MessageConsumerProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class KafkaMessageConsumerProvider implements MessageConsumerProvider {

    @Inject
    private ObjectMapper mapper;

    @Override
    public String getBrokerType() {
        return "kafka";
    }

    @Override
    public <T> MessageConsumer<T> createConsumer(ChannelConfig cfg, Class<T> payloadType) {
        return new KafkaConsumerAdapter<>(cfg, payloadType, mapper);
    }
}
