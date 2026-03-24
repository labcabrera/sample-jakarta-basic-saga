package org.samples.binder;

import java.util.Map;

public record Message<T>(
    T payload,
    String key,
    Map<String, String> headers) {
}