package ru.practicum.explore.with.me.service.event;

import ru.practicum.explore.with.me.model.event.Event;
import ru.practicum.explore.with.me.model.event.EventStatistics;
import ru.practicum.explore.with.me.model.event.PublicEventParam;
import ru.practicum.explore.with.me.model.event.dto.EventFullDto;
import ru.practicum.explore.with.me.model.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore.with.me.model.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore.with.me.model.event.dto.EventShortDto;
import ru.practicum.explore.with.me.model.event.dto.EventViewsParameters;
import ru.practicum.explore.with.me.model.event.dto.NewEventDto;
import ru.practicum.explore.with.me.model.event.dto.UpdateEventUserRequest;
import ru.practicum.explore.with.me.model.participation.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventService {
    EventFullDto createEvent(long userId, NewEventDto eventDto);

    EventFullDto getPrivateEventById(long userId, long eventId);

    EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEvent);

    EventFullDto getPublicEventById(long eventId);

    List<EventShortDto> getEventsByUser(long userId, int from, int count);

    Map<Long, Long> getEventViews(EventViewsParameters params);

    List<ParticipationRequestDto> getEventParticipationRequestsByUser(long userId, long eventId);

    EventRequestStatusUpdateResult updateEventRequestStatus(long userId, long eventId,
                                                            EventRequestStatusUpdateRequest updateRequest);

    List<EventShortDto> getPublicEvents(PublicEventParam params);

    Map<Long, Integer> getConfirmedRequests(List<Long> eventIds);

    EventStatistics getEventStatistics(List<Event> events, LocalDateTime startStats, LocalDateTime endStats);
}
