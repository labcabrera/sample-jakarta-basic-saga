package org.samples.app.application.cqrs.commands;

import org.samples.cqrs.Command;

public record CrearExpedienteCommand(
    String nombre,
    String apellido1,
    String apellido2,
    String codigoExpediente)

    implements Command {
}
