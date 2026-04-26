package ru.practicum.explore.with.me.service.event;

import org.springframework.data.domain.Pageable;
import ru.practicum.explore.with.me.interaction.api.dto.event.*;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestDto;


import java.util.List;

public interface EventService {
    EventFullDto createEvent(long userId, NewEventDto eventDto);

    EventFullDto getPrivateEventById(long userId, long eventId);

    EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEvent);

    EventFullDto getPublicEventById(long userId, long eventId);

    EventFullDto getEventFullDto(long eventId);

    List<EventShortDto> getEventsByUser(long userId, int from, int count);

    List<ParticipationRequestDto> getEventParticipationRequestsByUser(long userId, long eventId);

    EventRequestStatusUpdateResult updateEventRequestStatus(long userId, long eventId,
                                                            EventRequestStatusUpdateRequest updateRequest);

    List<EventShortDto> getPublicEvents(PublicEventParam params);

    List<EventShortDto> getEventsByIds(List<Long> eventIds);

    EventFullDto updateByAdmin(Long id, UpdateEventAdminRequestDto dto);

    List<EventFullDto> searchByAdmin(AdminEventFilter f, Pageable page);

    void likeEvent(long userId, long eventId);

    List<EventShortDto> getRecommendationsForUser(long userId, int limit);
}
