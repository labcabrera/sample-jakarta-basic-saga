package org.samples.binder;

/**
 * Interfaz para proveer MessageConsumer específicos de cada broker. El binder se
 * encargará de registrar las implementaciones de esta interfaz a través de SPI y
 * seleccionar la adecuada en función del broker configurado para cada canal.
 */
public interface MessageConsumerProvider {

    String getBrokerType();

    <T> MessageConsumer<T> createConsumer(ChannelConfig cfg, Class<T> payloadType);
}
