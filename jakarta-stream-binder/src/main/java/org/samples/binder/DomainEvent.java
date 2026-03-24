package org.samples.binder;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class DomainEvent {

    protected final String id;

}
