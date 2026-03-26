package org.samples.binder;

public interface MessageProducerProvider {
    ChannelConfig.BrokerType getBrokerType();

    <T> MessageProducer<T> createProducer(ChannelConfig cfg, Class<T> payloadType);
}
