package org.samples.app.application.cqrs.handlers;

import org.samples.app.application.cqrs.commands.CompensarExpedienteCommand;
import org.samples.app.application.cqrs.commands.EliminarExpedienteCommand;
import org.samples.app.domain.events.ErrorCreacionExpedienteEvent;
import org.samples.binder.Channel;
import org.samples.binder.MessageProducer;
import org.samples.cqrs.CommandBus;
import org.samples.cqrs.CommandHandler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class CompensarExpedienteHandler implements CommandHandler<CompensarExpedienteCommand, Void> {

    @Inject
    private CommandBus commandBus;

    @Inject
    @Channel("creacion-expediente-alerta")
    private MessageProducer<ErrorCreacionExpedienteEvent> eventProducer;

    @Override
    public Void apply(CompensarExpedienteCommand command) {
        log.debug("Ejecutando comando de compensación de creación de expediente: {}", command);
        String id = command.getId();
        String motivoError = command.getMotivoError();
        var deleteCommand = new EliminarExpedienteCommand(id);
        commandBus.execute(deleteCommand, Void.class);
        var event = new ErrorCreacionExpedienteEvent(id, motivoError);
        eventProducer.send(event);
        return null;
    }

}
