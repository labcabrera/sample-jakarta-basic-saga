package org.samples.worker.application.cqrs.commands;

import org.samples.cqrs.Command;

public record ProcesarExpedienteCommand(
    String idExpediente,
    String codigoExpediente

) implements Command {
}
