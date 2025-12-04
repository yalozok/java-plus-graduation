package ru.practicum.explore.with.me.model.compilation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explore.with.me.model.event.dto.EventShortDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationRequestDto {
    private Long id;

    private String title;

    private Boolean pinned;

    private List<EventShortDto> events;
}
