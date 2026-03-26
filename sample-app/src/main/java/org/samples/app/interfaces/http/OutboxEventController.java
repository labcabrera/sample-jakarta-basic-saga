package org.samples.app.interfaces.http;

import java.util.List;

import org.samples.saga.outbox.OutboxEventEntity;
import org.samples.saga.outbox.OutboxRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/outbox-events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Slf4j
public class OutboxEventController {

    @Inject
    private OutboxRepository outboxRepository;

    @GET
    public Response findByRsql(
        @DefaultValue("") @QueryParam("q") String rsql,
        @DefaultValue("0") @QueryParam("page") int page,
        @DefaultValue("10") @QueryParam("size") int size) {
        List<OutboxEventEntity> events = outboxRepository.findAll(page, size);
        return Response.ok(events).build();
    }
}
