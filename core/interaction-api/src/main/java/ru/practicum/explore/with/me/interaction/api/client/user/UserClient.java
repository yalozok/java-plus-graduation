package ru.practicum.explore.with.me.interaction.api.client.user;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserDto;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserShortDto;

import java.util.List;

@FeignClient(name = "user-service",
        path = "/admin/users",
        configuration = UserFeignConfig.class,
        fallback = UserFeignClientFallback.class)
public interface UserClient {
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
}
