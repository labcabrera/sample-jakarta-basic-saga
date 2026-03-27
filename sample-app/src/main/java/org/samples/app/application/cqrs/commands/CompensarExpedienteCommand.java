package org.samples.app.application.cqrs.commands;

import org.samples.cqrs.Command;

public record CompensarExpedienteCommand(
    String idExpediente,
    String motivoError)

    implements Command {
}