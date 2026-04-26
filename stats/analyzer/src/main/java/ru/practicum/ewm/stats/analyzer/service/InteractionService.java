package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.analyzer.dal.entity.ActionTypeWeight;
import ru.practicum.ewm.stats.analyzer.dal.entity.Interaction;
import ru.practicum.ewm.stats.analyzer.dal.repository.InteractionRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.explore.with.me.logging.Loggable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InteractionService {
    private final InteractionRepository repository;

    @Transactional
    @Loggable
    public void saveUserAction(UserActionAvro actionAvro) {
        long userId = actionAvro.getUserId();
        long eventId = actionAvro.getEventId();
        Interaction interaction = repository.findByEventIdAndUserId(eventId, userId)
                .orElseGet(() -> createNewInteraction(actionAvro));

        double newRating = ActionTypeWeight.fromAvro(actionAvro.getActionType());
        if (interaction.getId() == null || interaction.getRating() < newRating) {
            interaction.setRating(newRating);
            repository.save(interaction);
        }
    }

    private Interaction createNewInteraction(UserActionAvro actionAvro) {
        Interaction interaction = new Interaction();
        interaction.setEventId(actionAvro.getEventId());
        interaction.setUserId(actionAvro.getUserId());
        interaction.setRating(ActionTypeWeight.fromAvro(actionAvro.getActionType()));
        interaction.setTimestamp(actionAvro.getTimestamp());
        return interaction;
    }

    @Loggable
    @Transactional(readOnly = true)
    public Map<Long, Double> getTotalEventsRating(List<Long> eventIds) {
        List<Object[]> rows = repository.sumRatingsByEventId(eventIds);
        Map<Long, Double> result = new HashMap<>();

        for (Object[] row : rows) {
            Long eventId = (Long) row[0];
            Double sum = (Double) row[1];
            result.put(eventId, sum);
        }
        return result;
    }

}
