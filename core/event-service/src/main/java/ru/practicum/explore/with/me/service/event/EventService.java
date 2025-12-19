package ru.practicum.explore.with.me.service.event;

import org.springframework.data.domain.Pageable;
import ru.practicum.explore.with.me.interaction.api.dto.event.*;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestDto;
import ru.practicum.explore.with.me.model.event.EventViewsParameters;


import java.util.List;
import java.util.Map;

public interface EventService {
    EventFullDto createEvent(long userId, NewEventDto eventDto);

    EventFullDto getPrivateEventById(long userId, long eventId);

    EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEvent);

    EventFullDto getPublicEventById(long eventId);

    EventFullDto getEventFullDto(long eventId);

    List<EventShortDto> getEventsByUser(long userId, int from, int count);

    Map<Long, Long> getEventViews(EventViewsParameters params);

    List<ParticipationRequestDto> getEventParticipationRequestsByUser(long userId, long eventId);

    EventRequestStatusUpdateResult updateEventRequestStatus(long userId, long eventId,
                                                            EventRequestStatusUpdateRequest updateRequest);

    List<EventShortDto> getPublicEvents(PublicEventParam params);

    List<EventShortDto> getEventsByIds(List<Long> eventIds);

    EventFullDto updateByAdmin(Long id, UpdateEventAdminRequestDto dto);

    List<EventFullDto> searchByAdmin(AdminEventFilter f, Pageable page);
}
