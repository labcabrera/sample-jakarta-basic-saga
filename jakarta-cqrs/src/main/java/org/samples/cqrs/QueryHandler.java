package org.samples.cqrs;

import java.util.function.Function;

public interface QueryHandler<C, R> extends Function<C, R> {

}
