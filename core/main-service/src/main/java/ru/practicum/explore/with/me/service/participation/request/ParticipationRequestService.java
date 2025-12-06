package ru.practicum.explore.with.me.service.participation.request;


import ru.practicum.explore.with.me.interaction.api.dto.participation.CancelParticipationRequest;
import ru.practicum.explore.with.me.interaction.api.dto.participation.NewParticipationRequest;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> find(Long userId);

    ParticipationRequestDto create(NewParticipationRequest newParticipationRequest);

    ParticipationRequestDto cancel(CancelParticipationRequest cancelParticipationRequest);

    boolean isParticipantApproved(Long userId, Long eventId);
}
