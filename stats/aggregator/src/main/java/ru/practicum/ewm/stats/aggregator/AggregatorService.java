package ru.practicum.ewm.stats.aggregator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;


@Service
@RequiredArgsConstructor
public class AggregatorService {
    //eventId -> (userId -> weight)
    private final Map<Long, Map<Long, Double>> eventUserWeights = new HashMap<>();

    //userId  -> (eventId -> weight)
    private final Map<Long, Map<Long, Double>> userEventWeights = new HashMap<>();

    //eventId -> sum_all
    private final Map<Long, Double> eventWeightSum = new HashMap<>();

    //eventA  -> (eventB -> sum_min)
    private final Map<Long, Map<Long, Double>> minWeightsSum = new HashMap<>();

    public List<EventSimilarityAvro> handleUserAction(UserActionAvro userAction) {
        long userId = userAction.getUserId();
        long eventA = userAction.getEventId();
        double actionWeight = getActionWeightByType(userAction.getActionType());

        Map<Long, Double> eventUsers = eventUserWeights.computeIfAbsent(eventA, id -> new HashMap<>());
        Map<Long, Double> userEvents = userEventWeights.computeIfAbsent(userId, id -> new HashMap<>());
        Double oldWeight = eventUsers.get(userId);

        if (oldWeight != null && oldWeight >= actionWeight) {
            return Collections.emptyList();
        }

        double deltaWeight = oldWeight == null ? actionWeight : actionWeight - oldWeight;

        eventUsers.put(userId, actionWeight);
        userEvents.put(eventA, actionWeight);
        eventWeightSum.merge(eventA, deltaWeight, Double::sum);

        List<EventSimilarityAvro> similarities = new ArrayList<>();

        for (Map.Entry<Long, Double> event : userEvents.entrySet()) {
            long eventB = event.getKey();
            if (eventB == eventA) continue;

            double weightB = event.getValue();

            long first = Math.min(eventA, eventB);
            long second = Math.max(eventA, eventB);
            double oldMin = oldWeight == null
                    ? 0.0
                    : Math.min(oldWeight, weightB);

            double newMin = Math.min(actionWeight, weightB);
            double deltaMin = newMin - oldMin;

            if (deltaMin <= 0) continue;

            minWeightsSum
                    .computeIfAbsent(first, k -> new HashMap<>())
                    .merge(second, deltaMin, Double::sum);

            similarities.add(buildSimilarity(first, second, userAction.getTimestamp()));
        }
        return similarities;
    }

    private double getActionWeightByType(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

    private EventSimilarityAvro buildSimilarity(long first, long second, Instant timestamp) {
        double sumMin = minWeightsSum
                .getOrDefault(first, Map.of())
                .getOrDefault(second, 0.0);

        double sumA = eventWeightSum.getOrDefault(first, 0.0);
        double sumB = eventWeightSum.getOrDefault(second, 0.0);

        double score = 0.0;
        if (sumMin > 0 && sumA > 0 && sumB > 0) {
            score = sumMin / (Math.sqrt(sumA) * Math.sqrt(sumB));
        }

        return EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(score)
                .setTimestamp(timestamp)
                .build();
    }
}
