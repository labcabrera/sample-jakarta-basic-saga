package org.samples.app.interfaces.messaging.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResultadoCreacionExpedienteDto {

    private String id;

    private String message;

}
