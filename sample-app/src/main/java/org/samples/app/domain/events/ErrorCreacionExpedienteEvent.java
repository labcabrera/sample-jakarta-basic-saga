package org.samples.app.domain.events;

import org.samples.binder.DomainEvent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorCreacionExpedienteEvent(
    String idExpediente,
    String error) implements DomainEvent {

    // @JsonCreator
    // public ErrorCreacionExpedienteEvent(
    //     @JsonProperty("id") String idExpediente,
    //     @JsonProperty("error") String error) {
    //     this.idExpediente = idExpediente;
    //     this.error = error;
    // }

    @Override
    public String aggregateId() {
        return idExpediente;
    }

}
