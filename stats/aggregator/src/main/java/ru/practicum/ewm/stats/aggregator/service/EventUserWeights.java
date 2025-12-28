package ru.practicum.ewm.stats.aggregator.service;

import java.util.HashMap;
import java.util.Map;

public final class EventUserWeights {
    private final Map<Long, Map<Long, Double>> data = new HashMap<>();

    public Double getUserWeight(long eventId, long userId) {
        Map<Long, Double> users = data.get(eventId);
        return users == null ? null : users.get(userId);
    }

    public void putUserWeight(long eventId, long userId, double weight) {
        data.computeIfAbsent(eventId, id -> new HashMap<>()).put(userId, weight);
    }
}
