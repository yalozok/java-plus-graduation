package ru.practicum.explore.with.me.request.service.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore.with.me.interaction.api.dto.participation.CancelParticipationRequest;
import ru.practicum.explore.with.me.interaction.api.dto.participation.NewParticipationRequest;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestDto;
import ru.practicum.explore.with.me.logging.Loggable;
import ru.practicum.explore.with.me.request.service.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
public class ParticipationRequestPrivateController {
    private final ParticipationRequestService service;

    @GetMapping
    @Loggable
    public List<ParticipationRequestDto> find(@PathVariable
                                              @NotNull(message = "must not be null")
                                              @PositiveOrZero(message = "must be positive or zero")
                                              Long userId) {
        return service.find(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Loggable
    public ParticipationRequestDto create(@PathVariable
                                          @NotNull(message = "must not be null")
                                          @PositiveOrZero(message = "must be positive or zero")
                                          Long userId,
                                          @RequestParam
                                          @NotNull(message = "must not be null")
                                          @PositiveOrZero(message = "must be positive or zero")
                                          Long eventId) {
        NewParticipationRequest newParticipationRequest = NewParticipationRequest.builder()
                .userId(userId)
                .eventId(eventId)
                .build();
        return service.create(newParticipationRequest);
    }

    @PatchMapping("/{requestId}/cancel")
    @Loggable
    public ParticipationRequestDto cancel(@PathVariable
                                          @NotNull(message = "must not be null")
                                          @PositiveOrZero(message = "must be positive or zero")
                                          Long userId,
                                          @PathVariable
                                          @NotNull(message = "must not be null")
                                          @PositiveOrZero(message = "must be positive or zero")
                                          Long requestId) {
        CancelParticipationRequest cancelParticipationRequest = CancelParticipationRequest.builder()
                .userId(userId)
                .requestId(requestId)
                .build();
        return service.cancel(cancelParticipationRequest);
    }
}
