package ru.practicum.stats.client;

import org.springframework.http.ResponseEntity;
import ru.practicum.stats.dto.EndpointHitCreate;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatClient {
    ResponseEntity<Void> createHit(EndpointHitCreate endpointHitCreate);

    ResponseEntity<List<ViewStats>> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
