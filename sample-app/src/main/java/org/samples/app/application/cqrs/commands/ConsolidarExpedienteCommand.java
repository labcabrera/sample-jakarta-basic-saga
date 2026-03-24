package org.samples.app.application.cqrs.commands;

import org.samples.cqrs.Command;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConsolidarExpedienteCommand implements Command {

    private final String id;

}
