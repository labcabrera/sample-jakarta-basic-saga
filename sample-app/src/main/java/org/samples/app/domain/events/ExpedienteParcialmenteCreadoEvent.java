package org.samples.app.domain.events;

import org.samples.binder.DomainEvent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ExpedienteParcialmenteCreadoEvent(
    String idExpediente,
    String codigoExpediente) implements DomainEvent {

    // @JsonCreator
    // public ExpedienteParcialmenteCreadoEvent(
    //     @JsonProperty("idExpediente") String idExpediente,
    //     @JsonProperty("codigoExpediente") String codigoExpediente) {
    //     this.idExpediente = idExpediente;
    //     this.codigoExpediente = codigoExpediente;
    // }

    @Override
    public String aggregateId() {
        return idExpediente;
    }

}
