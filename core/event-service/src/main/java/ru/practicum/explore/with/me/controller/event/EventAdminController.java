package ru.practicum.explore.with.me.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore.with.me.interaction.api.dto.event.AdminEventFilter;
import ru.practicum.explore.with.me.interaction.api.dto.event.AdminEventSearchRequestDto;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventFullDto;
import ru.practicum.explore.with.me.interaction.api.dto.event.UpdateEventAdminRequestDto;
import ru.practicum.explore.with.me.logging.Loggable;
import ru.practicum.explore.with.me.service.event.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
@Validated
public class EventAdminController {
    private final EventService service;

    @GetMapping
    @Loggable
    public List<EventFullDto> searchEvents(@ModelAttribute AdminEventSearchRequestDto req) {
        Pageable page = PageRequest.of(req.getFrom() / req.getSize(), req.getSize());
        AdminEventFilter f = new AdminEventFilter(
                req.getUsers(), req.getStates(), req.getCategories(),
                req.getRangeStart(), req.getRangeEnd());
        return service.searchByAdmin(f, page);
    }

    @PatchMapping("/{eventId}")
    @Loggable
    public EventFullDto updateEvent(@PathVariable @PositiveOrZero @NotNull Long eventId,
                                    @RequestBody @Valid UpdateEventAdminRequestDto dto) {
        return service.updateByAdmin(eventId, dto);
    }
}
