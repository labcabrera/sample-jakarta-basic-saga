package org.samples.cqrs;

public interface QueryHandler<C, R> {

    R execute(C query);

}
