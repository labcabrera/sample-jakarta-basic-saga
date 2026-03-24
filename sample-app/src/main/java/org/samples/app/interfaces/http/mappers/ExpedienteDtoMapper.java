package org.samples.app.interfaces.http.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import org.samples.app.domain.Expediente;
import org.samples.app.interfaces.http.dto.ExpedienteDto;

@Mapper(componentModel = "cdi")
public interface ExpedienteDtoMapper {

    ExpedienteDto toDto(Expediente expediente);

    @InheritInverseConfiguration
    Expediente toDomain(ExpedienteDto dto);

}
