package org.samples.app.application.cqrs.handlers;

import org.samples.app.application.cqrs.commands.EliminarExpedienteCommand;
import org.samples.app.application.ports.ExpedienteRepository;
import org.samples.cqrs.CommandHandler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class EliminarExpedienteHandler implements CommandHandler<EliminarExpedienteCommand, Void> {

    @Inject
    private ExpedienteRepository repository;

    @Override
    public Void apply(EliminarExpedienteCommand command) {
        log.info("Eliminando expediente '{}'", command.getId());
        repository.deleteById(command.getId());
        return null;
    }

}
