package ru.practicum.explore.with.me.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.explore.with.me.interaction.api.client.user.UserClient;
import ru.practicum.explore.with.me.interaction.api.dto.event.*;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserDto;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserShortDto;
import ru.practicum.explore.with.me.interaction.api.exception.ConflictException;
import ru.practicum.explore.with.me.interaction.api.exception.NotFoundException;
import ru.practicum.explore.with.me.mapper.EventMapper;
import ru.practicum.explore.with.me.model.event.Event;

import ru.practicum.explore.with.me.repository.CategoryRepository;
import ru.practicum.explore.with.me.repository.EventRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventAdminService {
    private final EventRepository eventRepo;
    private final CategoryRepository categoryRepo;
    private final EventMapper mapper;
    private final EventService eventService;
    private final UserClient userClient;

    // GET /admin/events
    @Transactional(readOnly = true)
    public List<EventFullDto> search(AdminEventFilter f, Pageable page) {

        List<EventState> states = null;
        if (f.getStates() != null && !f.getStates().isEmpty()) {
            states = f.getStates().stream()
                    .map(String::toUpperCase)
                    .map(EventState::valueOf)
                    .toList();
        }

        Page<Event> events = eventRepo.searchForAdmin(
                emptyToNull(f.getUsers()),
                states,
                emptyToNull(f.getCategories()),
                f.getRangeStart(),
                f.getRangeEnd(),
                page
        );

        List<Event> eventList = events.getContent();
        if (eventList.isEmpty()) {
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

        LocalDateTime startStats = eventList.getFirst().getCreatedOn().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime endStats = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        EventStatistics stats = eventService.getEventStatistics(eventList, startStats, endStats);
        return events.stream()
                .map(event -> mapper.toFullDtoWithStats(event, stats, usersMap.get(event.getInitiatorId())))
                .toList();
    }

    // PATCH /admin/events/{id}
    @Transactional
    public EventFullDto update(Long id, UpdateEventAdminRequestDto dto) {
        Event event = eventRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("The required object was not found.",
                        "Event with id=" + id + " was not found"));

        mapper.updateFromAdmin(dto, event);

        if (dto.getCategory() != null) {
            event.setCategory(categoryRepo.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("The required object was not found.",
                            "Category with id=" + dto.getCategory() + " was not found")));
        }

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case PUBLISH_EVENT -> publish(event);
                case REJECT_EVENT -> reject(event);
            }
        }
        return mapper.toFullDto(eventRepo.save(event));
    }

    private <T> List<T> emptyToNull(List<T> list) {
        return (list == null || list.isEmpty()) ? null : list;
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
}