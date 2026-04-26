package ru.practicum.explore.with.me.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.explore.with.me.interaction.api.dto.event.*;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserShortDto;
import ru.practicum.explore.with.me.model.event.Event;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, LocationMapper.class})
public interface EventMapper {
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "comments", ignore = true)
    EventFullDto toFullDto(Event event);

    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    EventShortDto toShortDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "category", ignore = true)
    Event toModel(NewEventDto eventDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    void updateFromAdmin(UpdateEventAdminRequestDto dto, @MappingTarget Event event);

    default EventFullDto toFullDtoWithStats(Event event, EventStatistics stats, UserShortDto user) {
        EventFullDto dto = toFullDto(event);
        dto.setRating(stats.getRating(event.getId()));
        dto.setConfirmedRequests(stats.getConfirmedRequests(event.getId()));
        dto.setInitiator(user);
        return dto;
    }

    default EventShortDto toShortDtoWithStats(Event event, EventStatistics stats, UserShortDto user) {
        EventShortDto dto = toShortDto(event);
        dto.setRating(stats.getRating(event.getId()));
        dto.setConfirmedRequests(stats.getConfirmedRequests(event.getId()));
        dto.setInitiator(user);
        return dto;
    }
}
