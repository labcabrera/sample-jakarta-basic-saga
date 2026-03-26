package org.samples.app.application.cqrs.handlers;

import java.time.LocalDateTime;
import java.util.UUID;

import org.samples.app.application.cqrs.commands.CrearExpedienteCommand;
import org.samples.app.application.ports.ExpedienteRepository;
import org.samples.app.domain.entities.EstadoExpediente;
import org.samples.app.domain.entities.Expediente;
import org.samples.app.domain.events.ExpedienteParcialmenteCreadoEvent;
import org.samples.cqrs.CommandHandler;

import org.samples.saga.outbox.OutboxService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class CrearExpedienteHandler implements CommandHandler<CrearExpedienteCommand, Expediente> {

    @Inject
    private ExpedienteRepository expedienteRepository;

    @Inject
    private OutboxService outboxService;

    @Override
    @Transactional
    public Expediente apply(CrearExpedienteCommand command) {
        log.info("Ejecutando comando de creación de expediente: {}", command);
        Expediente expediente = Expediente.builder()
            .id(UUID.randomUUID().toString())
            .nombre(command.getNombre())
            .apellido1(command.getApellido1())
            .apellido2(command.getApellido2())
            .codigoExpediente(command.getCodigo())
            .fechaCreacion(LocalDateTime.now())
            .estado(EstadoExpediente.PENDIENTE_CREACION)
            .build();
        expedienteRepository.save(expediente);
        ExpedienteParcialmenteCreadoEvent event = new ExpedienteParcialmenteCreadoEvent(
            expediente.getId(),
            expediente.getCodigoExpediente());
        this.outboxService.enqueue("creacion-expediente", event);
        return expediente;
    }

}
