package ru.practicum.explore.with.me.interaction.api.dto.participation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CancelParticipationRequest {
    private Long userId;
    private Long requestId;
}
