package org.samples.worker.application.cqrs.commands;

import org.samples.cqrs.Command;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcesarExpedienteCommand implements Command {

    private final String id;
    private final String codigoExpediente;

}
