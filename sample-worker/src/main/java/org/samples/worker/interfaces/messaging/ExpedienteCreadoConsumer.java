package org.samples.worker.interfaces.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.context.Initialized;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import org.samples.binder.Channel;
import org.samples.binder.MessageConsumer;
import org.samples.binder.Message;
import org.samples.saga.outbox.OutboxService;
import org.samples.worker.application.ports.ProcesadorExpedientePort;
import org.samples.worker.domain.events.ExpedienteCreadoKoEvent;
import org.samples.worker.domain.events.ExpedienteCreadoOkEvent;
import org.samples.worker.interfaces.messaging.dtos.CreacionExpedienteDto;

@ApplicationScoped
@Slf4j
public class ExpedienteCreadoConsumer {

	@Inject
	private ProcesadorExpedientePort procesadorExpediente;

	@Inject
	@Channel("creacion-expediente")
	private MessageConsumer<CreacionExpedienteDto> consumer;

	@Inject
	private OutboxService outboxService;

	public void onStart(@Observes @Initialized(ApplicationScoped.class) Object init) {
		log.info("Application started - subscribing to creacion-expediente channel (CDI observer)");
		consumer.subscribe(this::handleAlert);
	}

	private void handleAlert(Message<CreacionExpedienteDto> msg) {
		CreacionExpedienteDto dto = msg.payload();
		var idExpediente = dto.getId();
		var codigoExpediente = dto.getCodigoExpediente();
		try {
			procesadorExpediente.procesar(idExpediente, codigoExpediente);
			var event = new ExpedienteCreadoOkEvent(idExpediente);
			outboxService.enqueue("creacion-expediente-ok", event);
		}
		catch (Exception ex) {
			var event = new ExpedienteCreadoKoEvent(idExpediente, ex.getMessage());
			outboxService.enqueue("creacion-expediente-ko", event);
		}
	}
}
