package org.samples.app.interfaces.messaging;

import org.samples.app.application.cqrs.commands.CompensarExpedienteCommand;
import org.samples.app.interfaces.messaging.dtos.ResultadoCreacionExpedienteDto;
import org.samples.binder.Channel;
import org.samples.binder.MessageConsumer;
import org.samples.binder.Message;
import org.samples.cqrs.CommandBus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ExpedienteCreadoErrorController {

    @Inject
    @Channel("creacion-expediente-ko")
    private MessageConsumer<ResultadoCreacionExpedienteDto> consumer;

    @Inject
    private CommandBus commandBus;

    public void onStart(@Observes @Initialized(ApplicationScoped.class) Object init) {
        log.info("Suscribiendo consumidor creacion-expediente-ko");
        consumer.subscribe(this::handleAlert);
    }

    private void handleAlert(Message<ResultadoCreacionExpedienteDto> msg) {
        ResultadoCreacionExpedienteDto resultado = msg.payload();
        log.info("Recibido resultado Error << {}", resultado);
        String id = resultado.getId();
        String error = resultado.getMessage();
        CompensarExpedienteCommand cmd = new CompensarExpedienteCommand(id, error);
        commandBus.execute(cmd);
    }
}
