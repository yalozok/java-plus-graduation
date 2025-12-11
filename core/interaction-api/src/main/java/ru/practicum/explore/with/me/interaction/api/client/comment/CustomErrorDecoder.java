package ru.practicum.explore.with.me.interaction.api.client.comment;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.commons.io.IOUtils;
import ru.practicum.explore.with.me.interaction.api.exception.BadRequestException;
import ru.practicum.explore.with.me.interaction.api.exception.ConflictException;
import ru.practicum.explore.with.me.interaction.api.exception.NotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CustomErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        String message = extractMessage(response);
        return switch (response.status()) {
            case 400 -> new BadRequestException("Bad Request", message);
            case 404 -> new NotFoundException("The required event was not found.", message);
            case 409 -> new ConflictException("For the requested operation the conditions are not met.", message);
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }

    private String extractMessage(Response response) {
        if (response.body() == null) {
            return "No error message available";
        }

        try {
            return IOUtils.toString(response.body().asReader(StandardCharsets.UTF_8));
        } catch (IOException e) {
            return "Failed to extract error message from exception response";
        }
    }
}
