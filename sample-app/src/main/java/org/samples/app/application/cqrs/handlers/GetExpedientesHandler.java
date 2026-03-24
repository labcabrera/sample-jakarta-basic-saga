package org.samples.app.application.cqrs.handlers;

import java.util.List;

import org.samples.app.application.cqrs.queries.GetExpedientesQuery;
import org.samples.app.application.ports.ExpedienteRepository;
import org.samples.app.domain.entities.Expediente;
import org.samples.cqrs.QueryHandler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetExpedientesHandler implements QueryHandler<GetExpedientesQuery, List<Expediente>> {

    @Inject
    private ExpedienteRepository repository;

    @Override
    public List<Expediente> execute(GetExpedientesQuery query) {
        return repository.findAll();
    }

}
