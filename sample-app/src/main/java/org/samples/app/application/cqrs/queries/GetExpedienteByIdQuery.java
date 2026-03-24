package org.samples.app.application.cqrs.queries;

import org.samples.cqrs.Query;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetExpedienteByIdQuery implements Query {

    private final String id;

}
