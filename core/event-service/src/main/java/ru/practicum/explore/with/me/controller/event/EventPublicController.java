package ru.practicum.explore.with.me.controller.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.with.me.interaction.api.client.comment.CommentClient;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentDto;
import ru.practicum.explore.with.me.logging.Loggable;
import ru.practicum.explore.with.me.service.event.EventService;

import ru.practicum.explore.with.me.interaction.api.dto.event.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class EventPublicController {
    private final EventService eventsService;
    private final CommentClient commentClient;

    @GetMapping
    @Loggable
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                         @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(required = false) EventPublicSort sort,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) {
        PublicEventParam publicEventParam = new PublicEventParam();
        publicEventParam.setText(Objects.requireNonNullElse(text, ""));
        publicEventParam.setCategories(categories);
        publicEventParam.setPaid(paid);
        publicEventParam.setRangeStart(rangeStart);
        publicEventParam.setRangeEnd(rangeEnd);
        publicEventParam.setOnlyAvailable(onlyAvailable);
        publicEventParam.setSort(sort);
        publicEventParam.setFrom(from);
        publicEventParam.setSize(size);

        return eventsService.getPublicEvents(publicEventParam);
    }

    @GetMapping("/{eventId}")
    @Loggable
    public EventFullDto getEventById(@RequestHeader("X-EWM-USER-ID") @PositiveOrZero @NotNull Long userId,
                                     @PathVariable @PositiveOrZero @NotNull Long eventId) {
        return eventsService.getPublicEventById(userId, eventId);
    }

    @GetMapping("/{eventId}/comments")
    @Loggable
    public List<CommentDto> getCommentsByEvent(@PathVariable @PositiveOrZero @NotNull Long eventId,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        return commentClient.getCommentsByEvent(eventId, PageRequest.of(from / size, size));
    }

    @GetMapping("/internal/{eventId}")
    @Loggable
    public EventFullDto getEventById(@PathVariable("eventId") long eventId) {
        return eventsService.getEventFullDto(eventId);
    }

    @GetMapping("/by-ids")
    @Loggable
    public List<EventShortDto> getEventsByIds(@RequestParam @NotNull List<Long> eventIds) {
        return eventsService.getEventsByIds(eventIds);
    }

    @PutMapping("/{eventId}/like")
    @Loggable
    public void likeEvent(@RequestHeader("X-EWM-USER-ID")  @PositiveOrZero @NotNull Long userId,
                                  @PathVariable("eventId") @PositiveOrZero @NotNull Long eventId) {
        eventsService.likeEvent(userId, eventId);
    }

    @GetMapping("/recommendations")
    @Loggable
    public List<EventShortDto> getRecommendationsForUser(@RequestHeader("X-EWM-USER-ID") @PositiveOrZero @NotNull Long userId,
                                                         @RequestParam(defaultValue = "10") @Positive int limit) {
        return eventsService.getRecommendationsForUser(userId, limit);
    }
}
