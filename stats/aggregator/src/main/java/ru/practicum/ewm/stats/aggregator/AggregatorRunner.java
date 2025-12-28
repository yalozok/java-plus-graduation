package ru.practicum.ewm.stats.aggregator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AggregatorRunner implements CommandLineRunner {
    private final AggregatorStarter starter;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Aggregator is running...");
        starter.start();
    }
}
