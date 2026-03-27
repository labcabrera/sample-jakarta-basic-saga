package org.samples.app.application.cqrs.queries;

import org.samples.cqrs.Query;

public record GetExpedienteByIdQuery(
    String idExpediente)

    implements Query {
}
