package ru.practicum.explore.with.me.comment.service;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.explore.with.me.interaction.api.exception.*;
import ru.practicum.explore.with.me.logging.Loggable;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler({MissingServletRequestParameterException.class, BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @Loggable
    public ApiError handleBadRequest(BadRequestException e) {
        return ApiError.builder()
                .reason(e.getReason())
                .message(e.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @Loggable
    public ApiError handleNotFound(NotFoundException e) {
        return ApiError.builder()
                .reason(e.getReason())
                .message(e.getMessage())
                .status(HttpStatus.NOT_FOUND)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @Loggable
    public ApiError handleConflict(ConflictException e) {
        return ApiError.builder()
                .reason(e.getReason())
                .message(e.getMessage())
                .status(HttpStatus.CONFLICT)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @Loggable
    public ApiError handleForbidden(ForbiddenException e) {
        return ApiError.builder()
                .reason(e.getReason())
                .message(e.getMessage())
                .status(HttpStatus.FORBIDDEN)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(InternalServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Loggable
    public ApiError handleInternalServer(InternalServerException e) {
        return ApiError.builder()
                .reason(e.getReason())
                .message(e.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @Loggable
    public ApiError handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String reason = "Incorrectly made request.";
        String message = String.format(
                "Failed to convert value of type '%s' to required type '%s'; %s",
                e.getValue() != null ? e.getValue().getClass().getSimpleName() : "(no value received)",
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "(no required type received)",
                e.getCause() != null ? e.getCause().getMessage() : "(no cause received)"
        );

        return ApiError.builder()
                .reason(reason)
                .message(message)
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @Loggable
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String reason = "Incorrectly made request.";
        String message = "Validation error. ";

        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            message = message.concat(error.getField() + ": " + error.getDefaultMessage() + ". ");
        }

        return ApiError.builder()
                .reason(reason)
                .message(message)
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @Loggable
    public ApiError handleConstraintViolation(ConstraintViolationException e) {
        String reason = "Integrity constraint has been violated.";
        String message = String.format(
                "could not execute statement; SQL ['%s']; constraint ['%s']; nested exception is '%s'",
                e.getSQLState() != null ? e.getSQLState() + "; " : "[n/a]; ",
                e.getConstraintName() != null ? e.getConstraintName() + "; " : "[n/a]; ",
                e.getCause() != null ? e.getCause().toString() : "(no cause received)");

        return ApiError.builder()
                .reason(reason)
                .message(message)
                .status(HttpStatus.CONFLICT)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Loggable
    public ApiError handleOthers(Exception e) {
        String reason = "Internal Server Error";
        return ApiError.builder()
                .reason(reason)
                .message(e.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
