package org.samples.app.application.ports;

import java.util.List;
import java.util.Optional;

import org.samples.app.domain.EstadoExpediente;
import org.samples.app.domain.Expediente;

public interface ExpedienteRepository {

    Optional<Expediente> findById(String id);

    List<Expediente> findAll();

    void save(Expediente expediente);

    void updateEstado(String expedienteId, EstadoExpediente estado);

    void deleteById(String id);

}
