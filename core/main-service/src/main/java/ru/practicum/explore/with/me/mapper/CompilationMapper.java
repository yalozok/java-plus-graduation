package ru.practicum.explore.with.me.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore.with.me.model.compilation.Compilation;
import ru.practicum.explore.with.me.model.compilation.CompilationCreateDto;
import ru.practicum.explore.with.me.model.compilation.CompilationRequestDto;
import ru.practicum.explore.with.me.model.event.Event;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", source = "events")
    Compilation toEntity(CompilationCreateDto compilationCreateDto, List<Event> events);

    @Mapping(target = "events", source = "events")
    CompilationRequestDto toRequestDto(Compilation compilation);
}
