package org.samples.worker.domain.events;

import org.samples.binder.DomainEvent;

public record ExpedienteCreadoOkEvent(String idExpediente) implements DomainEvent {

    @Override
    public String aggregateId() {
        return idExpediente;
    }

    // @JsonCreator
    // public ExpedienteCreadoOkEvent(@JsonProperty("id") String id) {
    //     super(id);
    // }

}
