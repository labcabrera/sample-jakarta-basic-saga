package org.samples.worker.domain.events;

import org.samples.binder.DomainEvent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExpedienteCreadoKoEvent extends DomainEvent {

    private final String error;

    @JsonCreator
    public ExpedienteCreadoKoEvent(
        @JsonProperty("id") String id,
        @JsonProperty("error") String error) {
        super(id);
        this.error = error;
    }

}
