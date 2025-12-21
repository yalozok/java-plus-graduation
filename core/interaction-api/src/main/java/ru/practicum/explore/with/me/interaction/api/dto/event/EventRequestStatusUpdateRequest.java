package ru.practicum.explore.with.me.interaction.api.dto.event;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestStatus;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {
    @NotEmpty(message = "Request IDs must not be empty")
    private List<@NotNull(message = "Request ID cannot be null") Long> requestIds;

    @NotNull(message = "Status must not be null")
    private ParticipationRequestStatus status;
}
