package ru.practicum.explore.with.me.service.compilation;

import ru.practicum.explore.with.me.model.compilation.CompilationCreateDto;
import ru.practicum.explore.with.me.model.compilation.CompilationRequestDto;
import ru.practicum.explore.with.me.model.compilation.CompilationUpdateDto;

import java.util.List;

public interface CompilationService {

    CompilationRequestDto create(CompilationCreateDto compilationCreateDto);

    CompilationRequestDto update(CompilationUpdateDto compilationUpdateDto, Long compId);

    void delete(Long compId);

    List<CompilationRequestDto> get(Boolean pinned, int from, int size);

    CompilationRequestDto getById(Long compId);
}
