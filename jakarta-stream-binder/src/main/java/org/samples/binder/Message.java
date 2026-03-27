package org.samples.binder;

import java.util.Map;

/**
 * Representa un mensaje con payload, clave y cabeceras.
 *
 * <p>
 * <b>Nota sobre {@code key}:</b> la propiedad {@code key} se usa como
 * <i>correlationId</i> —es decir, un identificador usado para correlacionar mensajes y
 * ejecuciones de saga/flujo entre servicios. Debe contener el identificador que permite
 * rastrear la misma transacción lógica a través de distintos componentes. Si no está
 * presente, el mensaje puede no ser correlacionable.
 * </p>
 *
 * @param <T> tipo de payload
 * @param payload contenido del mensaje
 * @param key correlationId del mensaje (identificador para correlación)
 * @param headers cabeceras asociadas al mensaje
 */
public record Message<T>(
    T payload,
    String key,
    Map<String, String> headers) {
}