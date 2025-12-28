package ru.practicum.ewm.stats.aggregator.service;

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
    private final EventUserWeights eventUserWeights = new EventUserWeights();
    private final UserEventWeights userEventWeights = new UserEventWeights();
    private final EventWeightSums eventWeightSums = new EventWeightSums();
    private final MinWeightSums minWeightSums = new MinWeightSums();

    public List<EventSimilarityAvro> handleUserAction(UserActionAvro userAction) {
        long userId = userAction.getUserId();
        long eventA = userAction.getEventId();
        double actionWeight = getActionWeightByType(userAction.getActionType());

        Double oldWeight = eventUserWeights.getUserWeight(eventA, userId);
        if (oldWeight != null && oldWeight >= actionWeight) {
            return Collections.emptyList();
        }

        double deltaWeight = oldWeight == null ? actionWeight : actionWeight - oldWeight;

        eventUserWeights.putUserWeight(eventA, userId, actionWeight);
        userEventWeights.putEventWeight(userId, eventA, actionWeight);
        eventWeightSums.add(eventA, deltaWeight);

        List<EventSimilarityAvro> similarities = new ArrayList<>();

        for (Map.Entry<Long, Double> event : userEventWeights.entriesForUser(userId)) {
            long eventB = event.getKey();
            if (eventB == eventA) continue;

            double weightB = event.getValue();
            minWeightSums.applyUserWeightChange(eventA, eventB, oldWeight, actionWeight, weightB);

            long first = Math.min(eventA, eventB);
            long second = Math.max(eventA, eventB);
            similarities.add(buildSimilarity(first, second, Instant.now()));
        }
        return similarities;
    }

    private double getActionWeightByType(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
            default -> throw new IllegalArgumentException("Unknown action type: " + actionType);
        };
    }

    private EventSimilarityAvro buildSimilarity(long first, long second, Instant timestamp) {
        double sumMin = minWeightSums.get(first,second);

        double sumA = eventWeightSums.get(first);
        double sumB = eventWeightSums.get(second);

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
