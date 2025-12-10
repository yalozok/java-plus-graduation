package ru.practicum.explore.with.me.request.service.model;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestDto;


@Mapper(componentModel = "spring")
public interface ParticipationRequestMapper {

    @Mapping(source = "eventId", target = "event")
    @Mapping(source = "requesterId", target = "requester")
    ParticipationRequestDto toDto(ParticipationRequest entity);
}
