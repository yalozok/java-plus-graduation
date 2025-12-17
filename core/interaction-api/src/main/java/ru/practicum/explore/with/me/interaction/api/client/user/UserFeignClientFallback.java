package ru.practicum.explore.with.me.interaction.api.client.user;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserDto;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserShortDto;

import java.util.Collections;
import java.util.List;

public class UserFeignClientFallback implements UserClient{

    @GetMapping
    @Override
    public List<UserDto> find(@RequestParam(required = false)
                       List<Long> ids,
                       @RequestParam(defaultValue = "0")
                       @PositiveOrZero(message = "must be positive or zero")
                       int from,
                       @RequestParam(defaultValue = "10")
                       @Positive(message = "must be positive")
                       int size) {
        return Collections.emptyList();
    }

    @GetMapping("/{id}")
    @Override
    public UserShortDto findById(@PathVariable Long id) {
        return UserShortDto.unavailable();
    }
}
