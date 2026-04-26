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

    public void applyUserWeightChange(long eventA,
                                      long eventB,
                                      Double oldWeightA,
                                      double newWeightA,
                                      double weightB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        double oldMin = oldWeightA == null ? 0.0 : Math.min(oldWeightA, weightB);
        double newMin = Math.min(newWeightA, weightB);
        double deltaMin = newMin - oldMin;

        if (deltaMin > 0) {
            addDelta(first,second,deltaMin);
        }
    }
}
