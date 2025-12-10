package ru.practicum.explore.with.me.interaction.api.client.event;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventFullDto;

@FeignClient(name = "main-service",
        path = "/events",
        configuration = EventFeignConfig.class)
public interface EventClient {

    @GetMapping("/internal/{eventId}")
    EventFullDto getEventById(@PathVariable("eventId") long id);
}
