package ru.practicum.explore.with.me.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.explore.with.me.interaction.api.client.request.RequestClient;
import ru.practicum.explore.with.me.interaction.api.client.user.UserClient;
import ru.practicum.explore.with.me.interaction.api.dto.event.*;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestDto;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestStatus;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserDto;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserShortDto;
import ru.practicum.explore.with.me.interaction.api.exception.BadRequestException;
import ru.practicum.explore.with.me.interaction.api.exception.ConflictException;
import ru.practicum.explore.with.me.interaction.api.exception.NotFoundException;
import ru.practicum.explore.with.me.mapper.EventMapper;
import ru.practicum.explore.with.me.mapper.LocationMapper;
import ru.practicum.explore.with.me.model.category.Category;
import ru.practicum.explore.with.me.model.event.Event;
import ru.practicum.explore.with.me.model.event.EventViewsParameters;
import ru.practicum.explore.with.me.model.event.Location;

import ru.practicum.explore.with.me.repository.CategoryRepository;
import ru.practicum.explore.with.me.repository.EventRepository;
import ru.practicum.explore.with.me.interaction.api.util.ExistenceValidator;
import ru.practicum.explore.with.me.util.StatsGetter;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements ExistenceValidator<Event>, EventService {
    private final EventRepository eventRepository;
    private final UserClient userClient;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final StatsGetter statsGetter;
    private final RequestClient requestClient;
    private final LocationMapper locationMapper;

    @Transactional
    @Override
    public EventFullDto createEvent(long userId, NewEventDto eventDto) {
        UserShortDto userDto = userClient.findById(userId);
        long categoryId = eventDto.getCategory();
        Category category = findCategoryByIdOrElseThrow(categoryId);

        Event event = eventMapper.toModel(eventDto);
        event.setInitiatorId(userDto.getId());
        event.setCategory(category);
        event.setState(EventState.PENDING);
        Event eventSaved = eventRepository.save(event);
        EventFullDto eventFullDto = eventMapper.toFullDto(eventSaved);
        eventFullDto.setViews(0L);
        eventFullDto.setInitiator(userDto);
        return eventFullDto;
    }

    private Event getEventById(long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("The required object was not found.",
                        "Event with id=" + eventId + " was not found"));
    }

    private EventFullDto toEventFullDto(Event event) {
        LocalDateTime startStats = event.getCreatedOn().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime endStats = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        EventStatistics stats = getEventStatistics(List.of(event), startStats, endStats);
        UserShortDto userDto = userClient.findById(event.getInitiatorId());
        return eventMapper.toFullDtoWithStats(event, stats, userDto);
    }

    private Event getEventIfInitiatedByUser(long userId, long eventId) {
        userClient.findById(userId);
        Event event = getEventById(eventId);

        if (event.getInitiatorId() != userId) {
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "Only initiator of event can can manipulate with it");
        }
        return event;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPrivateEventById(long userId, long eventId) {
        Event event = getEventIfInitiatedByUser(userId, eventId);
        return toEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventFullDto(long eventId) {
        Event event = getEventById(eventId);
        return toEventFullDto(event);
    }

    @Transactional
    @Override
    public EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEvent) {
        Event event = getEventIfInitiatedByUser(userId, eventId);

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "Only pending or canceled events can be changed");
        }

        if (updateEvent.getCategory() != null) {
            long categoryId = updateEvent.getCategory();
            Category category = findCategoryByIdOrElseThrow(categoryId);
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
        eventRepository.save(event);
        return toEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByUser(long userId, int from, int count) {
        UserShortDto userDto = userClient.findById(userId);
        Pageable pageable = PageRequest.of(from, count, Sort.by("createdOn").ascending());
        List<Event> events = eventRepository.findEventsByUser(userId, pageable).getContent();
        if (events.isEmpty()) {
            return List.of();
        }
        LocalDateTime startStats = events.getFirst().getCreatedOn().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime endStats = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        EventStatistics stats = getEventStatistics(events, startStats, endStats);
        return events.stream()
                .map(event -> eventMapper.toShortDtoWithStats(event, stats, userDto))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventParticipationRequestsByUser(long userId, long eventId) {
        getEventIfInitiatedByUser(userId, eventId);
        return requestClient.findAllByEventId(eventId);
    }

    @Override
    @Transactional
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

    @Transactional(readOnly = true)
    public EventFullDto getPublicEventById(long eventId) {
        Event event = getEventById(eventId);
        if(event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("The required object was not found.",
                    "Event with id=" + eventId + " was not found");
        }
        return toEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getPublicEvents(PublicEventParam params) {
        if (params.getRangeStart() != null && params.getRangeEnd() != null
                && params.getRangeStart().isAfter(params.getRangeEnd())) {
            throw new BadRequestException("Start date must be before end date",
                    "Start: " + params.getRangeStart() + " End: " + params.getRangeEnd());
        }

        Pageable pageable = PageRequest.of(
                params.getFrom() / params.getSize(),
                params.getSize(),
                getSort(params.getSort())
        );

        // Get events from a repository
        Page<Event> page = eventRepository.findPublicEvents(
                params.getText(),
                params.getCategories(),
                params.getPaid(),
                params.getRangeStart(),
                params.getRangeEnd(),
                pageable);

        List<Event> events = page.getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> initiatorIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .toList();

        List<UserDto> users = userClient.find(initiatorIds, 0, initiatorIds.size());
        Map<Long, UserShortDto> usersMap = users.stream().collect(Collectors.toMap(
                UserDto::getId,
                user -> new UserShortDto(user.getId(), user.getName())
        ));

        LocalDateTime startStats = params.getRangeStart() != null ? params.getRangeStart().truncatedTo(ChronoUnit.SECONDS)
                : events.getFirst().getCreatedOn().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime endStats = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        EventStatistics stats = getEventStatistics(events, startStats, endStats);
        return events.stream()
                .map(event -> eventMapper.toShortDtoWithStats(event, stats, usersMap.get(event.getInitiatorId())))
                .toList();
    }

    @Override
    public Map<Long, Long> getEventViews(EventViewsParameters params) {
        List<ViewStats> stats = statsGetter.getEventViewStats(params);
        Map<Long, Long> views = new HashMap<>();
        if (stats != null) {
            for (ViewStats stat : stats) {
                Long eventId = extractId(stat.getUri());
                if (eventId != null) {
                    views.put(eventId, stat.getHits());
                }
            }
        }
        return views;
    }

    @Override
    public Map<Long, Integer> getConfirmedRequests(List<Long> eventIds) {
        List<EventRequestCount> confirmedRequests = requestClient.getRequestsCountByEventId(eventIds);
        return confirmedRequests.stream().collect(
                Collectors.toMap(
                        EventRequestCount::eventId,
                        r -> r.count().intValue()
                )
        );
    }

    private Category findCategoryByIdOrElseThrow(long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("The required object was not found.", "Category with id=" + categoryId + " was not found"));
    }

    private Long extractId(String uri) {
        try {
            String[] parts = uri.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            return null;
        }
    }

    private Sort getSort(EventPublicSort sort) {
        if (sort == null) return Sort.unsorted();
        return switch (sort) {
            case EVENT_DATE -> Sort.by("eventDate").ascending();
            case VIEWS -> Sort.by("views").descending();
        };
    }

    @Override
    public void validateExists(Long id) {
        if (eventRepository.findById(id).isEmpty()) {
            throw new NotFoundException("The required object was not found.",
                    "Event with id=" + id + " was not found");
        }
    }

    @Override
    public EventStatistics getEventStatistics(List<Event> events, LocalDateTime start, LocalDateTime end) {
        if (events.isEmpty()) {
            return new EventStatistics(Map.of(), Map.of());
        }

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        EventViewsParameters params = EventViewsParameters.builder()
                .start(start)
                .end(end)
                .eventIds(eventIds).unique(true).build();
        Map<Long, Long> viewStats = getEventViews(params);
        Map<Long, Integer> confirmedRequests = getConfirmedRequests(eventIds);
        return new EventStatistics(viewStats, confirmedRequests);
    }
}
