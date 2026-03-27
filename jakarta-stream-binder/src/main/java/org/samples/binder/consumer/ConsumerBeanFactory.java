package org.samples.binder.consumer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.samples.binder.Channel;
import org.samples.binder.ChannelConfig;
import org.samples.binder.MessageConsumer;

@ApplicationScoped
@Slf4j
public class ConsumerBeanFactory {

    @Inject
    private ConsumerRegistry registry;

    @Inject
    private ConsumerFactory factory;

    @Produces
    @Channel("")
    public <T> MessageConsumer<T> produceConsumer(InjectionPoint injectionPoint) {
        log.info("Creating Consumer for {}", injectionPoint);
        Channel channel = injectionPoint.getAnnotated().getAnnotation(Channel.class);
        if (channel == null) {
            throw new IllegalStateException("Missing @Channel in injection point " + injectionPoint);
        }
        String channelName = channel.value();
        Class<T> payloadType = resolvePayloadType(injectionPoint);
        return registry.getOrCreate(channelName, payloadType, () -> {
            ChannelConfig config = factory.loadChannelConfig(channelName, payloadType);
            return factory.createConsumer(config, payloadType);
        });
    }

    private <T> Class<T> resolvePayloadType(InjectionPoint injectionPoint) {
        Type type = injectionPoint.getType();
        if (type instanceof ParameterizedType pt) {
            Type arg = pt.getActualTypeArguments()[0];
            if (arg instanceof Class<?> clazz) {
                return (Class<T>) clazz;
            }
        }
        throw new IllegalStateException("Can not resolve generic type of Consumer in " + injectionPoint);
    }
}
