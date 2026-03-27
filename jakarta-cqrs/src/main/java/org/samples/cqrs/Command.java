package org.samples.cqrs;

/**
 * Representa un comando del patrón CQRS.
 *
 * <p>
 * Un {@code Command} modela una intención de cambio de estado en el sistema. Es un
 * marcador para objetos que contienen los datos necesarios para ejecutar una acción (por
 * ejemplo: crear, actualizar o eliminar una entidad).
 * </p>
 *
 * <p>
 * Características y uso típico:
 * </p>
 * <ul>
 * <li>Se utiliza para encapsular la intención de mutar el modelo de dominio.</li>
 * <li>Normalmente se envía a un handler/command bus que valida y ejecuta la lógica.</li>
 * <li>Debe ser un objeto inmutable o tratado como datos transferibles.</li>
 * </ul>
 */
public interface Command {
}
