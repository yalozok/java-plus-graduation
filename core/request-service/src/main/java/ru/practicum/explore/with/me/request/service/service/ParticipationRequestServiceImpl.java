package ru.practicum.explore.with.me.request.service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.explore.with.me.interaction.api.client.event.EventClient;
import ru.practicum.explore.with.me.interaction.api.client.user.UserClient;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventFullDto;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventRequestCount;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.explore.with.me.interaction.api.dto.participation.CancelParticipationRequest;
import ru.practicum.explore.with.me.interaction.api.dto.participation.NewParticipationRequest;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestDto;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestStatus;
import ru.practicum.explore.with.me.interaction.api.exception.ConflictException;
import ru.practicum.explore.with.me.request.service.model.ParticipationRequestMapper;
import ru.practicum.explore.with.me.request.service.model.ParticipationRequest;
import ru.practicum.explore.with.me.interaction.api.util.DataProvider;
import ru.practicum.stats.client.StatClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ParticipationRequestServiceImpl implements ParticipationRequestService, DataProvider<ParticipationRequestDto, ParticipationRequest> {
    private final UserClient userClient;
    private final EventClient eventClient;
    private final StatClient statClient;
    private final ParticipationRequestMapper participationRequestMapper;
    private final RequestTransactionalService requestTransactionalService;

    @Override
    public List<ParticipationRequestDto> find(Long userId) {
        userClient.findById(userId);
        return requestTransactionalService.findAllByUserId(userId)
                .stream()
                .map(this::getDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto create(NewParticipationRequest newParticipationRequest) {
        Long requesterId = newParticipationRequest.getUserId();
        Long eventId = newParticipationRequest.getEventId();
        if (requestTransactionalService.existsByRequesterIdAndEventId(requesterId, eventId)) {
            throw new ConflictException("Duplicate request.", "participationRequest with requesterId: " + requesterId +
                    ", and eventId: " + eventId + " already exists");
        }

        EventFullDto event = eventClient.getEventById(eventId);
        if (event.getPublishedOn() == null) {
            throw new ConflictException("Can't create participation request for unpublished event.",
                    "event with id: " + eventId + " is not published yet");
        }

        userClient.findById(requesterId);
        if (event.getInitiator().getId().equals(requesterId)) {
            throw new ConflictException("Initiator can't create participation request.", "requesterId: "
                    + requesterId + " equals to initiatorId: " + event.getInitiator().getId());
        }

        if (event.getParticipantLimit() != 0) {
            List<ParticipationRequest> alreadyConfirmed = requestTransactionalService
                    .getAllByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
            AtomicInteger remainingSpots = new AtomicInteger(event.getParticipantLimit() - alreadyConfirmed.size());
            if (remainingSpots.get() <= 0) {
                throw new ConflictException("Participant limit is reached.", "event with id: " + eventId +
                        " has participant limit of: " + event.getParticipantLimit());
            }
        }

        ParticipationRequest request = mapEntity(newParticipationRequest);
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(ParticipationRequestStatus.CONFIRMED);
        }
        statClient.sendRegisterAction(requesterId, eventId);
        return getDto(requestTransactionalService.saveRequest(request));
    }

    @Override
    public ParticipationRequestDto cancel(CancelParticipationRequest cancelParticipationRequest) {
        Long userId = cancelParticipationRequest.getUserId();
        Long requestId = cancelParticipationRequest.getRequestId();
        ParticipationRequest request = requestTransactionalService.findById(requestId);

        userClient.findById(userId);
        if (request.getRequesterId() != userId) {
            throw new ConflictException("Request can be cancelled only by an owner",
                    "User with id=" + userId + " is not an owner of request with id=" + requestId);
        }

        ParticipationRequestDto result = participationRequestMapper.toDto(request);
        result.setStatus(ParticipationRequestStatus.CANCELED);
        requestTransactionalService.delete(requestId);
        return result;
    }

    private ParticipationRequest mapEntity(NewParticipationRequest newParticipationRequest) {
        Long userId = newParticipationRequest.getUserId();
        Long eventId = newParticipationRequest.getEventId();
        userClient.findById(userId);
        eventClient.getEventById(eventId);

        return ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .requesterId(userId)
                .eventId(eventId)
                .status(ParticipationRequestStatus.PENDING)
                .build();
    }

    @Override
    public ParticipationRequestDto getDto(ParticipationRequest entity) {
        return participationRequestMapper.toDto(entity);
    }

    @Override
    public boolean isParticipantApproved(Long userId, Long eventId) {
        return requestTransactionalService.isParticipantApproved(userId, eventId);
    }

    @Override
    public List<ParticipationRequestDto> findAllByIds(List<Long> requestIds) {
        List<ParticipationRequest> requests = requestTransactionalService.findAllById(requestIds);
        return requests.stream().map(participationRequestMapper::toDto).toList();
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventId(long eventId) {
        List<ParticipationRequest> requests = requestTransactionalService.findAllByEventId(eventId);
        return requests.stream().map(participationRequestMapper::toDto).toList();
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventIdAndStatus(long eventId,
                                                                   ParticipationRequestStatus status) {
        List<ParticipationRequest> requests = requestTransactionalService
                .getAllByEventIdAndStatus(eventId, status);
        return requests.stream().map(participationRequestMapper::toDto).toList();
    }

    @Override
    public void updateStatus(EventRequestStatusUpdateRequest updateRequest) {
        requestTransactionalService.updateStatus(updateRequest.getRequestIds(), updateRequest.getStatus());
    }

    @Override
    public Map<Long, Integer> getRequestsCountByEventId(List<Long> eventIds) {
        List<EventRequestCount> confirmedRequests = requestTransactionalService.getRequestCountByEventId(eventIds);
        return confirmedRequests.stream().collect(
                Collectors.toMap(
                        EventRequestCount::eventId,
                        r -> r.count().intValue()
                )
        );
    }
}
