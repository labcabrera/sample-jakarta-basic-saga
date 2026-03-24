package org.samples.binder;

import java.util.concurrent.CompletionStage;

public interface MessageProducer<T> extends AutoCloseable {

    CompletionStage<SendResult> send(T payload);

    CompletionStage<SendResult> send(Message<T> message);

}