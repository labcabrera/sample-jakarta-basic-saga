package org.samples.worker.interfaces.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.context.Initialized;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import org.samples.binder.Channel;
import org.samples.binder.MessageConsumer;
import org.samples.cqrs.CommandBus;
import org.samples.binder.Message;
import org.samples.worker.application.cqrs.commands.ProcesarExpedienteCommand;
import org.samples.worker.interfaces.messaging.dtos.CreacionExpedienteDto;

@ApplicationScoped
@Slf4j
public class ExpedienteCreadoConsumer {

	@Inject
	@Channel("creacion-expediente")
	private MessageConsumer<CreacionExpedienteDto> consumer;

	@Inject
	private CommandBus commandBus;

	public void onStart(@Observes @Initialized(ApplicationScoped.class) Object init) {
		log.info("Application started - subscribing to creacion-expediente channel (CDI observer)");
		consumer.subscribe(this::handleAlert);
	}

	private void handleAlert(Message<CreacionExpedienteDto> msg) {
		var payload = msg.payload();
		var idExpediente = payload.getId();
		var codigoExpediente = payload.getCodigoExpediente();
		var command = new ProcesarExpedienteCommand(idExpediente, codigoExpediente);
		commandBus.execute(command);
	}
}
