package ru.practicum.explore.with.me.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.with.me.interaction.api.dto.event.AdminEventFilter;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventPublicSort;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventState;
import ru.practicum.explore.with.me.interaction.api.dto.event.PublicEventParam;
import ru.practicum.explore.with.me.interaction.api.exception.ConflictException;
import ru.practicum.explore.with.me.interaction.api.exception.NotFoundException;
import ru.practicum.explore.with.me.interaction.api.util.ExistenceValidator;
import ru.practicum.explore.with.me.model.category.Category;
import ru.practicum.explore.with.me.model.event.Event;
import ru.practicum.explore.with.me.repository.CategoryRepository;
import ru.practicum.explore.with.me.repository.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventTransactionalService implements ExistenceValidator<Event> {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Event getEventById(long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("The required object was not found.",
                        "Event with id=" + eventId + " was not found"));
    }

    @Transactional
    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Event getEventByIdAndUserId(long eventId, long userId) {
        return eventRepository.getEventByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new ConflictException("For the requested operation the conditions are not met.",
                        "Only initiator of event can can manipulate with it"));
    }

    @Transactional(readOnly = true)
    public List<Event> getEventsByUserId(long userId, int from, int count) {
        Pageable pageable = PageRequest.of(from, count, Sort.by("createdOn").ascending());
        return eventRepository.findEventsByUser(userId, pageable).getContent();
    }

    @Transactional(readOnly = true)
    public List<Event> getEventsByIds(List<Long> eventIds) {
        return eventRepository.findByIdIn(eventIds);
    }

    @Transactional(readOnly = true)
    public List<Event> getPublicEvents(PublicEventParam params) {
        Pageable pageable = PageRequest.of(
                params.getFrom() / params.getSize(),
                params.getSize(),
                getSort(params.getSort())
        );

        Page<Event> page = eventRepository.findPublicEvents(
                params.getText(),
                params.getCategories(),
                params.getPaid(),
                params.getRangeStart(),
                params.getRangeEnd(),
                pageable);
        return page.getContent();
    }

    private Sort getSort(EventPublicSort sort) {
        if (sort == null) return Sort.unsorted();
        return switch (sort) {
            case EVENT_DATE -> Sort.by("eventDate").ascending();
            case VIEWS -> Sort.by("views").descending();
        };
    }

    @Transactional(readOnly = true)
    public List<Event> getEventsAdmin(AdminEventFilter f, Pageable page) {
        List<EventState> states = null;
        if (f.getStates() != null && !f.getStates().isEmpty()) {
            states = f.getStates().stream()
                    .map(String::toUpperCase)
                    .map(EventState::valueOf)
                    .toList();
        }

        Page<Event> events = eventRepository.searchForAdmin(
                emptyToNull(f.getUsers()),
                states,
                emptyToNull(f.getCategories()),
                f.getRangeStart(),
                f.getRangeEnd(),
                page
        );
        return events.getContent();
    }

    private <T> List<T> emptyToNull(List<T> list) {
        return (list == null || list.isEmpty()) ? null : list;
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("The required object was not found.",
                        "Category with id=" + categoryId + " was not found"));
    }

    @Transactional(readOnly = true)
    public Event getEventByIdAndState(Long eventId, EventState state) {
        return eventRepository.getEventByIdAndState(eventId, state)
                .orElseThrow(() -> new NotFoundException("The required object was not found.",
                        "Event with id=" + eventId + " was not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public void validateExists(Long id) {
        if (eventRepository.findById(id).isEmpty()) {
            throw new NotFoundException("The required object was not found.",
                    "Event with id=" + id + " was not found");
        }
    }

    @Transactional(readOnly = true)
    public Event getPastAndPublishedEventById(long eventId) {
        return eventRepository.getPastAndPublishedEventById(eventId)
                .orElseThrow(() -> new NotFoundException("The required object was not found.",
                        "Event with id=" + eventId + " was not found"));
    }
}
