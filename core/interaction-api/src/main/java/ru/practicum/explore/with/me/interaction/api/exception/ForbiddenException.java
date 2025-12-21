package ru.practicum.explore.with.me.interaction.api.exception;

public class ForbiddenException extends CustomException {
    public ForbiddenException(String reason, String message) {
        super(reason, message);
    }
}
