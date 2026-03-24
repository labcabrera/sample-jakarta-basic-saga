package org.samples.binder;

import java.io.IOException;
import java.util.Properties;

@SuppressWarnings("unchecked")
public class BinderConfiguration {

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

    public <T> T getValue(String key, Class<T> clazz) {
        if (clazz == Integer.class) {
            return (T) Integer.valueOf(properties.getProperty(key));
        }
        return (T) properties.get(key);
    }

    public boolean containsKey(String key) {
        return this.properties.containsKey(key);
    }

}
