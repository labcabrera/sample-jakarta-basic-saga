package org.samples.app.domain.entities;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Expediente {

    private String id;
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String codigoExpediente;
    private EstadoExpediente estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

}
