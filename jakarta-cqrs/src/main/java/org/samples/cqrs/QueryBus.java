package org.samples.cqrs;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
@SuppressWarnings({ "rawtypes", "unchecked" })
public class QueryBus {

    @Inject
    private Instance<QueryHandler> handlers;

    @Inject
    private BeanManager beanManager;

    private final Map<Class<?>, QueryHandler> registry = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Registrando handlers disponibles");
        // Primero: intentar registrar a partir de las instancias disponibles (desenmascarar proxys)
        for (QueryHandler handler : handlers) {
            log.trace("Procesando handler instancia: {}", handler.getClass());
            Class<?> handlerClass = handler.getClass();
            Class<?> cmdType = resolveCommandType(handlerClass);
            if (cmdType == null) {
                Class<?> superClass = handlerClass.getSuperclass();
                if (superClass != null && superClass != Object.class) {
                    cmdType = resolveCommandType(superClass);
                }
            }
            if (cmdType != null) {
                registry.put(cmdType, handler);
                log.info("Registrado handler {} -> {}", cmdType, handler.getClass());
            }
            else {
                log.warn("No se pudo resolver tipo de comando para handler instancia: {}", handler.getClass());
            }
        }
        // Fallback: intentar obtener beans desde BeanManager si el registro está vacío
        if (registry.isEmpty()) {
            for (Bean<?> bean : beanManager.getBeans(Object.class)) {
                log.trace("Procesando bean: {}", bean.getBeanClass());
                Class<?> beanClass = bean.getBeanClass();
                if (!QueryHandler.class.isAssignableFrom(beanClass))
                    continue;
                Class<?> cmdType = resolveCommandType(beanClass);
                if (cmdType != null) {
                    Object ref = beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean));
                    QueryHandler handlerRef = (QueryHandler) ref;
                    registry.put(cmdType, handlerRef);
                    log.info("(fallback) Registrado handler {} -> {}", cmdType, beanClass);
                }
            }
        }
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

    private Class<?> resolveCommandType(Class<?> handlerClass) {
        Class<?> cls = handlerClass;
        while (cls != null && cls != Object.class) {
            Type[] genericInterfaces = cls.getGenericInterfaces();
            for (Type iface : genericInterfaces) {
                if (iface instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) iface;
                    Type raw = pt.getRawType();
                    if (raw instanceof Class && QueryHandler.class.isAssignableFrom((Class<?>) raw)) {
                        Type[] args = pt.getActualTypeArguments();
                        if (args.length >= 1) {
                            Type commandType = args[0];
                            if (commandType instanceof Class) {
                                return (Class<?>) commandType;
                            }
                            else if (commandType instanceof ParameterizedType) {
                                return (Class<?>) ((ParameterizedType) commandType).getRawType();
                            }
                        }
                    }
                }
            }
            cls = cls.getSuperclass();
        }
        return null;
    }

}
