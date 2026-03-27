package org.samples.binder;

import java.io.IOException;
import java.util.Properties;

import lombok.Getter;

public class BinderConfiguration {

    @Getter
    private Properties properties;

    public BinderConfiguration() {
        properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("stream-binder.properties"));
        }
        catch (IOException ex) {
            throw new RuntimeException("Failed to load stream-binder.properties", ex);
        }
    }

    /**
     * Obtiene el tipo de broker para un determinado canal (ej: rabbitmq, kafka, etc.)
     * @param channelName
     * @return
     */
    public String getType(String channelName) {
        String key = "messaging.channels." + channelName + ".type";
        if (!properties.containsKey(key)) {
            throw new BinderConfigurationException("No type defined for channel: " + channelName);
        }
        return properties.getProperty(key);
    }

}
