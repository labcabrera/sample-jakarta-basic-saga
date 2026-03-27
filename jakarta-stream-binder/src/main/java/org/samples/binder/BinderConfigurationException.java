package org.samples.binder;

public class BinderConfigurationException extends RuntimeException {

    public BinderConfigurationException(String message) {
        super(message);
    }

    public BinderConfigurationException(String key, ChannelConfig cfg) {
        super("Missing required property '" + key + "' for channel '" + cfg.getChannelName() + "'. Properties: " + cfg.getProperties());
    }

    public BinderConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
