package org.samples.app.application.cqrs.commands;

import org.samples.cqrs.Command;

public record EliminarExpedienteCommand(
    String idExpediente)

    implements Command {
}
