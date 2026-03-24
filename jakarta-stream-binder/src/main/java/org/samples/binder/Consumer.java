package org.samples.binder;

import java.util.concurrent.CompletionStage;

/**
 * Consumer abstraction (symmetric to Producer).
 */
public interface Consumer<T> extends AutoCloseable {

    /**
     * Receive a single message (implementation may complete when a message arrives).
     */
    CompletionStage<Message<T>> receive();

    /**
     * Subscribe a handler that will be invoked for each incoming message.
     */
    void subscribe(java.util.function.Consumer<Message<T>> handler);

    @Override
    void close();
}
