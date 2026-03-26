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

    @PersistenceContext(unitName = "sample-app-pu")
    private EntityManager em;

    @Override
    public void save(OutboxEvent event) {
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
    public List<OutboxEvent> findPending(int limit) {
        return em.createQuery("SELECT e FROM OutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt", OutboxEvent.class)
            .setMaxResults(limit)
            .getResultList();
    }

    @Override
    public boolean markSending(String id) {
        int updated = em.createQuery("UPDATE OutboxEvent e SET e.status = 'SENDING' WHERE e.id = :id AND e.status = 'PENDING'")
            .setParameter("id", id)
            .executeUpdate();
        return updated == 1;
    }

    @Override
    public void markSent(String id, String messageId) {
        em.createQuery("UPDATE OutboxEvent e SET e.status = 'SENT', e.sentAt = :sentAt, e.messageId = :messageId WHERE e.id = :id")
            .setParameter("sentAt", Instant.now())
            .setParameter("messageId", messageId)
            .setParameter("id", id)
            .executeUpdate();
    }

    @Override
    public void markFailed(String id, int attempts) {
        em.createQuery("UPDATE OutboxEvent e SET e.status = 'FAILED', e.attempts = :attempts WHERE e.id = :id")
            .setParameter("attempts", attempts)
            .setParameter("id", id)
            .executeUpdate();
    }

}
