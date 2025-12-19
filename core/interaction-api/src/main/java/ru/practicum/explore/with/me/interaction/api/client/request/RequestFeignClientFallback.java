package ru.practicum.explore.with.me.interaction.api.client.request;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventRequestCount;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestDto;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RequestFeignClientFallback implements RequestClient{
    @GetMapping("/by-ids")
    @Override
    public List<ParticipationRequestDto> findAllByIds(@RequestParam List<Long> requestIds) {
        return Collections.emptyList();
    }

    @GetMapping("/by-event")
    @Override
    public List<ParticipationRequestDto> findAllByEventId(@RequestParam long eventId) {
        return Collections.emptyList();
    }

    @GetMapping("/by-event-status")
    @Override
    public List<ParticipationRequestDto> findAllByEventIdAndStatus(
            @RequestParam long eventId,
            @RequestParam ParticipationRequestStatus status) {
        return Collections.emptyList();
    }

    @PostMapping("/status")
    @Override
    public void updateStatus(@RequestBody EventRequestStatusUpdateRequest updateRequest){
    }

    @GetMapping("/count-by-events")
    @Override
    public Map<Long, Integer> getRequestsCountByEventId(@RequestParam List<Long> eventIds) {
        return Collections.emptyMap();
    }

    @GetMapping("/approved")
    @Override
    public boolean isParticipantApproved(@RequestParam long requestId, @RequestParam long eventId) {
        return false;
    }
}
