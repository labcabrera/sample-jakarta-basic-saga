package org.samples.cqrs;

public interface CommandHandler<C, R> {

    R execute(C command);

}
