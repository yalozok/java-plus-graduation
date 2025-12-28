package ru.practicum.ewm.stats.aggregator.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class UserEventWeights {
    private final Map<Long, Map<Long, Double>> data = new HashMap<>();

    public void putEventWeight(long userId, long eventId, double weight) {
        data.computeIfAbsent(userId, __ -> new HashMap<>()).put(eventId, weight);
    }

    public Map<Long, Double> eventsForUser(long userId) {
        return data.getOrDefault(userId, Map.of());
    }

    public Set<Map.Entry<Long, Double>> entriesForUser(long userId) {
        return eventsForUser(userId).entrySet();
    }
}
