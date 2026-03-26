package org.samples.app.application.cqrs.handlers;

import org.samples.app.application.cqrs.commands.CompensarExpedienteCommand;
import org.samples.app.application.cqrs.commands.EliminarExpedienteCommand;
import org.samples.app.domain.events.ErrorCreacionExpedienteEvent;
import org.samples.saga.outbox.OutboxService;
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
    private OutboxService outboxService;

    @Override
    public Void apply(CompensarExpedienteCommand command) {
        log.debug("Ejecutando comando de compensación de creación de expediente '{}'", command.getId());
        String id = command.getId();
        String motivoError = command.getMotivoError();
        var deleteCommand = new EliminarExpedienteCommand(id);
        commandBus.execute(deleteCommand, Void.class);
        var event = new ErrorCreacionExpedienteEvent(id, motivoError);
        outboxService.enqueue("creacion-expediente-alerta", event);
        return null;
    }

}
