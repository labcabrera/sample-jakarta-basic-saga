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
public class QueryBus extends AbstractBus {

    @Inject
    private Instance<QueryHandler<?, ?>> handlers;

    @Inject
    private BeanManager beanManager;

    private final Map<Class<?>, QueryHandler<?, ?>> registry = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Registrando handlers disponibles");
        registry.putAll(registerHandlers(handlers, beanManager, QueryHandler.class));
        log.info("Handlers registrados: {}", registry.keySet());
    }

    public <Q, R> R execute(Q query) {
        log.debug("Ejecutando query {}", query);
        QueryHandler<Q, R> found = (QueryHandler<Q, R>) registry.get(query.getClass());
        if (found == null) {
            for (Map.Entry<Class<?>, QueryHandler<?, ?>> e : registry.entrySet()) {
                if (e.getKey().isAssignableFrom(query.getClass())) {
                    found = (QueryHandler<Q, R>) e.getValue();
                    break;
                }
            }
        }
        if (found != null) {
            return found.apply(query);
        }
        throw new IllegalStateException("No QueryHandler found for command: " + query.getClass());
    }

    public <Q> void executeVoid(Q command) {
        execute(command);
    }

}
