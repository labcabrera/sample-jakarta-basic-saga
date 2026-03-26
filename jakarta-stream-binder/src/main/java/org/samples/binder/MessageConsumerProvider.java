package org.samples.binder;

public interface MessageConsumerProvider {
    ChannelConfig.BrokerType getBrokerType();

    <T> MessageConsumer<T> createConsumer(ChannelConfig cfg, Class<T> payloadType);
}
