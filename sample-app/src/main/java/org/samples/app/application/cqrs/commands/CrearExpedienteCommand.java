package org.samples.app.application.cqrs.commands;

import org.samples.cqrs.Command;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CrearExpedienteCommand implements Command {

    private final String nombre;

    private final String apellido1;

    private final String apellido2;

    private final String codigo;

}
