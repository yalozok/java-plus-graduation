package ru.practicum.explore.with.me.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore.with.me.interaction.api.dto.event.*;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestDto;
import ru.practicum.explore.with.me.logging.Loggable;
import ru.practicum.explore.with.me.service.event.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class EventPrivateController {
    private final EventService eventsService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @Loggable
    public EventFullDto createEvent(@PathVariable @PositiveOrZero @NotNull Long userId,
                                    @RequestBody @Valid NewEventDto event) {
        return eventsService.createEvent(userId, event);
    }

    @GetMapping("/{eventId}")
    @Loggable
    public EventFullDto getEventById(@PathVariable @PositiveOrZero @NotNull Long userId,
                                     @PathVariable @PositiveOrZero @NotNull Long eventId) {
        return eventsService.getPrivateEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    @Loggable
    public EventFullDto updateEvent(@PathVariable @PositiveOrZero @NotNull Long userId,
                                    @PathVariable @PositiveOrZero @NotNull Long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest updateEvent) {
        return eventsService.updateEvent(userId, eventId, updateEvent);
    }

    @GetMapping()
    @Loggable
    public List<EventShortDto> getEvents(@PathVariable @PositiveOrZero @NotNull Long userId,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(defaultValue = "10") @Positive int size) {
        return eventsService.getEventsByUser(userId, from, size);
    }

    @GetMapping("/{eventId}/requests")
    @Loggable
    public List<ParticipationRequestDto> getEventParticipationRequestsByUser(@PathVariable @PositiveOrZero @NotNull Long userId,
                                                                             @PathVariable @PositiveOrZero @NotNull Long eventId) {
        return eventsService.getEventParticipationRequestsByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    @Loggable
    public EventRequestStatusUpdateResult updateEventRequestStatus(@PathVariable @PositiveOrZero @NotNull Long userId,
                                                                   @PathVariable @PositiveOrZero @NotNull Long eventId,
                                                                   @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest) {
        return eventsService.updateEventRequestStatus(userId, eventId, updateRequest);
    }
}
