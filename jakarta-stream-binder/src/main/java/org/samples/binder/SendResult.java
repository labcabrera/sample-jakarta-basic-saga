package org.samples.binder;

public record SendResult(
    String destination,
    String messageId,
    long timestamp) {
}