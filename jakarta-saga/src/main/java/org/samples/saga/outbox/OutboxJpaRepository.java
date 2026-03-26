package org.samples.saga.outbox;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.samples.saga.outbox.OutboxEventEntity.Status;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
public class OutboxJpaRepository implements OutboxRepository {

    private static final String PERSISTENCE_UNIT_NAME = "sample-app-pu";
    private static final String SELECT_PENDING_QUERY = "SELECT e FROM OutboxEventEntity e WHERE e.status = :status AND (e.nextAttemptAt IS NULL OR e.nextAttemptAt <= :now) ORDER BY e.createdAt";
    private static final String SELECT_ALL_QUERY = "SELECT e FROM OutboxEventEntity e ORDER BY e.createdAt";
    private static final String UPDATE_STATE = "UPDATE OutboxEventEntity e SET e.status = :status WHERE e.id = :id AND e.status = :currentStatus";

    @PersistenceContext(unitName = PERSISTENCE_UNIT_NAME)
    private EntityManager em;

    @Override
    public List<OutboxEventEntity> findPending(int limit) {
        Date now = new Date();
        return em.createQuery(SELECT_PENDING_QUERY, OutboxEventEntity.class)
            .setParameter("status", Status.PENDING)
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
        em.persist(event);
    }

    @Override
    public boolean markSending(String id) {
        int updated = em.createQuery(UPDATE_STATE)
            .setParameter("id", id)
            .setParameter("status", Status.SENDING)
            .setParameter("currentStatus", Status.PENDING)
            .executeUpdate();
        return updated == 1;
    }

    @Override
    public void markSent(String id, String messageId) {
        String jpql = "UPDATE OutboxEventEntity e SET e.status = :status, e.sentAt = :sentAt, e.messageId = :messageId WHERE e.id = :id";
        em.createQuery(jpql)
            .setParameter("status", Status.SENT)
            .setParameter("sentAt", new Date())
            .setParameter("messageId", messageId)
            .setParameter("id", id)
            .executeUpdate();
    }

    @Override
    public void markFailed(String id, int attempts, LocalDateTime nextAttemptAt) {
        String jpql = "UPDATE OutboxEventEntity e SET e.status = :status, e.attempts = :attempts, e.nextAttemptAt = :nextAttemptAt WHERE e.id = :id";
        em.createQuery(jpql)
            .setParameter("status", Status.FAILED)
            .setParameter("attempts", attempts)
            .setParameter("nextAttemptAt", nextAttemptAt)
            .setParameter("id", id)
            .executeUpdate();
    }

    @Override
    public void markDlq(String id, int attempts, String reason) {
        String jpql = "UPDATE OutboxEventEntity e SET e.status = :status, e.attempts = :attempts, e.dlqReason = :reason WHERE e.id = :id";
        em.createQuery(jpql)
            .setParameter("status", Status.DLQ)
            .setParameter("attempts", attempts)
            .setParameter("reason", reason)
            .setParameter("id", id)
            .executeUpdate();
    }

}
