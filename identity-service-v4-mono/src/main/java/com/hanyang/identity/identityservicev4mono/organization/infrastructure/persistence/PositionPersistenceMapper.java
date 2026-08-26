package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.organization.domain.Position;
import com.hanyang.identity.identityservicev4mono.organization.domain.PositionId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PositionPersistenceMapper {
    @Mapping(target = "id", source = "id.value")
    PositionJpaEntity toEntity(Position position);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntity(Position position, @MappingTarget PositionJpaEntity entity);

    default Position toDomain(PositionJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Position.rehydrate(
                new PositionId(entity.getId()),
                entity.getCode(),
                entity.getName(),
                entity.getStatus()
        );
    }
}
