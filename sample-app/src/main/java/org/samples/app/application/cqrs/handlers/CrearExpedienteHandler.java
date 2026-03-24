package org.samples.app.application.cqrs.handlers;

import java.time.LocalDateTime;

import org.samples.app.application.cqrs.commands.CrearExpedienteCommand;
import org.samples.app.application.ports.ExpedienteRepository;
import org.samples.app.domain.EstadoExpediente;
import org.samples.app.domain.Expediente;
import org.samples.binder.Channel;
import org.samples.cqrs.CommandHandler;

import org.samples.binder.Producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class CrearExpedienteHandler implements CommandHandler<CrearExpedienteCommand, Expediente> {

    @Inject
    private ExpedienteRepository expedienteRepository;

    @Inject
    @Channel("creacion-expediente")
    private Producer<Expediente> eventProducer;

    @Override
    public Expediente execute(CrearExpedienteCommand command) {
        log.info("Ejecutando comando de creación de expediente: {}", command);
        Expediente expediente = Expediente.builder()
            .nombre(command.getNombre())
            .apellido1(command.getApellido1())
            .apellido2(command.getApellido2())
            .codigoExpediente(command.getCodigo())
            .fechaCreacion(LocalDateTime.now())
            .estado(EstadoExpediente.PENDIENTE_CREACION)
            .build();
        expedienteRepository.save(expediente);
        this.eventProducer.send(expediente).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Error al enviar evento de creacion de expediente", ex);
            }
            else {
                log.info("Enviado evento de creacion de expediente", result);
            }
        });
        return expediente;
    }

}
