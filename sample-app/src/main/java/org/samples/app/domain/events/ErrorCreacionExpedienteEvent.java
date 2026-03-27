package org.samples.app.domain.events;

import org.samples.binder.DomainEvent;

public record ErrorCreacionExpedienteEvent(
    String idExpediente,
    String error) implements DomainEvent {

    @Override
    public String aggregateId() {
        return idExpediente;
    }

}
