package ru.practicum.stat.service.exception;

public class StatValidationException extends RuntimeException {
    public StatValidationException(String message) {
        super(message);
    }
}
