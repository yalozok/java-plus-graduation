package ru.practicum.explore.with.me.interaction.api.client.request;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.with.me.interaction.api.client.user.UserFeignClientFallback;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventRequestCount;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestDto;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestStatus;

import java.util.List;

@FeignClient(name = "request-service",
        path = "/requests/internal",
        configuration = RequestFeignConfig.class,
        fallback = RequestFeignClientFallback.class)
public interface RequestClient {

    @GetMapping("/by-ids")
    List<ParticipationRequestDto> findAllByIds(@RequestParam List<Long> requestIds);

    @GetMapping("/by-event")
    List<ParticipationRequestDto> findAllByEventId(@RequestParam long eventId);

    @GetMapping("/by-event-status")
    List<ParticipationRequestDto> findAllByEventIdAndStatus(
            @RequestParam long eventId,
            @RequestParam ParticipationRequestStatus status);

    @PostMapping("/status")
    void updateStatus(@RequestBody EventRequestStatusUpdateRequest updateRequest);

    @GetMapping("/count-by-events")
    List<EventRequestCount> getRequestsCountByEventId(@RequestParam List<Long> eventIds);

    @GetMapping("/approved")
    boolean isParticipantApproved(@RequestParam long requestId, @RequestParam long eventId);
}
