package ru.practicum.explore.with.me.interaction.api.client.event;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventFullDto;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventShortDto;

import java.util.List;

@FeignClient(name = "event-service",
        configuration = EventFeignConfig.class,
        fallback = EventFeignClientFallback.class)
public interface EventClient {

    @GetMapping("/events/internal/{eventId}")
    EventFullDto getEventById(@PathVariable("eventId") long id);

    @GetMapping("/users/{userId}/events")
    List<EventShortDto> getEvents(@PathVariable Long userId,
                                  @RequestParam(defaultValue = "0") int from,
                                  @RequestParam(defaultValue = "10") int size);

    @GetMapping("/events/by-ids")
    List<EventShortDto> getEventsByIds(@RequestParam List<Long> eventIds);
}
