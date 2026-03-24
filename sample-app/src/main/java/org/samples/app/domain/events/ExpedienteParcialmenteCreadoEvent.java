package org.samples.app.domain.events;

import org.samples.binder.DomainEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExpedienteParcialmenteCreadoEvent extends DomainEvent {

    private final String codigoExpediente;

    public ExpedienteParcialmenteCreadoEvent(String id, String codigoExpediente) {
        super(id);
        this.codigoExpediente = codigoExpediente;
    }

}
