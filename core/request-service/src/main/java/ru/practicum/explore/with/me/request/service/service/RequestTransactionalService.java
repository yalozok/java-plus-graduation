package ru.practicum.explore.with.me.request.service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventRequestCount;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestStatus;
import ru.practicum.explore.with.me.interaction.api.exception.NotFoundException;
import ru.practicum.explore.with.me.interaction.api.util.ExistenceValidator;
import ru.practicum.explore.with.me.request.service.model.ParticipationRequest;
import ru.practicum.explore.with.me.request.service.model.ParticipationRequestRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestTransactionalService implements ExistenceValidator<ParticipationRequest> {
    private final ParticipationRequestRepository participationRequestRepository;

    @Transactional(readOnly = true)
    public List<ParticipationRequest> findAllByUserId(Long userId) {
        return participationRequestRepository.findAllByRequesterId(userId);
    }

    @Transactional
    public boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId) {
        return participationRequestRepository.existsByRequesterIdAndEventId(
                requesterId, eventId);
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequest> getAllByEventIdAndStatus(Long eventId, ParticipationRequestStatus status) {
        return participationRequestRepository.findAllByEventIdAndStatus(eventId, status);
    }

    @Transactional
    public ParticipationRequest saveRequest(ParticipationRequest participationRequest) {
        return participationRequestRepository.save(participationRequest);
    }

    @Transactional(readOnly = true)
    public ParticipationRequest findById(Long id) {
        return participationRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The required object was not found.",
                        "ParticipationRequest with id=" + id + " was not found"));
    }

    @Transactional
    public void delete(Long id) {
        participationRequestRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean isParticipantApproved(Long userId, Long eventId) {
        return participationRequestRepository
                .existsByRequesterIdAndEventIdAndStatus(
                        userId,
                        eventId,
                        ParticipationRequestStatus.CONFIRMED
                );
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequest> findAllById(List<Long> requestIds) {
        return participationRequestRepository.findAllById(requestIds);
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequest> findAllByEventId(Long eventId) {
        return participationRequestRepository.findAllByEventId(eventId);
    }

    @Transactional
    public void updateStatus(List<Long> requestId, ParticipationRequestStatus status) {
        participationRequestRepository.updateStatus(requestId, status);
    }

    @Transactional(readOnly = true)
    public List<EventRequestCount> getRequestCountByEventId(List<Long> eventIds) {
        return participationRequestRepository.countGroupByEventId(eventIds);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateExists(Long id) {
        if (participationRequestRepository.findById(id).isEmpty()) {
            throw new NotFoundException("The required object was not found.",
                    "ParticipationRequest with id=" + id + " was not found");
        }
    }
}
