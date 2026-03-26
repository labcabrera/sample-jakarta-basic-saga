package org.samples.worker.interfaces.messaging;

import org.samples.binder.Channel;
import org.samples.binder.Message;
import org.samples.binder.MessageConsumer;
import org.samples.worker.interfaces.messaging.dtos.AlertaExpedienteDto;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class AlertaExpedienteConsumer {

    @Inject
    @Channel("creacion-expediente-alerta")
    private MessageConsumer<AlertaExpedienteDto> consumer;

    public void onStart(@Observes @Initialized(ApplicationScoped.class) Object init) {
        log.info("Application started - subscribing to creacion-expediente channel (CDI observer)");
        consumer.subscribe(this::handle);
    }

    private void handle(Message<AlertaExpedienteDto> message) {
        AlertaExpedienteDto dto = message.payload();
        log.info("Recibida alerta de expediente {}", dto);
    }
}
