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
@SuppressWarnings({ "rawtypes", "unchecked" })
public class QueryBus extends AbstractBus {

    @Inject
    private Instance<QueryHandler> handlers;

    @Inject
    private BeanManager beanManager;

    private final Map<Class<?>, QueryHandler> registry = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Registrando handlers disponibles");
        registry.putAll(registerHandlers(handlers, beanManager, QueryHandler.class));
        log.info("Handlers registrados: {}", registry.keySet());
    }

    public <T> T execute(Query command, Class<T> responseType) {
        log.info("Ejecutando command: {} with con tipo de respuesta {}", command, responseType);
        QueryHandler found = registry.get(command.getClass());
        if (found == null) {
            for (Map.Entry<Class<?>, QueryHandler> e : registry.entrySet()) {
                if (e.getKey().isAssignableFrom(command.getClass())) {
                    found = e.getValue();
                    break;
                }
            }
        }
        if (found != null) {
            Object result = found.apply(command);
            return responseType.cast(result);
        }
        throw new IllegalStateException("No CommandHandler found for command: " + command.getClass());
    }

}
