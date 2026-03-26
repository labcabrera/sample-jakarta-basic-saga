package org.samples.saga.outbox;

import java.util.List;

public interface OutboxRepository {

    void save(OutboxEvent event);

    List<OutboxEvent> findPending(int limit);

    boolean markSending(String id);

    void markSent(String id, String messageId);

    void markFailed(String id, int attempts);

}
