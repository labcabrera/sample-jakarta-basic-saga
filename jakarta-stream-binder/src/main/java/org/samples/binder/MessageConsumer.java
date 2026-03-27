package org.samples.binder;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * Representa el receptor de mensajes para un canal.
 *
 * @param <T> tipo de la carga útil del mensaje
 */
public interface MessageConsumer<T> extends AutoCloseable {

    /**
     * Recibe un único mensaje de forma asincrónica.
     *
     * @return un `CompletionStage` que completa con el siguiente `Message<T>`
     */
    CompletionStage<Message<T>> receive();

    /**
     * Suscribe un manejador que será invocado para cada mensaje entrante.
     *
     * @param handler consumidor que procesa mensajes
     */
    void subscribe(Consumer<Message<T>> handler);
}
