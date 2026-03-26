package org.samples.worker.application.cqrs.handlers;

import org.samples.cqrs.CommandHandler;
import org.samples.saga.outbox.OutboxService;
import org.samples.worker.application.cqrs.commands.ProcesarExpedienteCommand;
import org.samples.worker.domain.events.ExpedienteCreadoKoEvent;
import org.samples.worker.domain.events.ExpedienteCreadoOkEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ProcesadorExpedienteHandler implements CommandHandler<ProcesarExpedienteCommand, Void> {

    private static final String TEMPLATE_ERR_EMPTY = "Petición de procesamiento de expediente con código de expediente nulo o vacío";
    private static final String TEMPLATE_ERR_SIMULATION = "Simulando error en el procesamiento del expediente al recibir codigo de expediente 'ERROR'";

    @Inject
    private OutboxService outboxService;

    @Override
    public Void apply(ProcesarExpedienteCommand command) {
        var idExpediente = command.getId();
        try {
            processCommand(command);
            var event = new ExpedienteCreadoOkEvent(idExpediente);
            outboxService.enqueue("creacion-expediente-ok", event);
        }
        catch (Exception ex) {
            log.error(String.format("Error procesando expediente %s", idExpediente), ex);
            var event = new ExpedienteCreadoKoEvent(idExpediente, ex.getMessage());
            outboxService.enqueue("creacion-expediente-ko", event);
        }
        return null;
    }

    public void processCommand(ProcesarExpedienteCommand command) {
        log.info("Procesando expediente {}", command);
        if (command.getCodigoExpediente() == null || command.getCodigoExpediente().isEmpty()) {
            throw new IllegalArgumentException(TEMPLATE_ERR_EMPTY);
        }
        else if (command.getCodigoExpediente().equals("ERROR")) {
            throw new RuntimeException(TEMPLATE_ERR_SIMULATION);
        }
    }

}
