package org.samples.cqrs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class CommandBus extends AbstractBus {

    @Inject
    private Instance<CommandHandler<?, ?>> handlers;

    @Inject
    private BeanManager beanManager;

    private final Map<Class<?>, CommandHandler<?, ?>> registry = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Registrando handlers disponibles");
        registry.putAll(registerHandlers(handlers, beanManager, CommandHandler.class));
        log.info("Handlers registrados: {}", registry.keySet());
    }

    public <C, R> R execute(C command) {
        log.info("Ejecutando command: {}", command);
        CommandHandler<C, R> found = (CommandHandler<C, R>) registry.get(command.getClass());
        if (found == null) {
            for (Map.Entry<Class<?>, CommandHandler<?, ?>> e : registry.entrySet()) {
                if (e.getKey().isAssignableFrom(command.getClass())) {
                    found = (CommandHandler<C, R>) e.getValue();
                    break;
                }
            }
        }
        if (found != null) {
            return found.apply(command);
        }
        throw new IllegalStateException("No CommandHandler found for command: " + command.getClass());
    }
}
