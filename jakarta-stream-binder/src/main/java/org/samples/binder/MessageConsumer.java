package org.samples.binder;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * Representa el receptor de mensajes para un canal.
 *
 * <p>
 * Proporciona dos formas de consumir mensajes:
 * <ul>
 * <li>`receive()` — recibe un único mensaje de forma asincrónica (one-shot).</li>
 * <li>`subscribe(Consumer)` — registra un manejador continuo que será invocado por cada
 * mensaje entrante.</li>
 * </ul>
 *
 * <p>
 * Las implementaciones deben encargarse del control de acuses/ack y del cierre ordenado
 * de recursos en `close()`.
 * </p>
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
