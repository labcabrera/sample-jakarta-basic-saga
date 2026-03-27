package org.samples.binder;

import java.util.concurrent.CompletionStage;

/**
 * Representa un productor de mensajes para un canal de mensajería.
 *
 * Provee métodos para enviar mensajes de forma asíncrona. Las implementaciones deben
 * encargarse de serializar el payload, establecer las propiedades necesarias (messageId,
 * correlationId, headers) y gestionar la liberación de recursos en `close()`.
 *
 * @param <T> tipo de payload de los mensajes a enviar
 */
public interface MessageProducer<T> extends AutoCloseable {

    /**
     * Envía el mensaje completa el `CompletionStage` con el resultado del envío
     * (`SendResult`) o con la excepción si falla.
     *
     * @param payload la carga útil a enviar
     * @return `CompletionStage` completado con el `SendResult`
     */
    CompletionStage<SendResult> send(T payload);

    /**
     * Envía un objeto `Message` con metadatos (key/correlationId y headers).
     *
     * @param message el mensaje completo a enviar
     * @return `CompletionStage` completado con el `SendResult`
     */
    CompletionStage<SendResult> send(Message<T> message);

}