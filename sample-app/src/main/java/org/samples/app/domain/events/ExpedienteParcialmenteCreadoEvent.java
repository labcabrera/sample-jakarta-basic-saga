package org.samples.app.domain.events;

import org.samples.binder.DomainEvent;

public record ExpedienteParcialmenteCreadoEvent(
    String idExpediente,
    String codigoExpediente) implements DomainEvent {

    @Override
    public String aggregateId() {
        return idExpediente;
    }

}
