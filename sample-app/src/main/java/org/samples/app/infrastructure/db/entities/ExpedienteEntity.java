package org.samples.app.infrastructure.db.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.samples.app.domain.EstadoExpediente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "expediente")
@Data
public class ExpedienteEntity {

    @Id
    @Column(length = 36)
    private String id;

    private String nombre;

    private String apellido1;

    private String apellido2;

    private String codigoExpediente;

    @Enumerated(EnumType.STRING)
    private EstadoExpediente estado;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    public ExpedienteEntity() {
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = fechaCreacion;
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

}
