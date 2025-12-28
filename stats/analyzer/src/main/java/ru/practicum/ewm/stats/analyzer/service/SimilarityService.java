package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.analyzer.dal.entity.Similarity;
import ru.practicum.ewm.stats.analyzer.dal.repository.SimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.explore.with.me.logging.Loggable;

@Service
@RequiredArgsConstructor
public class SimilarityService {
    private final SimilarityRepository repository;

    @Transactional
    @Loggable
    public void saveEvent(EventSimilarityAvro event) {
        Similarity similarity = new Similarity();
        similarity.setEvent1(event.getEventA());
        similarity.setEvent2(event.getEventB());
        similarity.setSimilarity(event.getScore());
        similarity.setTimestamp(event.getTimestamp());
        repository.save(similarity);
    }


}
