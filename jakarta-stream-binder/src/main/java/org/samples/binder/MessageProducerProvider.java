package org.samples.binder;

public interface MessageProducerProvider {

    String getBrokerType();

    <T> MessageProducer<T> createProducer(ChannelConfig cfg);
}
