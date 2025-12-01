package ru.practicum.stats.client;

public class StatsServerUnavailable extends RuntimeException {
    public StatsServerUnavailable(String message) {
        super(message);
    }
}
