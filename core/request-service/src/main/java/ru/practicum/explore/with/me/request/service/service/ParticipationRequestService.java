package ru.practicum.explore.with.me.request.service.service;


import ru.practicum.explore.with.me.interaction.api.dto.event.EventRequestCount;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.explore.with.me.interaction.api.dto.participation.CancelParticipationRequest;
import ru.practicum.explore.with.me.interaction.api.dto.participation.NewParticipationRequest;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestDto;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestStatus;

import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> find(Long userId);

    ParticipationRequestDto create(NewParticipationRequest newParticipationRequest);

    ParticipationRequestDto cancel(CancelParticipationRequest cancelParticipationRequest);

    boolean isParticipantApproved(Long userId, Long eventId);

    List<ParticipationRequestDto> findAllByIds(List<Long> requestIds);

    List<ParticipationRequestDto> findAllByEventId(long eventId);

    List<ParticipationRequestDto> findAllByEventIdAndStatus(long eventId, ParticipationRequestStatus status);

    void updateStatus(EventRequestStatusUpdateRequest updateRequest);

    List<EventRequestCount> getRequestsCountByEventId(List<Long> eventIds);
}
