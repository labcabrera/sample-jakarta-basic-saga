package org.samples.worker.interfaces.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.context.Initialized;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import org.samples.binder.Channel;
import org.samples.binder.Consumer;
import org.samples.binder.Message;
import org.samples.binder.Producer;
import org.samples.worker.application.ports.ProcesadorExpedientePort;
import org.samples.worker.interfaces.messaging.dtos.CreacionExpedienteDto;
import org.samples.worker.interfaces.messaging.dtos.ResultadoCreacionExpedienteDto;

@ApplicationScoped
@Slf4j
public class ExpedienteCreadoConsumer {

	@Inject
	@Channel("creacion-expediente")
	private Consumer<CreacionExpedienteDto> consumer;

	@Inject
	@Channel("creacion-expediente-ok")
	private Producer<ResultadoCreacionExpedienteDto> producerSuccess;

	@Inject
	@Channel("creacion-expediente-ko")
	private Producer<ResultadoCreacionExpedienteDto> producerError;

	@Inject
	private ProcesadorExpedientePort procesadorExpediente;

	public void onStart(@Observes @Initialized(ApplicationScoped.class) Object init) {
		log.info("Application started - subscribing to creacion-expediente channel (CDI observer)");
		consumer.subscribe(this::handleAlert);
	}

	private void handleAlert(Message<CreacionExpedienteDto> msg) {
		CreacionExpedienteDto dto = msg.payload();
		ResultadoCreacionExpedienteDto resultado = new ResultadoCreacionExpedienteDto(dto.getId(), "OK");
		boolean success = false;
		try {
			this.procesadorExpediente.procesar(dto.getId(), dto.getCodigoExpediente());
			success = true;
		}
		catch (Exception ex) {
			resultado.setMessage("Error :" + ex.getMessage());
		}
		if (success) {
			this.producerSuccess.send(resultado);
		}
		else {
			this.producerError.send(resultado);
		}
	}
}
