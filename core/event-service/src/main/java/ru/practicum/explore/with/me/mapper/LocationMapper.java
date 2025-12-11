package ru.practicum.explore.with.me.mapper;

import org.mapstruct.Mapper;
import ru.practicum.explore.with.me.interaction.api.dto.event.LocationDto;
import ru.practicum.explore.with.me.model.event.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDto toDto(Location location);

    Location toEntity(LocationDto locationDto);
}
