package org.samples.cqrs;

import java.util.function.Function;

public interface CommandHandler<C, R> extends Function<C, R> {

}
