package org.samples.binder;

/**
 * Generalización de los eventos de dominio asociados a una entidad.
 * 
 * Define el metodo aggregateId() con el que relacionaremos los mensajes ya que será
 * utilizado para establecer los correlationId de los mensajes.
 * 
 * En casos de entidad con una clave simple simplemente podremos devolver esta clave. En
 * los casos de una clave compuesta podremos devolver un string con la concatenación de
 * los campos que componen la clave o utilizar otro mecanismo de correlación.
 */
public interface DomainEvent {

    String aggregateId();

}
