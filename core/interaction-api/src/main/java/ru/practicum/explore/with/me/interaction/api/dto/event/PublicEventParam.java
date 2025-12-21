package ru.practicum.explore.with.me.interaction.api.dto.event;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PublicEventParam {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private EventPublicSort sort;
    private Integer from;
    private Integer size;
}
