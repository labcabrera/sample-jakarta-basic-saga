package org.samples.binder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.Properties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelConfig {

    private String channelName;
    private String type;
    private Integer maxAttempts;

    @Builder.Default
    private Properties properties = new Properties();

    public <E> Optional<E> getProperty(String primaryKey, Class<E> type) {
        if (properties.containsKey(primaryKey)) {
            if (Integer.class.equals(type)) {
                try {
                    return Optional.of(type.cast(Integer.parseInt((String) properties.get(primaryKey))));
                }
                catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Property " + primaryKey + " is not a valid integer: " + properties.get(primaryKey));
                }
            }
            return Optional.ofNullable(type.cast(properties.get(primaryKey)));
        }
        return Optional.empty();
    }

}
