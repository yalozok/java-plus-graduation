package ru.practicum.explore.with.me.interaction.api.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final String reason;

    public CustomException(String reason, String message) {
        super(message);
        this.reason = reason;
    }
}
