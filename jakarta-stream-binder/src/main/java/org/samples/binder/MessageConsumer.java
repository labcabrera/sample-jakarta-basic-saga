package org.samples.binder;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public interface MessageConsumer<T> extends AutoCloseable {

    CompletionStage<Message<T>> receive();

    void subscribe(Consumer<Message<T>> handler);
}
