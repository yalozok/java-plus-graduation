package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.analyzer.dal.entity.Interaction;
import ru.practicum.ewm.stats.analyzer.dal.entity.Similarity;
import ru.practicum.ewm.stats.analyzer.dal.repository.InteractionRepository;
import ru.practicum.ewm.stats.analyzer.dal.repository.SimilarityRepository;
import ru.practicum.explore.with.me.logging.Loggable;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final InteractionRepository interactionRepository;
    private final SimilarityRepository similarityRepository;

    @Loggable
    public Map<Long, Double> getSimilarEvents(long eventId, long userId, long maxResults) {
        List<Similarity> similarities = similarityRepository.findSimilaritiesForEvent(eventId);
        if (similarities.isEmpty()) {
            return Map.of();
        }

        Map<Long, Double> similarityMap = similarities.stream()
                .collect(Collectors.toMap(
                        s -> s.getEvent1() == eventId ? s.getEvent2() : s.getEvent1(),
                        Similarity::getSimilarity
                ));
        Set<Long> similarEventIds = similarityMap.keySet();

        Set<Long> interactedEvents = interactionRepository.findUserInteractedEvents(userId);
        Set<Long> eventsInteractedWithBoth = interactedEvents.stream()
                .filter(similarEventIds::contains)
                .collect(Collectors.toSet());

        return similarityMap.entrySet().stream()
                .filter(e -> !eventsInteractedWithBoth.contains(e.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    @Loggable
    public Map<Long, Double> getRecommendationsForUser(long userId, long limit) {
        int RECENT_INTERACTIONS_LIMIT = 20;
        List<Interaction> recentInteractions = interactionRepository
                .findRecentInteractions(userId, PageRequest.of(0, RECENT_INTERACTIONS_LIMIT));
        if (recentInteractions.isEmpty()) {
            return Map.of();
        }

        Set<Long> interactedEventIds = extractEventIds(recentInteractions);
        List<Similarity> similarities = similarityRepository.findSimilaritiesForEvents(interactedEventIds);
        if (similarities.isEmpty()) {
            return Map.of();
        }

        Map<Long, Double> candidateSimilarities = findCandidateSimilarities(interactedEventIds, similarities);
        if (candidateSimilarities.isEmpty()) {
            return Map.of();
        }

        Set<Long> topCandidates = selectTopCandidates(candidateSimilarities, limit);
        Map<Long, Double> userRatings = loadUserRatings(userId, interactedEventIds);
        return predictRatings(topCandidates, interactedEventIds, userRatings, similarities);
    }

    private Set<Long> extractEventIds(List<Interaction> interactions) {
        return interactions.stream()
                .map(Interaction::getEventId)
                .collect(Collectors.toSet());
    }

    private Map<Long, Double> findCandidateSimilarities(Set<Long> interactedEventIds, List<Similarity> similarities) {
        Map<Long, Double> result = new HashMap<>();

        for (Similarity s : similarities) {
            long candidate = interactedEventIds.contains(s.getEvent1()) ? s.getEvent2() : s.getEvent1();

            if (interactedEventIds.contains(candidate)) {
                continue;
            }
            result.merge(candidate, s.getSimilarity(), Math::max);
        }
        return result;
    }


    private Set<Long> selectTopCandidates(Map<Long, Double> similarities, long limit) {
        return similarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Map<Long, Double> loadUserRatings(long userId, Set<Long> interactedEventIds) {
        return interactionRepository.findUserRatingsForEvents(userId, interactedEventIds)
                .stream()
                .collect(Collectors.toMap(
                        Interaction::getEventId,
                        Interaction::getRating
                ));
    }

    private Map<Long, Double> predictRatings(Set<Long> candidates,
                                             Set<Long> interactedEventIds,
                                             Map<Long, Double> userRatings,
                                             List<Similarity> similarities) {
        Map<Long, Double> result = new LinkedHashMap<>();

        for (Long candidate : candidates) {
            Map<Long, Double> neighbors = findNearestNeighbors(candidate, similarities, interactedEventIds);

            double score = calculateScore(neighbors, userRatings);
            if (score > 0) {
                result.put(candidate, score);
            }
        }
        return result;
    }

    private Map<Long, Double> findNearestNeighbors(long candidate,
                                                   List<Similarity> similarities, Set<Long> interactedEventIds) {
        int NEIGHBORS = 5;
        return similarities.stream()
                .filter(s -> (s.getEvent1() == candidate && interactedEventIds.contains(s.getEvent2()))
                        || (s.getEvent2() == candidate && interactedEventIds.contains(s.getEvent1()))
                )
                .sorted(Comparator.comparingDouble(Similarity::getSimilarity).reversed())
                .limit(NEIGHBORS)
                .collect(Collectors.toMap(
                        s -> s.getEvent1() == candidate ? s.getEvent2() : s.getEvent1(),
                        Similarity::getSimilarity,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private double calculateScore(Map<Long, Double> neighbors, Map<Long, Double> userRatings) {
        double weightedSum = 0.0;
        double similaritySum = 0.0;

        for (var entry : neighbors.entrySet()) {
            double similarity = entry.getValue();
            double rating = userRatings.get(entry.getKey());

            weightedSum += rating * similarity;
            similaritySum += similarity;
        }
        return similaritySum > 0 ? weightedSum / similaritySum : 0.0;
    }
}
