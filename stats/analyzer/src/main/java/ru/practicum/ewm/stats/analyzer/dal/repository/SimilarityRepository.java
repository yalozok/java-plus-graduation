package ru.practicum.ewm.stats.analyzer.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.analyzer.dal.entity.Similarity;

import java.util.List;
import java.util.Set;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {
    @Query("""
                SELECT s
                FROM Similarity s
                WHERE s.event1 = :eventId
                   OR s.event2 = :eventId
            """)
    List<Similarity> findSimilaritiesForEvent(@Param("eventId") long eventId);

    @Query("""
                SELECT s
                FROM Similarity s
                WHERE s.event1 IN :eventIds
                   OR s.event2 IN :eventIds
            """)
    List<Similarity> findSimilaritiesForEvents(@Param("eventIds") Set<Long> eventIds);
}
