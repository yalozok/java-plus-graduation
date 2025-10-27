package ru.practicum.explore.with.me.model.participation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class NewParticipationRequest {
    private Long userId;
    private Long eventId;
}
