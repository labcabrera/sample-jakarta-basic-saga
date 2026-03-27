package org.samples.binder;

public interface MessageConsumerProvider {
    String getBrokerType();

    <T> MessageConsumer<T> createConsumer(ChannelConfig cfg, Class<T> payloadType);
}
