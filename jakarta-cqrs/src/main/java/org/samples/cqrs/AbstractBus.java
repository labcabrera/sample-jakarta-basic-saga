package org.samples.cqrs;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

public abstract class AbstractBus {

    @SuppressWarnings("unchecked")
    protected <H> Map<Class<?>, H> registerHandlers(Instance<H> handlers, BeanManager beanManager, Class<?> handlerInterface) {
        Map<Class<?>, H> registry = new ConcurrentHashMap<>();
        for (H handler : handlers) {
            Class<?> handlerClass = handler.getClass();
            Class<?> cmdType = resolveHandlerType(handlerClass, handlerInterface);
            if (cmdType == null) {
                Class<?> superClass = handlerClass.getSuperclass();
                if (superClass != null && superClass != Object.class) {
                    cmdType = resolveHandlerType(superClass, handlerInterface);
                }
            }
            if (cmdType != null) {
                registry.put(cmdType, handler);
            }
        }
        if (registry.isEmpty() && beanManager != null) {
            for (Bean<?> bean : beanManager.getBeans(Object.class)) {
                Class<?> beanClass = bean.getBeanClass();
                if (!handlerInterface.isAssignableFrom(beanClass)) {
                    continue;
                }
                Class<?> cmdType = resolveHandlerType(beanClass, handlerInterface);
                if (cmdType != null) {
                    var ctx = beanManager.createCreationalContext(bean);
                    H handlerRef = (H) beanManager.getReference(bean, bean.getBeanClass(), ctx);
                    registry.put(cmdType, handlerRef);
                }
            }
        }
        return registry;
    }

    protected Class<?> resolveHandlerType(Class<?> handlerClass, Class<?> handlerInterface) {
        Class<?> cls = handlerClass;
        while (cls != null && cls != Object.class) {
            Type[] genericInterfaces = cls.getGenericInterfaces();
            for (Type iface : genericInterfaces) {
                if (iface instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) iface;
                    Type raw = pt.getRawType();
                    if (raw instanceof Class && handlerInterface.isAssignableFrom((Class<?>) raw)) {
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