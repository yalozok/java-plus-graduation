package ru.practicum.explore.with.me.controller.event;

import feign.Param;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore.with.me.interaction.api.client.comment.CommentClient;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentDto;
import ru.practicum.explore.with.me.logging.Loggable;
import ru.practicum.explore.with.me.service.event.EventService;
import ru.practicum.stats.client.StatClient;

import ru.practicum.explore.with.me.interaction.api.dto.event.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventPublicController {
    private final EventService eventsService;
    private final CommentClient commentClient;
    private final StatClient statClient;
    private final String className = this.getClass().getSimpleName();

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                         @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(required = false) EventPublicSort sort,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size,
                                         HttpServletRequest request) {
        statClient.createHit(request);

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
        log.trace("{}: getEvents() call with publicEventParam: {}", className, publicEventParam);

        return eventsService.getPublicEvents(publicEventParam);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable @PositiveOrZero @NotNull Long eventId,
                                     HttpServletRequest request) {
        statClient.createHit(request);
        log.trace("{}: getEventById() call with eventId: {}", className, eventId);
        return eventsService.getPublicEventById(eventId);
    }

    @GetMapping("/{eventId}/comments")
    public List<CommentDto> getCommentsByEvent(@PathVariable @PositiveOrZero @NotNull Long eventId,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        log.trace("{}: getCommentsByEvent() call with eventId: {}, from: {}, size: {}",
                className, eventId, from, size);
        return commentClient.getCommentsByEvent(eventId, PageRequest.of(from / size, size));
    }

    @GetMapping("/internal/{eventId}")
    @Loggable
    public EventFullDto getEventById(@PathVariable("eventId") long eventId) {
        return eventsService.getEventFullDto(eventId);
    }

    @GetMapping("/by-ids")
    @Loggable
    public List<EventShortDto> getEventsByIds (@RequestParam @NotNull List<Long> eventIds) {
        return eventsService.getEventsByIds(eventIds);
    }
}
