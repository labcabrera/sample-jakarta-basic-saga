package org.samples.app.application.cqrs.commands;

import org.samples.cqrs.Command;

public record ConsolidarExpedienteCommand(
    String idExpediente)

    implements Command {
}
