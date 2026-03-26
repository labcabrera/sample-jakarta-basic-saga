package org.samples.worker.domain.events;

import org.samples.binder.DomainEvent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExpedienteCreadoOkEvent extends DomainEvent {

    @JsonCreator
    public ExpedienteCreadoOkEvent(@JsonProperty("id") String id) {
        super(id);
    }

}
