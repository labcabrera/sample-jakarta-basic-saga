package org.samples.app.application.cqrs.commands;

import org.samples.cqrs.Command;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompensarExpedienteCommand implements Command {

    private final String id;

    private final String motivoError;

}
