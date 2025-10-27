package ru.practicum.stat.service;

import ru.practicum.stat.dto.EndpointHitCreate;
import ru.practicum.stat.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {
    void saveHit(EndpointHitCreate hitCreate);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
