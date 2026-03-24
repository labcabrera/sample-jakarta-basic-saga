package org.samples.app.infrastructure.db.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.samples.app.domain.entities.Expediente;
import org.samples.app.infrastructure.db.entities.ExpedienteEntity;

@Mapper(componentModel = "cdi")
public interface ExpedienteEntityMapper {

    ExpedienteEntity toEntity(Expediente expediente);

    @InheritInverseConfiguration
    Expediente toDomain(ExpedienteEntity dto);

}
