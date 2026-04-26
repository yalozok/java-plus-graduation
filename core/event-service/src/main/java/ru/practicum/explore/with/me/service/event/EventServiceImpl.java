package ru.practicum.explore.with.me.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ru.practicum.explore.with.me.interaction.api.client.request.RequestClient;
import ru.practicum.explore.with.me.interaction.api.client.user.UserClient;
import ru.practicum.explore.with.me.interaction.api.dto.event.*;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestDto;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestStatus;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserDto;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserShortDto;
import ru.practicum.explore.with.me.interaction.api.exception.BadRequestException;
import ru.practicum.explore.with.me.interaction.api.exception.ConflictException;
import ru.practicum.explore.with.me.mapper.EventMapper;
import ru.practicum.explore.with.me.mapper.LocationMapper;
import ru.practicum.explore.with.me.model.category.Category;
import ru.practicum.explore.with.me.model.event.Event;
import ru.practicum.explore.with.me.model.event.Location;
import ru.practicum.stats.client.StatClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventTransactionalService eventTransactionalService;
    private final UserClient userClient;
    private final EventMapper eventMapper;
    private final RequestClient requestClient;
    private final LocationMapper locationMapper;
    private final StatClient statClient;

    @Override
    public EventFullDto createEvent(long userId, NewEventDto eventDto) {
        UserShortDto userDto = userClient.findById(userId);
        long categoryId = eventDto.getCategory();
        Category category = eventTransactionalService.getCategoryById(categoryId);

        Event event = eventMapper.toModel(eventDto);
        event.setInitiatorId(userDto.getId());
        event.setCategory(category);
        event.setState(EventState.PENDING);
        Event eventSaved = eventTransactionalService.saveEvent(event);
        EventFullDto eventFullDto = eventMapper.toFullDto(eventSaved);
        eventFullDto.setRating(0D);
        eventFullDto.setInitiator(userDto);
        return eventFullDto;
    }

    private EventFullDto toEventFullDto(Event event) {
        EventStatistics stats = getEventStatistics(List.of(event));
        UserShortDto userDto = userClient.findById(event.getInitiatorId());
        return eventMapper.toFullDtoWithStats(event, stats, userDto);
    }

    private Event getEventIfInitiatedByUser(long userId, long eventId) {
        userClient.findById(userId);
        return eventTransactionalService.getEventByIdAndUserId(eventId, userId);
    }

    @Override
    public EventFullDto getPrivateEventById(long userId, long eventId) {
        Event event = getEventIfInitiatedByUser(userId, eventId);
        return toEventFullDto(event);
    }

    @Override
    public EventFullDto getEventFullDto(long eventId) {
        Event event = eventTransactionalService.getEventById(eventId);
        return toEventFullDto(event);
    }

    @Override
    public EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEvent) {
        Event event = getEventIfInitiatedByUser(userId, eventId);

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "Only pending or canceled events can be changed");
        }

        if (updateEvent.getCategory() != null) {
            long categoryId = updateEvent.getCategory();
            Category category = eventTransactionalService.getCategoryById(categoryId);
            event.setCategory(category);
        }

        if (updateEvent.getStateAction() != null) {
            UpdateEventUserAction action = updateEvent.getStateAction();
            switch (action) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
            }
        }

        if (updateEvent.getAnnotation() != null) {
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getDescription() != null) {
            event.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getTitle() != null) {
            event.setTitle(updateEvent.getTitle());
        }
        if (updateEvent.getEventDate() != null) {
            event.setEventDate(updateEvent.getEventDate());
        }
        if (updateEvent.getLocation() != null) {
            Location location = locationMapper.toEntity(updateEvent.getLocation());
            event.setLocation(location);
        }
        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        eventTransactionalService.saveEvent(event);
        return toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getEventsByUser(long userId, int from, int count) {
        UserShortDto userDto = userClient.findById(userId);
        List<Event> events = eventTransactionalService.getEventsByUserId(userId, from, count);
        if (events.isEmpty()) {
            return List.of();
        }
        EventStatistics stats = getEventStatistics(events);
        return events.stream()
                .map(event -> eventMapper.toShortDtoWithStats(event, stats, userDto))
                .toList();
    }

    @Override
    public List<EventShortDto> getEventsByIds(List<Long> eventIds) {
        List<Event> events = eventTransactionalService.getEventsByIds(eventIds);

        Map<Long, UserShortDto> userMap = getUserMapFromEventList(events);
        return events.stream()
                .map(event -> {
                    EventShortDto eventShortDto = eventMapper.toShortDto(event);
                    eventShortDto.setInitiator(userMap.get(event.getInitiatorId()));
                    return eventShortDto;
                })
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipationRequestsByUser(long userId, long eventId) {
        getEventIfInitiatedByUser(userId, eventId);
        return requestClient.findAllByEventId(eventId);
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequestStatus(long userId, long eventId,
                                                                   EventRequestStatusUpdateRequest updateRequest) {
        Event event = getEventIfInitiatedByUser(userId, eventId);
        List<ParticipationRequestDto> requestsByEventId = requestClient.findAllByIds(updateRequest.getRequestIds());

        if (event.getParticipantLimit() == 0 || !event.isRequestModeration()) {
            return new EventRequestStatusUpdateResult(requestsByEventId, List.of());
        }

        List<ParticipationRequestDto> alreadyConfirmed = requestClient
                .findAllByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        AtomicInteger remainingSpots = new AtomicInteger(event.getParticipantLimit() - alreadyConfirmed.size());

        if (remainingSpots.get() <= 0) {
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "The participant limit has been reached");
        }

        requestsByEventId.forEach(request -> {
            if (request.getStatus() != ParticipationRequestStatus.PENDING) {
                throw new ConflictException("For the requested operation the conditions are not met.",
                        "It's not allowed to change the status of the request");
            }
        });

        List<ParticipationRequestDto> confirmedDto = new ArrayList<>();
        List<ParticipationRequestDto> rejectedDto = new ArrayList<>();

        requestsByEventId.forEach(request -> {
            if (remainingSpots.get() > 0 && updateRequest.getStatus() == ParticipationRequestStatus.CONFIRMED) {
                request.setStatus(ParticipationRequestStatus.CONFIRMED);
                confirmedDto.add(request);
                remainingSpots.getAndDecrement();
            } else {
                request.setStatus(ParticipationRequestStatus.REJECTED);
                rejectedDto.add(request);
            }
        });

        if (!confirmedDto.isEmpty()) {
            requestClient.updateStatus(
                    new EventRequestStatusUpdateRequest(
                            confirmedDto.stream().map(ParticipationRequestDto::getId).toList(),
                            ParticipationRequestStatus.CONFIRMED));
        }
        if (!rejectedDto.isEmpty()) {
            requestClient.updateStatus(
                    new EventRequestStatusUpdateRequest(
                            rejectedDto.stream().map(ParticipationRequestDto::getId).toList(),
                            ParticipationRequestStatus.REJECTED));
        }
        if (remainingSpots.get() == 0) {
            List<Long> pendingIds = requestClient
                    .findAllByEventIdAndStatus(eventId, ParticipationRequestStatus.PENDING)
                    .stream().map(ParticipationRequestDto::getId).toList();
            if (!pendingIds.isEmpty()) {
                requestClient.updateStatus(new EventRequestStatusUpdateRequest(pendingIds, ParticipationRequestStatus.REJECTED));
            }
        }
        return new EventRequestStatusUpdateResult(confirmedDto, rejectedDto);
    }

    public EventFullDto getPublicEventById(long userId, long eventId) {
        Event event = eventTransactionalService.getEventByIdAndState(eventId, EventState.PUBLISHED);
        statClient.sendViewAction(userId, eventId);
        return toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getPublicEvents(PublicEventParam params) {
        if (params.getRangeStart() != null && params.getRangeEnd() != null
                && params.getRangeStart().isAfter(params.getRangeEnd())) {
            throw new BadRequestException("Start date must be before end date",
                    "Start: " + params.getRangeStart() + " End: " + params.getRangeEnd());
        }

        List<Event> events = eventTransactionalService.getPublicEvents(params);
        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, UserShortDto> usersMap = getUserMapFromEventList(events);
        EventStatistics stats = getEventStatistics(events);
        return events.stream()
                .map(event -> eventMapper.toShortDtoWithStats(event, stats, usersMap.get(event.getInitiatorId())))
                .toList();
    }

    @Override
    public void likeEvent(long userId, long eventId) {
        userClient.findById(userId);
        eventTransactionalService.getEventByIdAndState(eventId,  EventState.PUBLISHED);
        if(!requestClient.isParticipantApproved(userId, eventId)) {
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "You can only like events you has attended: " + eventId);
        }
        statClient.sendLikeAction(eventId, userId);
    }

    @Override
    public List<EventShortDto> getRecommendationsForUser(long userId, int limit) {
        userClient.findById(userId);
        Map<Long, Double> recommendations = statClient.getRecommendationsForUser(userId, limit);
        if (recommendations.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = recommendations.keySet().stream().toList();
        List<Event> events = eventTransactionalService.getEventsByIds(eventIds);
        Map<Long, UserShortDto> usersMap = getUserMapFromEventList(events);
        EventStatistics stats = getEventStatistics(events);
        return events.stream()
                .map(event -> eventMapper.toShortDtoWithStats(event, stats, usersMap.get(event.getInitiatorId())))
                .toList();
    }

    // GET /admin/events
    @Override
    public List<EventFullDto> searchByAdmin(AdminEventFilter f, Pageable page) {
        List<Event> eventList = eventTransactionalService.getEventsAdmin(f, page);
        if (eventList.isEmpty()) {
            return List.of();
        }

        Map<Long, UserShortDto> usersMap = getUserMapFromEventList(eventList);
        EventStatistics stats = getEventStatistics(eventList);
        return eventList.stream()
                .map(event -> eventMapper.toFullDtoWithStats(event, stats, usersMap.get(event.getInitiatorId())))
                .toList();
    }

    private Map<Long, UserShortDto> getUserMapFromEventList(List<Event> events) {
        List<Long> initiatorIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .toList();

        List<UserDto> users = userClient.find(initiatorIds, 0, initiatorIds.size());
        return users.stream().collect(Collectors.toMap(
                UserDto::getId,
                user -> new UserShortDto(user.getId(), user.getName())
        ));
    }

    // PATCH /admin/events/{id}
    @Override
    public EventFullDto updateByAdmin(Long id, UpdateEventAdminRequestDto dto) {
        Event event = eventTransactionalService.getEventById(id);
        eventMapper.updateFromAdmin(dto, event);

        if (dto.getCategory() != null) {
            event.setCategory(eventTransactionalService.getCategoryById(dto.getCategory()));
        }

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case PUBLISH_EVENT -> publish(event);
                case REJECT_EVENT -> reject(event);
            }
        }
        return eventMapper.toFullDto(eventTransactionalService.saveEvent(event));
    }

    private void publish(Event e) {
        if (e.getState() != EventState.PENDING) {
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "Cannot publish the event because it's not in the right state: " + e.getState());
        }
        if (e.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "Event start time must be at least 1 hour from publication time");
        }
        e.setState(EventState.PUBLISHED);
        e.setPublishedOn(LocalDateTime.now());
    }

    private void reject(Event e) {
        if (e.getState() == EventState.PUBLISHED) {
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "Cannot reject the event because it's already published");
        }
        e.setState(EventState.CANCELED);
    }

    private EventStatistics getEventStatistics(List<Event> events) {
        if (events.isEmpty()) {
            return new EventStatistics(Map.of(), Map.of());
        }
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Double> ratings = statClient.getInteractionsCount(eventIds);
        Map<Long, Integer> confirmedRequests = requestClient.getRequestsCountByEventId(eventIds);
        return new EventStatistics(ratings, confirmedRequests);
    }
}
