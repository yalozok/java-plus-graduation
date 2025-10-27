package ru.practicum.explore.with.me.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.explore.with.me.model.event.Event;
import ru.practicum.explore.with.me.model.event.EventStatistics;
import ru.practicum.explore.with.me.model.event.dto.EventFullDto;
import ru.practicum.explore.with.me.model.event.dto.EventShortDto;
import ru.practicum.explore.with.me.model.event.dto.NewEventDto;
import ru.practicum.explore.with.me.model.event.dto.UpdateEventAdminRequestDto;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class, CommentMapper.class})
public interface EventMapper {
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    EventFullDto toFullDto(Event event);

    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    EventShortDto toShortDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Event toModel(NewEventDto eventDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    void updateFromAdmin(UpdateEventAdminRequestDto dto, @MappingTarget Event event);

    default EventFullDto toFullDtoWithStats(Event event, EventStatistics stats) {
        EventFullDto dto = toFullDto(event);
        dto.setViews(stats.getViews(event.getId()));
        dto.setConfirmedRequests(stats.getConfirmedRequests(event.getId()));
        return dto;
    }

    default EventShortDto toShortDtoWithStats(Event event, EventStatistics stats) {
        EventShortDto dto = toShortDto(event);
        dto.setViews(stats.getViews(event.getId()));
        dto.setConfirmedRequests(stats.getConfirmedRequests(event.getId()));
        return dto;
    }
}
