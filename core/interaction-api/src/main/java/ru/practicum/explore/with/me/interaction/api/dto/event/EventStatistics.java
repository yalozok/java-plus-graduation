package ru.practicum.explore.with.me.interaction.api.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@AllArgsConstructor
@Data
public class EventStatistics {
    private final Map<Long, Double> ratings;
    private final Map<Long, Integer> confirmedRequests;

    public double getRating(Long eventId) {
        return ratings.getOrDefault(eventId,  0D);
    }

    public int getConfirmedRequests(Long eventId) {
        return confirmedRequests.getOrDefault(eventId, 0);
    }

}
