package org.samples.app.interfaces.http.mappers;

import org.mapstruct.Mapper;
import org.samples.app.interfaces.http.dto.OutboxEventDto;
import org.samples.saga.outbox.OutboxEventEntity;

@Mapper(componentModel = "cdi")
public interface OutboxEventDtoMapper {

    OutboxEventDto toDto(OutboxEventEntity event);
}
