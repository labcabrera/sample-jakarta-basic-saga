package org.samples.app.application.cqrs.handlers;

import org.samples.app.application.cqrs.commands.CompensarExpedienteCommand;
import org.samples.app.application.ports.ExpedienteRepository;
import org.samples.app.domain.entities.AlertaExpediente;
import org.samples.binder.Channel;
import org.samples.binder.MessageProducer;
import org.samples.cqrs.CommandHandler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class CompensarExpedienteHandler implements CommandHandler<CompensarExpedienteCommand, Void> {

    @Inject
    private ExpedienteRepository expedienteRepository;

    @Inject
    @Channel("creacion-expediente-alerta")
    private MessageProducer<AlertaExpediente> eventProducer;

    @Override
    public Void execute(CompensarExpedienteCommand command) {
        log.debug("Ejecutando comando de compensación de creación de expediente: {}", command);
        String id = command.getId();
        expedienteRepository.deleteById(id);
        AlertaExpediente alerta = AlertaExpediente.builder()
            .idExpediente(id)
            .mensaje("Error al crear expediente: " + command.getMotivoError())
            .build();
        eventProducer.send(alerta);
        return null;
    }

}
