package ru.practicum.ewm.stats.analyzer.dal.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.analyzer.dal.entity.Interaction;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    @Query("""
            SELECT i.eventId, SUM(i.rating)
            FROM Interaction i
            WHERE i.eventId IN :eventIds
            GROUP BY i.eventId
            """)
    List<Object[]> sumRatingsByEventId(@Param("eventIds") List<Long> eventIds);

    Optional<Interaction> findByEventIdAndUserId(Long eventId, Long userId);

    @Query("""
                SELECT i.eventId
                FROM Interaction i
                WHERE i.userId = :userId
            """)
    Set<Long> findUserInteractedEvents(@Param("userId") long userId);

    @Query("""
                SELECT i
                FROM Interaction i
                WHERE i.userId = :userId
                ORDER BY i.timestamp DESC
            """)
    List<Interaction> findRecentInteractions(@Param("userId") long userId, Pageable pageable);

    @Query("""
                SELECT i
                FROM Interaction i
                WHERE i.userId = :userId
                  AND i.eventId IN :eventIds
            """)
    List<Interaction> findUserRatingsForEvents(@Param("userId") long userId, @Param("eventIds") Set<Long> eventIds);
}
