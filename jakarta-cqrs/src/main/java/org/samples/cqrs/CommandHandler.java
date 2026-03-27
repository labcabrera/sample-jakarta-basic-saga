package org.samples.cqrs;

import java.util.function.Function;

/**
 * Interfaz funcional que representa un handler de comandos del patrón CQRS utilizado para
 * registrar los manejadores en el bus.
 */
public interface CommandHandler<C, R> extends Function<C, R> {

}
