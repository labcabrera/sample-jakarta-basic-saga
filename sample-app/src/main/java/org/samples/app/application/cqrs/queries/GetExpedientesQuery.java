package org.samples.app.application.cqrs.queries;

import org.samples.cqrs.Query;

public record GetExpedientesQuery(
    String rsql,
    int page,
    int size)

    implements Query {
}
