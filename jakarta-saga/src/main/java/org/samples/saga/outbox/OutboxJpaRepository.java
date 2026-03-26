package org.samples.saga.outbox;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
public class OutboxJpaRepository implements OutboxRepository {

    private static final String PERSISTENCE_UNIT_NAME = "sample-app-pu";
    private static final String SELECT_PENDING_QUERY = "SELECT e FROM OutboxEventEntity e WHERE e.status = 'PENDING' AND (e.nextAttemptAt IS NULL OR e.nextAttemptAt <= :now) ORDER BY e.createdAt";
    private static final String SELECT_ALL_QUERY = "SELECT e FROM OutboxEventEntity e ORDER BY e.createdAt";
    private static final String UPDATE_STATE = "UPDATE OutboxEventEntity e SET e.status = :status WHERE e.id = :id AND e.status = :currentStatus";

    @PersistenceContext(unitName = PERSISTENCE_UNIT_NAME)
    private EntityManager em;

    @Override
    public List<OutboxEventEntity> findPending(int limit) {
        Instant now = Instant.now();
        return em.createQuery(SELECT_PENDING_QUERY, OutboxEventEntity.class)
            .setParameter("now", now)
            .setMaxResults(limit)
            .getResultList();
    }

    @Override
    public List<OutboxEventEntity> findAll(int page, int limit) {
        return em.createQuery(SELECT_ALL_QUERY, OutboxEventEntity.class)
            .setMaxResults(limit)
            .setFirstResult(page * limit)
            .getResultList();
    }

    @Override
    public void save(OutboxEventEntity event) {
        if (event.getId() == null) {
            event.setId(UUID.randomUUID().toString());
        }
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(Instant.now());
        }
        if (event.getStatus() == null) {
            event.setStatus("PENDING");
        }
        em.persist(event);
    }

    @Override
    public boolean markSending(String id) {
        int updated = em.createQuery(UPDATE_STATE)
            .setParameter("id", id)
            .setParameter("status", "SENDING")
            .setParameter("currentStatus", "PENDING")
            .executeUpdate();
        return updated == 1;
    }

    @Override
    public void markSent(String id, String messageId) {
        em.createQuery("UPDATE OutboxEventEntity e SET e.status = 'SENT', e.sentAt = :sentAt, e.messageId = :messageId WHERE e.id = :id")
            .setParameter("sentAt", Instant.now())
            .setParameter("messageId", messageId)
            .setParameter("id", id)
            .executeUpdate();
    }

    @Override
    public void markFailed(String id, int attempts, java.time.Instant nextAttemptAt) {
        em.createQuery(
            "UPDATE OutboxEventEntity e SET e.status = 'FAILED', e.attempts = :attempts, e.nextAttemptAt = :nextAttemptAt WHERE e.id = :id")
            .setParameter("attempts", attempts)
            .setParameter("nextAttemptAt", nextAttemptAt)
            .setParameter("id", id)
            .executeUpdate();
    }

    @Override
    public void markDlq(String id, int attempts, String reason) {
        em.createQuery("UPDATE OutboxEventEntity e SET e.status = 'DLQ', e.attempts = :attempts, e.dlqReason = :reason WHERE e.id = :id")
            .setParameter("attempts", attempts)
            .setParameter("reason", reason)
            .setParameter("id", id)
            .executeUpdate();
    }

}
