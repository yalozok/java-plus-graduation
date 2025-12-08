package ru.practicum.explore.with.me.interaction.api.contract;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.with.me.interaction.api.dto.user.NewUserRequest;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserDto;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserShortDto;

import java.util.List;

public interface UserOperations {
    @GetMapping
    List<UserDto> find(@RequestParam(required = false)
                       List<Long> ids,
                       @RequestParam(defaultValue = "0")
                       @PositiveOrZero(message = "must be positive or zero")
                       int from,
                       @RequestParam(defaultValue = "10")
                       @Positive(message = "must be positive")
                       int size);

    @GetMapping("/{id}")
    UserShortDto findById(@PathVariable Long id);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserDto create(@RequestBody
                   @Valid
                   NewUserRequest newUserRequest);

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable
                @Positive(message = "must be positive")
                Long userId);
}
