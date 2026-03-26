package org.samples.worker.infrastructure.adapters;

import org.samples.worker.application.ports.ProcesadorExpedientePort;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ProcesadorExpedienteAdapter implements ProcesadorExpedientePort {

    private static final String TEMPLATE_ERR = "Simulando error en el procesamiento del expediente al recibir codigo de expediente 'ERROR'";

    @Override
    public void procesar(String id, String codigoExpediente) {
        log.info("Procesando expediente con ID: {} y código: {}", id, codigoExpediente);
        if (codigoExpediente == null || codigoExpediente.isEmpty()) {
            throw new IllegalArgumentException("Código de expediente no puede ser nulo o vacío");
        }
        else if (codigoExpediente.equals("ERROR")) {
            throw new RuntimeException(TEMPLATE_ERR);
        }
    }

}
