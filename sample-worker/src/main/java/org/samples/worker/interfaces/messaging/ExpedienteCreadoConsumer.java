package org.samples.worker.interfaces.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.context.Initialized;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.samples.binder.Channel;
import org.samples.binder.MessageConsumer;
import org.samples.binder.Message;
import org.samples.binder.MessageProducer;
import org.samples.worker.application.ports.ProcesadorExpedientePort;
import org.samples.worker.interfaces.messaging.dtos.CreacionExpedienteDto;
import org.samples.worker.interfaces.messaging.dtos.ResultadoCreacionExpedienteDto;

@ApplicationScoped
@Slf4j
public class ExpedienteCreadoConsumer {

	@Inject
	private ProcesadorExpedientePort procesadorExpediente;

	/**
	 * Consumidor de mensajes del broker
	 */
	@Inject
	@Channel("creacion-expediente")
	private MessageConsumer<CreacionExpedienteDto> consumer;

	@Inject
	@Channel("creacion-expediente-ok")
	private MessageProducer<ResultadoCreacionExpedienteDto> producerSuccess;

	@Inject
	@Channel("creacion-expediente-ko")
	private MessageProducer<ResultadoCreacionExpedienteDto> producerError;

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
			resultado.setMessage(ex.getMessage());
		}
		var key = msg.key() != null ? msg.key() : dto.getId();
		var headers = Map.of("correlationId", key);
		if (success) {
			this.producerSuccess.send(new Message<>(resultado, key, headers));
		}
		else {
			this.producerError.send(new Message<>(resultado, key, headers));
		}
	}
}
