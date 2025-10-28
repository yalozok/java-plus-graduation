package ru.practicum.stats.service.exception;

public class StatValidationException extends RuntimeException {
    public StatValidationException(String message) {
        super(message);
    }
}
