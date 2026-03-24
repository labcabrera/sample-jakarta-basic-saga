package org.samples.app.application.cqrs.handlers;

import org.samples.app.application.cqrs.commands.ConsolidarExpedienteCommand;
import org.samples.app.application.ports.ExpedienteRepository;
import org.samples.app.domain.EstadoExpediente;
import org.samples.app.domain.Expediente;
import org.samples.cqrs.CommandHandler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ConsolidarExpedienteHandler implements CommandHandler<ConsolidarExpedienteCommand, Expediente> {

    @Inject
    private ExpedienteRepository expedienteRepository;

    @Override
    public Expediente execute(ConsolidarExpedienteCommand command) {
        log.info("Ejecutando comando de consolidación de expediente: {}", command);
        String id = command.getId();
        Expediente expediente = expedienteRepository.findById(id).orElseThrow(NotFoundException::new);
        expediente.setEstado(EstadoExpediente.CREADO);
        expedienteRepository.updateEstado(expediente.getId(), EstadoExpediente.CREADO);
        return expediente;
    }

}
