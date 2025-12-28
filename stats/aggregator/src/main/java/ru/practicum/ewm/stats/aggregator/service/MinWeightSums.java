package ru.practicum.ewm.stats.aggregator.service;

import java.util.HashMap;
import java.util.Map;

public final class MinWeightSums {
    private final Map<Long, Map<Long, Double>> data = new HashMap<>();

    public void addDelta(long first, long second, double deltaMin) {
        data.computeIfAbsent(first, id -> new HashMap<>()).merge(second, deltaMin, Double::sum);
    }

    public double get(long first, long second) {
        return data.getOrDefault(first, Map.of()).getOrDefault(second, 0.0);
    }
}
