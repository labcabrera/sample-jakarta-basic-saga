package org.samples.app.domain.events;

import org.samples.binder.DomainEvent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExpedienteParcialmenteCreadoEvent extends DomainEvent {

    private final String codigoExpediente;

    @JsonCreator
    public ExpedienteParcialmenteCreadoEvent(
        @JsonProperty("id") String id,
        @JsonProperty("codigoExpediente") String codigoExpediente) {
        super(id);
        this.codigoExpediente = codigoExpediente;
    }

}
