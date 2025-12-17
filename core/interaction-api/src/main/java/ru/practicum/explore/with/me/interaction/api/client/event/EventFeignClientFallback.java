package ru.practicum.explore.with.me.interaction.api.client.event;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventFullDto;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventShortDto;

import java.util.Collections;
import java.util.List;

public class EventFeignClientFallback implements EventClient{
    @GetMapping("/events/internal/{eventId}")
    @Override
    public EventFullDto getEventById(@PathVariable("eventId") long id) {
        return EventFullDto.unavailable();
    }

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> getEvents(@PathVariable Long userId,
                                  @RequestParam(defaultValue = "0") int from,
                                  @RequestParam(defaultValue = "10") int size) {
        return Collections.emptyList();
    }

    @GetMapping("/events/by-ids")
    @Override
    public List<EventShortDto> getEventsByIds(@RequestParam List<Long> eventIds) {
        return Collections.emptyList();
    }
}
