package org.samples.app.application.cqrs.handlers;

import org.samples.app.application.cqrs.queries.GetExpedienteByIdQuery;
import org.samples.app.application.ports.ExpedienteRepository;
import org.samples.app.domain.entities.Expediente;
import org.samples.cqrs.QueryHandler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class GetExpedienteByIdHandler implements QueryHandler<GetExpedienteByIdQuery, Expediente> {

    @Inject
    private ExpedienteRepository repository;

    @Override
    public Expediente apply(GetExpedienteByIdQuery query) {
        return repository.findById(query.idExpediente()).orElseThrow(NotFoundException::new);
    }

}
