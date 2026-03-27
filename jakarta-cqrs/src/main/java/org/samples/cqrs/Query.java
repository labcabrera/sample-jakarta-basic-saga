package org.samples.cqrs;

/**
 * Representa una consulta del patrón CQRS.
 *
 * <p>
 * Un {@code Query} describe una petición de lectura del sistema. A diferencia de un
 * {@code Command}, no debe provocar cambios de estado: su propósito es recuperar
 * información o proyección del modelo.
 * </p>
 *
 * <p>
 * Características y uso típico:
 * </p>
 * <ul>
 * <li>Contiene los parámetros necesarios para obtener los datos (filtros,
 * identificadores, paginación, etc.).</li>
 * <li>Se envía a un handler/query bus que ejecuta la lógica de consulta y devuelve un
 * resultado.</li>
 * <li>Debe ser tratada como un objeto inmutable o de solo lectura.</li>
 * </ul>
 */
public interface Query {

}
