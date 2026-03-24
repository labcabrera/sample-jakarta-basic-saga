package org.samples.worker.application.ports;

public interface ProcesadorExpedientePort {

    void procesar(String expedienteId, String codigoExpediente);

}
