package org.gh.afriluck.afriluckussd.mapping;

import org.gh.afriluck.afriluckussd.dto.SessionDto;
import org.gh.afriluck.afriluckussd.entities.Session;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSessionFromDto(SessionDto sessionDto, @MappingTarget Session session);
}
