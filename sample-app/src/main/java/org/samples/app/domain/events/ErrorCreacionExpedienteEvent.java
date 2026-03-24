package org.samples.app.domain.events;

import org.samples.binder.DomainEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ErrorCreacionExpedienteEvent extends DomainEvent {

    private final String error;

    public ErrorCreacionExpedienteEvent(String id, String error) {
        super(id);
        this.error = error;
    }

}
