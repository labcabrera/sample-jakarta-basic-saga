package org.samples.app.infrastructure.db;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import org.samples.app.application.ports.ExpedienteRepository;
import org.samples.app.domain.Expediente;
import org.samples.app.infrastructure.db.entities.ExpedienteEntity;
import org.samples.app.infrastructure.db.mappers.ExpedienteEntityMapper;
import org.samples.app.domain.EstadoExpediente;

import java.time.LocalDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
@Transactional
public class ExpedienteJpaRepository implements ExpedienteRepository {

    @PersistenceContext(unitName = "sample-app-pu")
    private EntityManager em;

    @Inject
    private ExpedienteEntityMapper mapper;

    @Override
    public Optional<Expediente> findById(String id) {
        ExpedienteEntity entity = em.find(ExpedienteEntity.class, id);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDomain(entity));
    }

    @Override
    public List<Expediente> findAll() {
        String jpql = "SELECT e FROM ExpedienteEntity e";
        List<ExpedienteEntity> results = em.createQuery(jpql, ExpedienteEntity.class).getResultList();
        return results.stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void save(Expediente expediente) {
        log.info("Guardando expediente {}", expediente.getId());
        ExpedienteEntity entity = mapper.toEntity(expediente);
        em.persist(entity);
        expediente.setId(entity.getId());
    }

    @Override
    public void updateEstado(String expedienteId, EstadoExpediente estado) {
        log.info("Actualizando estado de expediente {}", expedienteId);
        ExpedienteEntity e = em.find(ExpedienteEntity.class, expedienteId);
        if (e == null) {
            throw new NotFoundException("Expediente no encontrado: " + expedienteId);
        }
        e.setEstado(estado);
        e.setFechaActualizacion(LocalDateTime.now());
        em.merge(e);
    }

    @Override
    public void deleteById(String id) {
        log.info("Borrando expediente: {}", id);
        ExpedienteEntity e = em.find(ExpedienteEntity.class, id);
        if (e != null)
            em.remove(e);
    }

}
