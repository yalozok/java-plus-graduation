package ru.practicum.ewm.stats.analyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class AnalyzerRunner implements CommandLineRunner {
    private final EventSimilarityConsumer eventSimilarityConsumer;
    private final UserActionConsumer userActionConsumer;

    @Override
    public void run(String... args) throws Exception {
        log.info("Analyzer is running...");
        Thread userActionThread = new Thread(userActionConsumer);
        userActionThread.setName("user-action-consumer-thread");
        userActionThread.start();

        eventSimilarityConsumer.run();
    }
}
