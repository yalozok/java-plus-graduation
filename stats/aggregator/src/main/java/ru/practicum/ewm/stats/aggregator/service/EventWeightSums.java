package ru.practicum.ewm.stats.aggregator.service;

import java.util.HashMap;
import java.util.Map;

public final class EventWeightSums {
    private final Map<Long, Double> data = new HashMap<>();

    public void add(long eventId, double delta) {
        data.merge(eventId, delta, Double::sum);
    }

    public double get(long eventId) {
        return data.getOrDefault(eventId, 0.0);
    }
}
