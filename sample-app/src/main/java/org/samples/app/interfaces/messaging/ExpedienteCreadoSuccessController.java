package org.samples.app.interfaces.messaging;

import org.samples.app.application.cqrs.commands.ConsolidarExpedienteCommand;
import org.samples.app.interfaces.messaging.dtos.ResultadoCreacionExpedienteDto;
import org.samples.binder.Channel;
import org.samples.binder.Consumer;
import org.samples.binder.Message;
import org.samples.cqrs.CommandBus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ExpedienteCreadoSuccessController {

    @Inject
    @Channel("creacion-expediente-ok")
    private Consumer<ResultadoCreacionExpedienteDto> consumer;

    @Inject
    private CommandBus commandBus;

    public void onStart(@Observes @Initialized(ApplicationScoped.class) Object init) {
        log.info("Suscribiendo consumidor creacion-expediente-ok");
        consumer.subscribe(this::handleAlert);
    }

    private void handleAlert(Message<ResultadoCreacionExpedienteDto> msg) {
        ResultadoCreacionExpedienteDto resultado = msg.payload();
        log.info("Recibido resultado Success << {}", resultado);
        String id = resultado.getId();
        ConsolidarExpedienteCommand cmd = new ConsolidarExpedienteCommand(id);
        commandBus.execute(cmd, Void.class);
    }
}
