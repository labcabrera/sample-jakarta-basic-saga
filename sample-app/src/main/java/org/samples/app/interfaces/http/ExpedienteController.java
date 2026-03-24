package org.samples.app.interfaces.http;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.samples.app.application.cqrs.commands.CrearExpedienteCommand;
import org.samples.app.application.cqrs.queries.GetExpedienteByIdQuery;
import org.samples.app.application.cqrs.queries.GetExpedientesQuery;
import org.samples.app.domain.entities.Expediente;
import org.samples.app.interfaces.http.dto.CreacionExpedienteDto;
import org.samples.app.interfaces.http.dto.ExpedienteDto;
import org.samples.app.interfaces.http.mappers.ExpedienteDtoMapper;
import org.samples.cqrs.CommandBus;
import org.samples.cqrs.Query;
import org.samples.cqrs.QueryBus;

@Path("/expedientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Slf4j
public class ExpedienteController {

	@Inject
	private CommandBus commandBus;

	@Inject
	private QueryBus queryBus;

	@Inject
	private ExpedienteDtoMapper expedienteMapper;

	@GET
	@Path("{id}")
	public Response findById(@PathParam("id") String id) {
		Query query = new GetExpedienteByIdQuery(id);
		Expediente expediente = queryBus.execute(query, Expediente.class);
		return Response.ok(expediente).build();
	}

	@GET
	@SuppressWarnings("unchecked")
	public Response findByRsql(
		@DefaultValue("") @QueryParam("q") String rsql,
		@DefaultValue("0") @QueryParam("page") int page,
		@DefaultValue("10") @QueryParam("size") int size) {
		Query query = new GetExpedientesQuery(rsql, page, size);
		List<Expediente> list = queryBus.execute(query, List.class);
		List<ExpedienteDto> dtos = list.stream().map(expedienteMapper::toDto).toList();
		return Response.ok(dtos).build();
	}

	@POST
	public Response createAlert(CreacionExpedienteDto dto) {
		CrearExpedienteCommand command = new CrearExpedienteCommand(
			dto.getNombre(),
			dto.getApellido1(),
			dto.getApellido2(),
			dto.getCodigo());
		Expediente expediente = commandBus.execute(command, Expediente.class);
		ExpedienteDto result = expedienteMapper.toDto(expediente);
		return Response.status(Response.Status.CREATED).entity(result).build();
	}

}
