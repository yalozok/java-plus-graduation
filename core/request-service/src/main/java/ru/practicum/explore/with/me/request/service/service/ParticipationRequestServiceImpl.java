package ru.practicum.explore.with.me.request.service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.practicum.explore.with.me.interaction.api.exception.NotFoundException;
import ru.practicum.explore.with.me.request.service.model.ParticipationRequestMapper;
import ru.practicum.explore.with.me.request.service.model.ParticipationRequest;
import ru.practicum.explore.with.me.request.service.model.ParticipationRequestRepository;
import ru.practicum.explore.with.me.interaction.api.util.DataProvider;
import ru.practicum.explore.with.me.interaction.api.util.ExistenceValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ParticipationRequestServiceImpl implements ParticipationRequestService,
        ExistenceValidator<ParticipationRequest>, DataProvider<ParticipationRequestDto, ParticipationRequest> {

    private final ParticipationRequestRepository participationRequestRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final ParticipationRequestMapper participationRequestMapper;


    @Override
    public List<ParticipationRequestDto> find(Long userId) {
        userClient.findById(userId);
        return participationRequestRepository.findAllByRequesterId(userId).stream()
                .map(this::getDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto create(NewParticipationRequest newParticipationRequest) {
        Long requesterId = newParticipationRequest.getUserId();
        Long eventId = newParticipationRequest.getEventId();

        if (participationRequestRepository.existsByRequesterIdAndEventId(
                requesterId, eventId)) {
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
            List<ParticipationRequest> alreadyConfirmed = participationRequestRepository
                    .findAllByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
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

        return getDto(participationRequestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(CancelParticipationRequest cancelParticipationRequest) {
        ParticipationRequest request = participationRequestRepository
                .findById(cancelParticipationRequest.getRequestId())
                .orElseThrow(() -> new NotFoundException("The required object was not found.",
                            "ParticipationRequest with id=" + cancelParticipationRequest.getRequestId() +
                                    " was not found"));

        userClient.findById(cancelParticipationRequest.getUserId());
        if (request.getRequesterId() != cancelParticipationRequest.getUserId()) {
            throw new ConflictException("Request can be cancelled only by an owner",
                    "User with id=" + cancelParticipationRequest.getUserId() +
                            " is not an owner of request with id=" + cancelParticipationRequest.getRequestId());
        }

        ParticipationRequestDto result = participationRequestMapper.toDto(
                participationRequestRepository.findById(cancelParticipationRequest.getRequestId()).get());
        result.setStatus(ParticipationRequestStatus.CANCELED);
        participationRequestRepository.deleteById(cancelParticipationRequest.getRequestId());
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
    public void validateExists(Long id) {
        if (participationRequestRepository.findById(id).isEmpty()) {
            throw new NotFoundException("The required object was not found.",
                    "ParticipationRequest with id=" + id + " was not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isParticipantApproved(Long userId, Long eventId) {
        return participationRequestRepository
                .existsByRequesterIdAndEventIdAndStatus(
                        userId,
                        eventId,
                        ParticipationRequestStatus.CONFIRMED
                );
    }

    @Override
    public List<ParticipationRequestDto> findAllByIds(List<Long> requestIds) {
        List<ParticipationRequest> requests = participationRequestRepository.findAllById(requestIds);
        return requests.stream().map(participationRequestMapper::toDto).toList();
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventId(long eventId){
        List<ParticipationRequest> requests = participationRequestRepository.findAllByEventId(eventId);
        return requests.stream().map(participationRequestMapper::toDto).toList();
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventIdAndStatus(long eventId,
                                                            ParticipationRequestStatus status) {
        List<ParticipationRequest> requests = participationRequestRepository
                .findAllByEventIdAndStatus(eventId, status);
        return requests.stream().map(participationRequestMapper::toDto).toList();
    }

    @Override
    @Transactional
    public void updateStatus(EventRequestStatusUpdateRequest updateRequest){
        participationRequestRepository.updateStatus(
                updateRequest.getRequestIds(),
                updateRequest.getStatus());
    }

    @Override
    public List<EventRequestCount> getRequestsCountByEventId(List<Long> eventIds) {
        return participationRequestRepository.countGroupByEventId(eventIds);
    }
}
