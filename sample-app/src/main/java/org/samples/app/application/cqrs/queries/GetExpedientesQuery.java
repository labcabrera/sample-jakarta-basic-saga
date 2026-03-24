package org.samples.app.application.cqrs.queries;

import org.samples.cqrs.Query;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetExpedientesQuery implements Query {

    private final String rsql;
    private final int page;
    private final int size;

}
