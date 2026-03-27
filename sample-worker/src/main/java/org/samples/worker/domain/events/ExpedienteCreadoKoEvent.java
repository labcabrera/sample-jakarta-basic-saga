package org.samples.worker.domain.events;

import org.samples.binder.DomainEvent;

public record ExpedienteCreadoKoEvent(
    String idExpediente,
    String error) implements DomainEvent {

    @Override
    public String aggregateId() {
        return idExpediente;
    }

    // @JsonCreator
    // public ExpedienteCreadoKoEvent(
    //     @JsonProperty("id") String id,
    //     @JsonProperty("error") String error) {
    //     super(id);
    //     this.error = error;
    // }

}
