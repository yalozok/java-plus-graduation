package ru.practicum.explore.with.me.request.service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventRequestCount;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestDto;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestStatus;
import ru.practicum.explore.with.me.logging.Loggable;
import ru.practicum.explore.with.me.request.service.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/requests/internal")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ParticipationRequestInternalController {
    private final ParticipationRequestService requestService;

    @GetMapping("/by-ids")
    @Loggable
    public List<ParticipationRequestDto> findAllByIds(@RequestParam List<Long> requestIds) {
        return requestService.findAllByIds(requestIds);
    }

    @GetMapping("/by-event")
    @Loggable
    public List<ParticipationRequestDto> findAllByEventId(@RequestParam long eventId) {
        return requestService.findAllByEventId(eventId);
    }

    @GetMapping("/by-event-status")
    @Loggable
    public List<ParticipationRequestDto> findAllByEventIdAndStatus(
            @RequestParam long eventId,
            @RequestParam ParticipationRequestStatus status) {
        return requestService.findAllByEventIdAndStatus(eventId, status);
    }

    @PostMapping("/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Loggable
    public void updateStatus(@RequestBody EventRequestStatusUpdateRequest updateRequest) {
        requestService.updateStatus(updateRequest);
    }

    @GetMapping("/count-by-events")
    @Loggable
    public List<EventRequestCount> getRequestsCountByEventId(@RequestParam List<Long> eventIds) {
        return requestService.getRequestsCountByEventId(eventIds);
    }

    @GetMapping("/approved")
    @Loggable
    public boolean isParticipantApproved(@RequestParam long requestId, @RequestParam long eventId) {
        return requestService.isParticipantApproved(requestId, eventId);
    }
}
